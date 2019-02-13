package com.uniofsurrey.lorawandashboard.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.uniofsurrey.lorawandashboard.entities.*;
import com.uniofsurrey.lorawandashboard.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.time.LocalDateTime;

@Component
public class Init {
    private final UserRepository userRepository;
    private final RegionRepository regionRepository;
    private final DeviceRepository deviceRepository;
    private final ReportRepository reportRepository;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final UserPasswordEncoder userPasswordEncoder = new UserPasswordEncoder();

    @Autowired
    public Init(UserRepository userRepository, RegionRepository regionRepository, DeviceRepository deviceRepository, ReportRepository reportRepository) {
        this.userRepository = userRepository;
        this.regionRepository = regionRepository;
        this.deviceRepository = deviceRepository;
        this.reportRepository = reportRepository;
    }


    private void createRegions() {
        JsonParser parser = new JsonParser();
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("GBR_adm2.json");
        Reader reader = new InputStreamReader(inputStream);
        JsonElement rootElement = parser.parse(reader);
        JsonObject rootObject = rootElement.getAsJsonObject();
        JsonArray features = rootObject.getAsJsonArray("features");

        for (JsonElement element : features) {
            JsonObject properties = element.getAsJsonObject().getAsJsonObject("properties");
            Region region = new Region();
            region.setRegionName(properties.get("NAME_2").getAsString());
            regionRepository.save(region);
            }
    }



    private User checkAdminUser() {

        User admin = userRepository.findByUsername("admin@admin.com");
        if (admin != null) return admin;

        Region region = regionRepository.findByRegionName("Sutton");
        logger.info("Admin is missing! Creating new admin user with password \"admin\".");
        admin = new User();
        admin.setUsername("admin@admin.com");
        admin.setPassword(userPasswordEncoder.encode("admin"));
        admin.setAdmin(true);
        admin.setRegion(region);
        admin = userRepository.save(admin);
        return admin;
    }

    private Device createDevices() {
        Device device = new Device();
        device.setDeviceName("abc123");
        device.setLatitude((float)51.498484547170605);
        device.setLongitude((float)-0.22865295410156253);
        Region region = regionRepository.findByRegionName("Hammersmith and Fulham");
        device.setRegion(region);
        deviceRepository.save(device);

        Device device2 = new Device();
        device2.setDeviceName("123abc");
        device2.setLatitude((float)54.36775852406841);
        device2.setLongitude((float)-2.0654296875000004);
        Region region2 = regionRepository.findByRegionName("West Yorkshire");
        device2.setRegion(region2);
        deviceRepository.save(device2);

        return device2;
    }

    private Report createReports() {
        Report report1 = new Report();
        report1.setDateTime(LocalDateTime.parse("2019-02-05T08:22:12"));
        report1.setBatteryLevel((float)70);
        report1.setDevice(deviceRepository.findByDeviceName("abc123"));
        reportRepository.save(report1);
        Report report2 = new Report();
        report2.setDateTime(LocalDateTime.parse("2019-02-05T08:22:12"));
        report2.setBatteryLevel((float)69);
        report2.setDevice(deviceRepository.findByDeviceName("123abc"));
        reportRepository.save(report2);
        Report report3 = new Report();
        report3.setDateTime(LocalDateTime.parse("2019-02-05T08:32:12"));
        report3.setBatteryLevel((float)10);
        report3.setDevice(deviceRepository.findByDeviceName("abc123"));
        reportRepository.save(report3);
        Report report4 = new Report();
        report4.setDateTime(LocalDateTime.parse("2019-02-05T08:32:12"));
        report4.setBatteryLevel((float)70);
        report4.setDevice(deviceRepository.findByDeviceName("123abc"));
        reportRepository.save(report4);

        return report1;
    }

    @PostConstruct
    public void init() {
        createRegions();
        checkAdminUser();
        createDevices();
        createReports();
    }
}
