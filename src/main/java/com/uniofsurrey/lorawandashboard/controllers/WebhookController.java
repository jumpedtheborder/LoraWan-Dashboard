package com.uniofsurrey.lorawandashboard.controllers;

import com.google.gson.*;
import com.uniofsurrey.lorawandashboard.entities.Device;
import com.uniofsurrey.lorawandashboard.entities.Grouping;
import com.uniofsurrey.lorawandashboard.entities.Webhook;
import com.uniofsurrey.lorawandashboard.entities.PendingDeviceWebhook;
import com.uniofsurrey.lorawandashboard.entities.UnregisteredDeviceWebhook;
import com.uniofsurrey.lorawandashboard.models.DeviceConsistDTO;
import com.uniofsurrey.lorawandashboard.repositories.DeviceRepository;
import com.uniofsurrey.lorawandashboard.repositories.GroupingRepository;
import com.uniofsurrey.lorawandashboard.repositories.WebhookRepository;
import com.uniofsurrey.lorawandashboard.repositories.PendingDeviceWebhookRepository;
import com.uniofsurrey.lorawandashboard.repositories.UnregisteredDeviceWebhookRepository;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        //JsonObject nextObject = rootObject.getAsJsonObject("webhookJson");
        //Change to nextObject for debugging test
        String payload = rootObject.getAsJsonPrimitive("data").getAsString();

        byte[] bytes = Base64.decodeBase64(payload);

        //Change to nextObject for debugging test
        JsonArray rxInfo = rootObject.getAsJsonArray("rxInfo");
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
            if(counter == 3) {
                builder.append(",");
            }
            builder.append(String.format("%02X",b));
            counter += 1;
        }

        Device device = deviceRepository.findByDeviceName(rootObject.getAsJsonPrimitive("applicationID").getAsString());

        if (device == null) {
            //Store device consists temporarily until device has been registered
            UnregisteredDeviceWebhook unregisteredDeviceWebhook = new UnregisteredDeviceWebhook();
            unregisteredDeviceWebhook.setAppId(rootObject.getAsJsonPrimitive("applicationID").getAsString());
            unregisteredDeviceWebhook.setDevId(rootObject.getAsJsonPrimitive("devEUI").getAsString());
            unregisteredDeviceWebhook.setCandidateConsist(builder.toString());
            unregisteredDeviceWebhook.setDateTime(zonedDateTime);
            unregisteredDeviceWebhookRepository.save(unregisteredDeviceWebhook);
        }

        else {
            //Save to table if the device exists and awaiting processing
            PendingDeviceWebhook pending = new PendingDeviceWebhook();
            pending.setAppId(rootObject.getAsJsonPrimitive("applicationID").getAsString());
            pending.setDevId(rootObject.getAsJsonPrimitive("devEUI").getAsString());
            pending.setCandidateConsist(builder.toString());
            pending.setDateTime(zonedDateTime);
            pendingDeviceWebhookRepository.save(pending);
        }
    }

    public void retrieveAllWebhooks() {
        Iterable<Grouping> allGroups = groupingRepository.findAll();
        for (Grouping grouping : allGroups) {
            List<Device> devicesByGrouping = deviceRepository.findAllByGrouping(grouping);
            List<PendingDeviceWebhook> allPendingWebhooks = new ArrayList<>();
            for (int i = 0; i < devicesByGrouping.size(); i++) {
                List<PendingDeviceWebhook> pendingDeviceWebhooks = pendingDeviceWebhookRepository.findAllByDevId(devicesByGrouping.get(i).getDeviceName());
                for (int j = 0; j < pendingDeviceWebhooks.size(); j++) {
                    allPendingWebhooks.add(pendingDeviceWebhooks.get(i));
                }
            }
            validateConsist(allPendingWebhooks);
        }
    }

    public void validateConsist(List<PendingDeviceWebhook> allPendingWebhooks) {
        boolean validTime = true;
        LocalDateTime localDateTime = LocalDateTime.now();

        for (PendingDeviceWebhook pendingDeviceWebhook : allPendingWebhooks) {
            LocalDateTime currentPendingTime = pendingDeviceWebhook.getDateTime().toLocalDateTime();
            //Must wait one minute before processing begins
            if (ChronoUnit.MINUTES.between(currentPendingTime, localDateTime) < 1) {
                validTime = false;
            }
        }
        if (validTime == true) {
            Map<String, Integer> consistMap = new HashMap<>();
            Map<String, Integer> directionMap = new HashMap<>();
            List<PendingDeviceWebhook> devicesToMove = new ArrayList<>();

            for (int i = 0; i < allPendingWebhooks.size(); i++) {
                PendingDeviceWebhook currentWebhook = allPendingWebhooks.get(i);
                LocalDateTime localDateTimeForCurrentWebhook = currentWebhook.getDateTime().toLocalDateTime();
                for (int j = i + 1; j < allPendingWebhooks.size(); i++) {
                    PendingDeviceWebhook comparisonWebhook = allPendingWebhooks.get(j);

                    LocalDateTime localDateTimeForComparisonWebhook = comparisonWebhook.getDateTime().toLocalDateTime();
                    if (ChronoUnit.MINUTES.between(localDateTimeForCurrentWebhook, localDateTimeForComparisonWebhook) <= 1) {
                        if (devicesToMove.contains(currentWebhook) == false) {
                            devicesToMove.add(currentWebhook);
                        }
                        if (devicesToMove.contains(comparisonWebhook) == false) {
                            devicesToMove.add(comparisonWebhook);
                        }
                        String correctConsist = getCorrectConsist(currentWebhook, comparisonWebhook);
                        String correctDirection = getCorrectDirection(currentWebhook, comparisonWebhook);
                        if (consistMap.containsKey(correctConsist)) {
                            consistMap.put(correctConsist, consistMap.get(correctConsist) + 1);
                        }
                        else {
                            consistMap.put(correctConsist, 1);
                        }

                        if (directionMap.containsKey(correctDirection)) {
                            directionMap.put(correctDirection, directionMap.get(correctDirection) + 1);
                        }
                        else {
                            directionMap.put(correctDirection, 1);
                        }
                    }
                }
            }
            int frequencyConsist = 0;
            String elementConsist = "";
            Set<Map.Entry<String, Integer>> entrySet = consistMap.entrySet();
            for(Map.Entry<String, Integer> entry: entrySet) {
                if(entry.getValue() > frequencyConsist) {
                    elementConsist = entry.getKey();
                    frequencyConsist = entry.getValue();
                }
            }

            int frequencyDirection = 0;
            String elementDirection = "";
            Set<Map.Entry<String, Integer>> entrySetDirection = directionMap.entrySet();
            for(Map.Entry<String, Integer> entry: entrySetDirection) {
                if(entry.getValue() > frequencyDirection) {
                    elementDirection = entry.getKey();
                    frequencyDirection = entry.getValue();
                }
            }

            for (PendingDeviceWebhook pending : allPendingWebhooks) {
                Webhook webhook = new Webhook();
                webhook.setAppId(pending.getAppId());
                webhook.setDevId(pending.getDevId());
                webhook.setDateTime(pending.getDateTime());
                webhook.setCandidateConsist(elementConsist);
                webhook.setDirection(elementDirection);
                webhookRepository.save(webhook);

                pendingDeviceWebhookRepository.delete(pending);
            }
        }
    }

    public String getCorrectConsist(PendingDeviceWebhook pending1, PendingDeviceWebhook pending2) {
        String[] pending1Consist = pending1.getCandidateConsist().split(",");
        String[] pending2Consist = pending2.getCandidateConsist().split(",");
        List<String> webhook1ConsistList = new ArrayList(Arrays.asList(pending1Consist));
        List<String> webhook2ConsistList = new ArrayList(Arrays.asList(pending2Consist));
        List<String> webhook1ConsistListCopy = new ArrayList<>();
        List<String> webhook2ConsistListCopy = new ArrayList<>();
        String overs;
        String correctConsist = "";

        if (pending1Consist.length > pending2Consist.length) {

            for (String value : webhook1ConsistList) {
                webhook1ConsistListCopy.add(value);
            }

            webhook2ConsistList.removeAll(webhook1ConsistList);
            if (webhook2ConsistList.size() == 0) {
                logger.info("pending2 is valid");

                webhook1ConsistListCopy.removeAll(webhook2ConsistList);

                overs = String.join(",", webhook1ConsistListCopy );

                PendingDeviceWebhook webhookNew = new PendingDeviceWebhook();
                webhookNew.setAppId(pending1.getAppId());
                webhookNew.setDevId(pending1.getDevId());
                webhookNew.setDateTime(pending1.getDateTime());
                webhookNew.setCandidateConsist(overs);
                pendingDeviceWebhookRepository.save(webhookNew);
                logger.info("New webhook record created!");

                correctConsist = pending2.getCandidateConsist();
            }
        }

        else if (pending1Consist.length < pending2Consist.length) {

            for (String value : webhook2ConsistList) {
                webhook2ConsistListCopy.add(value);
            }

            webhook1ConsistList.removeAll(webhook2ConsistList);
            if (webhook1ConsistList.size() == 0) {
                logger.info("webhook1 is valid");

                webhook2ConsistListCopy.removeAll(webhook1ConsistList);

                overs = String.join(",", webhook2ConsistListCopy );

                PendingDeviceWebhook webhookNew = new PendingDeviceWebhook();
                webhookNew.setAppId(pending2.getAppId());
                webhookNew.setDevId(pending2.getDevId());
                webhookNew.setDateTime(pending2.getDateTime());
                webhookNew.setCandidateConsist(overs);
                pendingDeviceWebhookRepository.save(webhookNew);
                logger.info("New webhook record created!");

                correctConsist = pending1.getCandidateConsist();

            }
        }

        else if (pending1Consist.length == pending2Consist.length) {

            webhook1ConsistList.removeAll(webhook2ConsistList);
            if (webhook1ConsistList.size() == 0) {
                logger.info("webhook1 and webhook2 is valid");

                correctConsist = pending2.getCandidateConsist();
            }
        }

        return correctConsist;
    }

    public String getCorrectDirection(PendingDeviceWebhook pending1, PendingDeviceWebhook pending2) {
        Device device1 = deviceRepository.findByDeviceName(pending1.getDevId());
        Device device2 = deviceRepository.findByDeviceName(pending2.getDevId());
        String direction;

        if (pending1.getDateTime().toLocalDateTime().isAfter(pending2.getDateTime().toLocalDateTime()) && device2.getGroupOrder() > device1.getGroupOrder()) {
            direction = "E/W";
        }
        else if (pending1.getDateTime().toLocalDateTime().isBefore(pending2.getDateTime().toLocalDateTime()) && device2.getGroupOrder() > device1.getGroupOrder()) {
            direction = "W/E";
        }
        else {
            direction = "UNKNOWN";
        }

        return direction;
    }

  /*  @PostMapping("/webhook")
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
            if(counter == 3) {
                builder.append(",");
            }
            builder.append(String.format("%02X",b));
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

        Device currentDevice = deviceRepository.findByDeviceName(webhook1.getDevId());

        if (currentDevice.getId() != null) {
            validatePairing(currentDevice, webhook1);
        }
        else {
            logger.info("The device " + webhook1.getDevId() + " has not been registered");
        }

    }

    public void validatePairing(Device currentWebhook, Webhook webhook1) {
        Grouping grouping = groupingRepository.findByGroupName(currentWebhook.getGrouping().toString());
        if (grouping != null) {
            List<Webhook> webhookToCompare;
            if (grouping.getDevice1() == currentWebhook) {
                webhookToCompare = webhookRepository.findByDevIdAndFlagFalseOrderByDateTimeDesc(grouping.getDevice2().getDeviceName());
            }
            else {
                webhookToCompare = webhookRepository.findByDevIdAndFlagFalseOrderByDateTimeDesc(grouping.getDevice1().getDeviceName());
            }

            LocalDateTime localDateTimeForCurrentWebhook = webhook1.getDateTime().toLocalDateTime();

            boolean valid = false;
            LocalDateTime localDateTimeForComparisonWebhook;
            Webhook webhook2 = new Webhook();
            for (int i = 0; i < webhookToCompare.size(); i++) {
                if (valid == false) {
                    localDateTimeForComparisonWebhook = webhookToCompare.get(i).getDateTime().toLocalDateTime();
                    if (ChronoUnit.MINUTES.between(localDateTimeForCurrentWebhook, localDateTimeForComparisonWebhook) <= 10) {
                        webhook2 = webhookToCompare.get(i);
                        valid = true;
                    }
                }
            }
            if (webhook2.getDevId() != null) {
                validate(webhook1, webhook2);
            }
            else {
                logger.info("No webhook found for grouping within 10 minutes of report");
            }
        }
        else {
            logger.info("There is not a grouping for this device, cannot continue");
        }
    }

    public void validate(Webhook webhook1, Webhook webhook2) {
        String[] webhook1Consist = webhook1.getCandidateConsist().split(",");
        String[] webhook2Consist = webhook2.getCandidateConsist().split(",");
        List<String> webhook1ConsistList = new ArrayList(Arrays.asList(webhook1Consist));
        List<String> webhook2ConsistList = new ArrayList(Arrays.asList(webhook2Consist));
        List<String> webhook1ConsistListCopy = new ArrayList<>();
        List<String> webhook2ConsistListCopy = new ArrayList<>();
        String overs;

        if (webhook1Consist.length > webhook2Consist.length) {

            for (String value : webhook1ConsistList) {
                webhook1ConsistListCopy.add(value);
            }

            webhook2ConsistList.removeAll(webhook1ConsistList);
            if (webhook2ConsistList.size() == 0) {
                logger.info("webhook2 is valid, setting flags as true");

                if (webhook1.getDateTime().toLocalDateTime().isAfter(webhook2.getDateTime().toLocalDateTime())) {
                    webhook1.setDirection("E/W");
                    webhook2.setDirection("E/W");
                }
                else if (webhook1.getDateTime().toLocalDateTime().isBefore(webhook2.getDateTime().toLocalDateTime())) {
                    webhook1.setDirection("W/E");
                    webhook2.setDirection("W/E");
                }
                else {
                    webhook1.setDirection("UNKNOWN");
                    webhook2.setDirection("UNKNOWN");
                }

                webhook1.setFlag(true);
                webhook2.setFlag(true);

                webhookRepository.save(webhook1);
                webhookRepository.save(webhook2);

                webhook1ConsistListCopy.removeAll(webhook2ConsistList);

                overs = String.join(",", webhook1ConsistListCopy );

                Webhook webhookNew = new Webhook();
                webhookNew.setAppId(webhook1.getAppId());
                webhookNew.setDevId(webhook1.getDevId());
                webhookNew.setFlag(false);
                webhookNew.setDateTime(webhook1.getDateTime());
                webhookNew.setCandidateConsist(overs);
                webhookRepository.save(webhookNew);
                logger.info("New webhook record created!");


            }
        }

        else if (webhook1Consist.length < webhook2Consist.length) {

            for (String value : webhook2ConsistList) {
                webhook2ConsistListCopy.add(value);
            }

            webhook1ConsistList.removeAll(webhook2ConsistList);
            if (webhook1ConsistList.size() == 0) {
                logger.info("webhook1 is valid, setting flags as true");

                if (webhook1.getDateTime().toLocalDateTime().isAfter(webhook2.getDateTime().toLocalDateTime())) {
                    webhook1.setDirection("E/W");
                    webhook2.setDirection("E/W");
                }
                else if (webhook1.getDateTime().toLocalDateTime().isBefore(webhook2.getDateTime().toLocalDateTime())) {
                    webhook1.setDirection("W/E");
                    webhook2.setDirection("W/E");
                }
                else {
                    webhook1.setDirection("UNKNOWN");
                    webhook2.setDirection("UNKNOWN");
                }

                webhook1.setFlag(true);
                webhook2.setFlag(true);

                webhookRepository.save(webhook1);
                webhookRepository.save(webhook2);

                webhook2ConsistListCopy.removeAll(webhook1ConsistList);

                overs = String.join(",", webhook2ConsistListCopy );

                Webhook webhookNew = new Webhook();
                webhookNew.setAppId(webhook2.getAppId());
                webhookNew.setDevId(webhook2.getDevId());
                webhookNew.setFlag(false);
                webhookNew.setDateTime(webhook2.getDateTime());
                webhookNew.setCandidateConsist(overs);
                webhookRepository.save(webhookNew);
                logger.info("New webhook record created!");
            }
        }

        else if (webhook1Consist.length == webhook2Consist.length) {

            webhook1ConsistList.removeAll(webhook2ConsistList);
            if (webhook1ConsistList.size() == 0) {
                logger.info("webhook1 and webhook2 is valid, setting flags as true");

                if (webhook1.getDateTime().toLocalDateTime().isAfter(webhook2.getDateTime().toLocalDateTime())) {
                    webhook1.setDirection("E/W");
                    webhook2.setDirection("E/W");
                }
                else if (webhook1.getDateTime().toLocalDateTime().isBefore(webhook2.getDateTime().toLocalDateTime())) {
                    webhook1.setDirection("W/E");
                    webhook2.setDirection("W/E");
                }
                else {
                    webhook1.setDirection("UNKNOWN");
                    webhook2.setDirection("UNKNOWN");
                }

                webhook1.setFlag(true);
                webhook2.setFlag(true);

                webhookRepository.save(webhook1);
                webhookRepository.save(webhook2);
            }
        }
    }
*/

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


}
