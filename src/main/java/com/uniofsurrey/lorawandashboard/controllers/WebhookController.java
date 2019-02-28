package com.uniofsurrey.lorawandashboard.controllers;

import com.google.gson.*;
import com.uniofsurrey.lorawandashboard.entities.Device;
import com.uniofsurrey.lorawandashboard.entities.Pairing;
import com.uniofsurrey.lorawandashboard.entities.Webhook;
import com.uniofsurrey.lorawandashboard.models.DeviceConsistDTO;
import com.uniofsurrey.lorawandashboard.repositories.DeviceRepository;
import com.uniofsurrey.lorawandashboard.repositories.PairingRepository;
import com.uniofsurrey.lorawandashboard.repositories.WebhookRepository;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class WebhookController {
    private WebhookRepository webhookRepository;
    private PairingRepository pairingRepository;
    private DeviceRepository deviceRepository;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public WebhookController(WebhookRepository webhookRepository, PairingRepository pairingRepository, DeviceRepository deviceRepository) {
        this.webhookRepository = webhookRepository;
        this.pairingRepository = pairingRepository;
        this.deviceRepository = deviceRepository;
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
        Pairing pairing = pairingRepository.findByDevice1OrDevice2(currentWebhook, currentWebhook);
        if (pairing != null) {
            List<Webhook> webhookToCompare;
            if (pairing.getDevice1() == currentWebhook) {
                webhookToCompare = webhookRepository.findByDevIdAndFlagFalseOrderByDateTimeDesc(pairing.getDevice2().getDeviceName());
            }
            else {
                webhookToCompare = webhookRepository.findByDevIdAndFlagFalseOrderByDateTimeDesc(pairing.getDevice1().getDeviceName());
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
                logger.info("No webhook found for pairing within 10 minutes of report");
            }
        }
        else {
            logger.info("There is not a pairing for this device, cannot continue");
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
