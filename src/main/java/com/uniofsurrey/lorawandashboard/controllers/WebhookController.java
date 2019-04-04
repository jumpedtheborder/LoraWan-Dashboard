package com.uniofsurrey.lorawandashboard.controllers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.uniofsurrey.lorawandashboard.entities.Device;
import com.uniofsurrey.lorawandashboard.entities.Grouping;
import com.uniofsurrey.lorawandashboard.entities.Webhook;
import com.uniofsurrey.lorawandashboard.models.DeviceConsistDTO;
import com.uniofsurrey.lorawandashboard.repositories.*;
import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@EnableScheduling
@RestController
public class WebhookController {
    private WebhookRepository webhookRepository;
    private GroupingRepository groupingRepository;
    private DeviceRepository deviceRepository;
    private UnregisteredDeviceWebhookRepository unregisteredDeviceWebhookRepository;
    private PendingDeviceWebhookRepository pendingDeviceWebhookRepository;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public WebhookController(WebhookRepository webhookRepository, GroupingRepository groupingRepository, DeviceRepository deviceRepository, UnregisteredDeviceWebhookRepository unregisteredDeviceWebhookRepository, PendingDeviceWebhookRepository pendingDeviceWebhookRepository) {
        this.webhookRepository = webhookRepository;
        this.groupingRepository = groupingRepository;
        this.deviceRepository = deviceRepository;
        this.unregisteredDeviceWebhookRepository = unregisteredDeviceWebhookRepository;
        this.pendingDeviceWebhookRepository = pendingDeviceWebhookRepository;
    }

    @PostMapping("/webhook")
    public void createWebhook(@RequestBody String webhookJson) {

        logger.info(webhookJson);
        JsonParser parser = new JsonParser();
        JsonObject rootObject = parser.parse(webhookJson).getAsJsonObject();
        JsonObject nextObject = rootObject.getAsJsonObject("webhookJson");
        //Change to nextObject for debugging test
        String payload = nextObject.getAsJsonPrimitive("data").getAsString();

        byte[] bytes = Base64.decodeBase64(payload);

        //Change to nextObject for debugging test
        JsonArray rxInfo = nextObject.getAsJsonArray("rxInfo");
        String datetime = "";
        for (JsonElement element : rxInfo) {
            datetime = element.getAsJsonObject().getAsJsonPrimitive("time").getAsString();
        }

        ZonedDateTime zonedDateTime = ZonedDateTime.parse(datetime);

        byte[] consist_bytes;

        //Take only the bytes that correspond with the consist
        consist_bytes = Arrays.copyOfRange(bytes, 3, bytes.length);

        //Create consist string, splitting each 6th character with a comma except the last one
        StringBuilder builder = new StringBuilder();
        int counter = 0;
        for (byte b : consist_bytes) {
            if (counter == 3) {
                builder.append(",");
            }
            builder.append(String.format("%02X", b));
            counter += 1;
        }

        //Save the webhook for that device
        Webhook webhook1 = new Webhook();
        //Replace both rootObject with nextObject for debugging
        webhook1.setAppId(nextObject.getAsJsonPrimitive("applicationID").getAsString());
        webhook1.setDevId(nextObject.getAsJsonPrimitive("devEUI").getAsString());
        webhook1.setCandidateConsist(builder.toString());
        webhook1.setDateTime(zonedDateTime);
        webhook1.setFlag(false);
        webhookRepository.save(webhook1);

        //Replace rootObject with nextObject for internal webhook
        Device device = deviceRepository.findByDeviceName(nextObject.getAsJsonPrimitive("devEUI").getAsString());

        if (device != null) {
            validateWebhook(device);
        } else {
            logger.info("device is null");
        }
    }


