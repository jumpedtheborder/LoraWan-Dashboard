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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.ZonedDateTime;

@Component
public class Init {
    private final UserRepository userRepository;
    private final RegionRepository regionRepository;
    private final DeviceRepository deviceRepository;
    private final ReportRepository reportRepository;
    private final GroupingRepository groupingRepository;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final UserPasswordEncoder userPasswordEncoder = new UserPasswordEncoder();

    @Autowired
    public Init(UserRepository userRepository, RegionRepository regionRepository, DeviceRepository deviceRepository, ReportRepository reportRepository, GroupingRepository groupingRepository) {
        this.userRepository = userRepository;
        this.regionRepository = regionRepository;
        this.deviceRepository = deviceRepository;
        this.reportRepository = reportRepository;
        this.groupingRepository = groupingRepository;
    }


    private void createRegions() {
        if (regionRepository.count() == 0) {
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

    private void createDevices() {
        Device device1Exist = deviceRepository.findByDeviceName("0de5b209945d3b07");
        Device device2Exist = deviceRepository.findByDeviceName("c8d63e89945d3b07");
        //Device device3Exist = deviceRepository.findByDeviceName("testDevice");
        Grouping groupingExist = groupingRepository.findByGroupName("Guildford");

        if(groupingExist == null) {
            Grouping grouping = new Grouping();
            grouping.setGroupName("Guildford");
            groupingRepository.save(grouping);
        }

        if(device1Exist == null) {
            Device device = new Device();
            device.setDeviceName("0de5b209945d3b07");
            device.setLatitude((float) 51.244724592848236);
            device.setLongitude((float) -0.5857086181640626);
            Region region = regionRepository.findByRegionName("Surrey");
            device.setRegion(region);
            Grouping grouping = groupingRepository.findByGroupName("Guildford");
            device.setGrouping(grouping);
            device.setGroupOrder(1);
            deviceRepository.save(device);
        }

        if(device2Exist == null) {
            Device device2 = new Device();
            device2.setDeviceName("c8d63e89945d3b07");
            device2.setLatitude((float) 51.24402610267261);
            device2.setLongitude((float) -0.5847215652465821);
            Region region2 = regionRepository.findByRegionName("Surrey");
            device2.setRegion(region2);
            Grouping grouping = groupingRepository.findByGroupName("Guildford");
            device2.setGrouping(grouping);
            device2.setGroupOrder(2);
            deviceRepository.save(device2);
        }

//        if(device3Exist == null) {
//            Device device3 = new Device();
//            device3.setDeviceName("testDevice");
//            device3.setLatitude((float) 51.34402610267261);
//            device3.setLongitude((float) -0.6847215652465821);
//            Region region3 = regionRepository.findByRegionName("Surrey");
//            device3.setRegion(region3);
//            Grouping grouping = groupingRepository.findByGroupName("Guildford");
//            device3.setGrouping(grouping);
//            device3.setGroupOrder(3);
//            deviceRepository.save(device3);
//        }
    }

    private void createReports() {
        if (reportRepository.count() == 0) {
            Report report1 = new Report();
            report1.setDateTime(ZonedDateTime.parse("2019-02-05T08:22:12.279285Z"));
            report1.setBatteryLevel((float) 70);
            report1.setDevice(deviceRepository.findByDeviceName("0de5b209945d3b07"));
            reportRepository.save(report1);
            Report report2 = new Report();
            report2.setDateTime(ZonedDateTime.parse("2019-02-05T08:22:13.279285Z"));
            report2.setBatteryLevel((float) 69);
            report2.setDevice(deviceRepository.findByDeviceName("c8d63e89945d3b07"));
            reportRepository.save(report2);
            Report report3 = new Report();
            report3.setDateTime(ZonedDateTime.parse("2019-02-05T08:32:12.279285Z"));
            report3.setBatteryLevel((float) 10);
            report3.setDevice(deviceRepository.findByDeviceName("0de5b209945d3b07"));
            reportRepository.save(report3);
            Report report4 = new Report();
            report4.setDateTime(ZonedDateTime.parse("2019-02-05T08:32:14.279285Z"));
            report4.setBatteryLevel((float) 70);
            report4.setDevice(deviceRepository.findByDeviceName("c8d63e89945d3b07"));
            reportRepository.save(report4);
        }
    }

    @PostConstruct
    public void init() {
        createRegions();
        checkAdminUser();
        createDevices();
        createReports();
    }
}
