package com.uniofsurrey.lorawandashboard.controllers;

import com.uniofsurrey.lorawandashboard.config.AuthenticatedUser;
import com.uniofsurrey.lorawandashboard.entities.*;
import com.uniofsurrey.lorawandashboard.exceptions.BadRequestException;
import com.uniofsurrey.lorawandashboard.models.DeviceDTO;
import com.uniofsurrey.lorawandashboard.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@RestController
public class DeviceController {
    private DeviceRepository deviceRepository;
    private RegionRepository regionRepository;
    private UserRepository userRepository;
    private GroupingRepository groupingRepository;
    private ReportRepository reportRepository;
    private WebhookRepository webhookRepository;

    @Autowired
    public DeviceController(DeviceRepository deviceRepository, RegionRepository regionRepository, UserRepository userRepository, GroupingRepository groupingRepository, ReportRepository reportRepository, WebhookRepository webhookRepository) {
        this.deviceRepository = deviceRepository;
        this.regionRepository = regionRepository;
        this.userRepository = userRepository;
        this.groupingRepository = groupingRepository;
        this.reportRepository = reportRepository;
        this.webhookRepository = webhookRepository;
    }

    @PostMapping("/rest/device")
    public void createDevice(@Valid @RequestBody DeviceDTO newDevice) {
        Device deviceExists = deviceRepository.findByDeviceName(newDevice.getDeviceName());
        Grouping grouping = groupingRepository.findByGroupName(newDevice.getGroup());
        if (deviceExists == null) {

            List<Device> devicesInGroup = deviceRepository.findAllByGrouping(grouping);
            for (Device currentDevice : devicesInGroup) {
                if (currentDevice.getGroupOrder() >= newDevice.getGroupOrder()) {
                    currentDevice.setGroupOrder(currentDevice.getGroupOrder() + 1);
                    deviceRepository.save(currentDevice);
                }
            }

            Device device = new Device();
            device.setDeviceName(newDevice.getDeviceName());
            device.setLatitude(newDevice.getLatitude());
            device.setLongitude(newDevice.getLongitude());
            Region region = regionRepository.findByRegionName(newDevice.getRegion());
            device.setRegion(region);
            device.setGrouping(grouping);
            device.setGroupOrder(newDevice.getGroupOrder());
            deviceRepository.save(device);
        }
        else {
            throw new BadRequestException("Device already exists");
        }
    }

    @GetMapping("/rest/devices")
    public Iterable<Device> getDevice() {
        return deviceRepository.findAll();
    }

    @GetMapping("/rest/groupings")
    public List<String> getGroupings() {
        List<String> groupNames = new ArrayList<>();
        Iterable<Grouping> allGroups = groupingRepository.findAll();
        for (Grouping grouping : allGroups) {
            groupNames.add(grouping.getGroupName());
        }
        return groupNames;
    }

    @GetMapping("/rest/groupDevices/{groupName}")
    public List<Device> getGroupDevices(@PathVariable String groupName) {
        List<Device> groupDevices = new ArrayList<>();
        Grouping grouping = groupingRepository.findByGroupName(groupName);
        groupDevices = deviceRepository.findAllByGrouping(grouping);

        return groupDevices;

    }

    @GetMapping("/rest/getDeviceNames")
    public List<String> getDeviceNames() {
        List<String> deviceNames = new ArrayList<>();
        Iterable<Device> allDevices = deviceRepository.findAll();
        for (Device device : allDevices) {
            deviceNames.add(device.getDeviceName());
        }

        return deviceNames;
    }

    @GetMapping("/rest/devices/region")
    public Iterable<Device> getDeviceRegion() {
        User user = AuthenticatedUser.getPrincipal();
        return deviceRepository.findByRegion(user.getRegion());
    }

    @DeleteMapping("/rest/device/{deviceName}")
    public void deleteDevice(@PathVariable String deviceName) {
        Device device = deviceRepository.findByDeviceName(deviceName);
        List<Report> reports = reportRepository.findAllByDevice(device);
        for (Report report : reports) {
            reportRepository.delete(report);
        }
        List<Webhook> webhooks = webhookRepository.findByDevId(device.getDeviceName());
        for (Webhook webhook : webhooks) {
            webhookRepository.delete(webhook);
        }
        deviceRepository.delete(device);
    }

    @PostMapping("/rest/createGroup")
    public void createGrouping(@Valid @RequestBody Grouping grouping ) {
        Grouping groupingExists = groupingRepository.findByGroupName(grouping.getGroupName());
        if (groupingExists == null) {
            Grouping newGrouping = new Grouping();
            newGrouping.setGroupName(grouping.getGroupName());
            groupingRepository.save(newGrouping);
        }
        else {
            throw new BadRequestException("This group is already in use");
        }
    }
}