    //get all webhooks
    public List<Webhook> getAllWebhooksOrderTimeDesc(Device currentDevice){
        Grouping deviceGroup = groupingRepository.findByGroupName(currentDevice.getGrouping().getGroupName());
        List<Device> devicesInGroup = deviceRepository.findAllByGrouping(deviceGroup);
        logger.info("Devices in a group: " + devicesInGroup.toString());
        List<Webhook> allWebhooksFromDevicesInGroup = new ArrayList<>();
        for (Device device : devicesInGroup) {
            List<Webhook> webhooksFromDevice = webhookRepository.findByDevIdAndFlagIsFalse(device.getDeviceName());
            allWebhooksFromDevicesInGroup.addAll(webhooksFromDevice);
        }
        logger.info("all webhooks from group: " + allWebhooksFromDevicesInGroup);
        if (!allWebhooksFromDevicesInGroup.isEmpty()){
            Collections.sort(allWebhooksFromDevicesInGroup);
            logger.info("All webhooks from device" + allWebhooksFromDevicesInGroup);
        }

        return allWebhooksFromDevicesInGroup;
    }


    //Divide webhooks into segments
    public List<List<Webhook>> segmentedWebhooks(List<Webhook> webhooks, Device currentDevice){
        Grouping deviceGroup = groupingRepository.findByGroupName(currentDevice.getGrouping().getGroupName());
        int numberDevicesInGroup = deviceRepository.findAllByGrouping(deviceGroup).size();
        List<List<Webhook>> webhookArrayList = new ArrayList<>();
        int numberOfArrays;
        if ( webhooks.size() % numberDevicesInGroup > 0){
            numberOfArrays  = ((webhooks.size() / numberDevicesInGroup) +  1 );
        }
        else {
            numberOfArrays  = (webhooks.size() / numberDevicesInGroup);
        }

        logger.info("Number of arrays:" + numberOfArrays);

        int count = 0;
        int index = 0;

        while (count < numberOfArrays) {
            logger.info("segementation of webhooks");
            List<Webhook> sectionedWebhooks = new ArrayList<>();
            logger.info("before webhook array segementation loop");
            while (index < webhooks.size()){
                logger.info("DEVID: " + webhooks.get(index).getDevId());
                sectionedWebhooks.add(webhooks.get(index));
                index++;
            }
            webhookArrayList.add(sectionedWebhooks);
            count++;
        }
        logger.info("after loop; separated webhook arrays: " + webhookArrayList);

        return  webhookArrayList;
    }

    //validate consist
    public void calculateConsists(List<Webhook> sectionedWebhook){
        Map<String, Integer> frequencyMap = new HashMap<>();
        for( Webhook hook : sectionedWebhook) {
            Integer frequency = frequencyMap.get(hook.getCandidateConsist());
            frequencyMap.put(hook.getCandidateConsist(), frequency == null ? 1 : frequency + 1);
        }

        String maxFrequency = GetMostFrequentValueFrom(frequencyMap);
        logger.info("most frequent consist:" + maxFrequency);

        for(Webhook hook: sectionedWebhook) {
            hook.setCandidateConsist(maxFrequency);
            webhookRepository.save(hook);
        }
    }

    //validate direction
    public void calculateDirection(List<Webhook> sectionedWebhook){
        int index = 0;
        int comparisonId = 1;

        Map<String, Integer> frequencyMap = new HashMap<>();
        while(index < sectionedWebhook.size() - 1){
            Webhook hook = sectionedWebhook.get(index);
            if ((hook.getDateTime().isAfter(sectionedWebhook.get(comparisonId).getDateTime()))
                    && (deviceRepository.findByDeviceName(hook.getDevId()).getGroupOrder() < deviceRepository.findByDeviceName(sectionedWebhook.get(comparisonId).getDevId()).getGroupOrder())) {
                logger.info("if direction here!");
                Integer frequency = frequencyMap.get("E/W");
                frequencyMap.put("E/W", frequency == null ? 1 : frequency + 1);
            } else if ((hook.getDateTime().isBefore(sectionedWebhook.get(comparisonId).getDateTime()))
                    && (deviceRepository.findByDeviceName(hook.getDevId()).getGroupOrder() > deviceRepository.findByDeviceName(sectionedWebhook.get(comparisonId).getDevId()).getGroupOrder())) {
                logger.info("else if direction here!");
                Integer frequency = frequencyMap.get("W/E");
                frequencyMap.put("W/E", frequency == null ? 1 : frequency + 1);
            } else {
                logger.info("else direction here!");
                Integer frequency = frequencyMap.get("UNKNOWN");
                frequencyMap.put("UNKNOWN", frequency == null ? 1 : frequency + 1);
            }
            if(comparisonId <= sectionedWebhook.size()){
                comparisonId++;
            }
            index++;
        }
//        for ( Webhook hook : sectionedWebhook.) {
//            if ((hook.getDateTime().isAfter(sectionedWebhook.get(comparisonId).getDateTime()))
//                    && (deviceRepository.findByDeviceName(hook.getDevId()).getGroupOrder() > deviceRepository.findByDeviceName(sectionedWebhook.get(comparisonId).getDevId()).getGroupOrder())) {
//                logger.info("if direction here!");
//                Integer frequency = frequencyMap.get("E/W");
//                frequencyMap.put("E/W", frequency == null ? 1 : frequency + 1);
//            } else if ((hook.getDateTime().isBefore(sectionedWebhook.get(comparisonId).getDateTime()))
//                    && (deviceRepository.findByDeviceName(hook.getDevId()).getGroupOrder() < deviceRepository.findByDeviceName(sectionedWebhook.get(comparisonId).getDevId()).getGroupOrder())) {
//                logger.info("else if direction here!");
//                Integer frequency = frequencyMap.get("W/E");
//                frequencyMap.put("W/E", frequency == null ? 1 : frequency + 1);
//            } else {
//                logger.info("else direction here!");
//                Integer frequency = frequencyMap.get("UNKNOWN");
//                frequencyMap.put("UNKNOWN", frequency == null ? 1 : frequency + 1);
//            }
//            if(comparisonId <= sectionedWebhook.size()){
//                comparisonId++;
//            }
//        }

        String maxFrequency = GetMostFrequentValueFrom(frequencyMap);
        logger.info("most frequent direction:" + maxFrequency);

        for(Webhook hook: sectionedWebhook) {
            hook.setDirection(maxFrequency);
            hook.setFlag(true);
            webhookRepository.save(hook);
        }

    }

    public void validateWebhook(Device currentDevice){
        logger.info("start vaildation");
        List<Webhook> allWebhooks = getAllWebhooksOrderTimeDesc(currentDevice);
        logger.info("all webhooks successful");
        List<List<Webhook>> segmentedWebhooks = segmentedWebhooks(allWebhooks, currentDevice);
        logger.info("segmented webhooks successful");

        int i = 0;
        while (i < segmentedWebhooks.size()){
            calculateConsists(segmentedWebhooks.get(i));
            calculateDirection(segmentedWebhooks.get(i));
            i++;
        }
        logger.info("end of validation.");
    }


    @GetMapping("/rest/webhook/consist/{deviceId}")
    public List<DeviceConsistDTO> getWebhooksById(@PathVariable Long deviceId) {

        Device device = deviceRepository.findById(deviceId).orElseThrow(() -> new RuntimeException ("not found"));

        List<Webhook> webhooks = webhookRepository.findByDevId(device.getDeviceName());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        List<DeviceConsistDTO> deviceConsistDtos = new ArrayList<>();

        for (Webhook webhook : webhooks) {
            DeviceConsistDTO currentDeviceDto = new DeviceConsistDTO();
            currentDeviceDto.setDeviceName(webhook.getDevId());
            currentDeviceDto.setCandidateConsist(webhook.getCandidateConsist());
            currentDeviceDto.setDirection(webhook.getDirection());
            currentDeviceDto.setFormattedTime(webhook.getDateTime().format(formatter));
            deviceConsistDtos.add(currentDeviceDto);
        }

        return deviceConsistDtos;
    }

    private String GetMostFrequentValueFrom(Map<String,Integer> frequencyMap){
        Map.Entry<String, Integer> maxFrequency = null;
        for( Map.Entry<String, Integer> mapEntry : frequencyMap.entrySet()){
            if(maxFrequency == null || mapEntry.getValue() > maxFrequency.getValue()) {
                maxFrequency = mapEntry;
            }
        }

        return maxFrequency.getKey();
    }

}
