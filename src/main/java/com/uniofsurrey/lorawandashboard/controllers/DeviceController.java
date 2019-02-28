package com.uniofsurrey.lorawandashboard.controllers;

import com.uniofsurrey.lorawandashboard.config.AuthenticatedUser;
import com.uniofsurrey.lorawandashboard.entities.*;
import com.uniofsurrey.lorawandashboard.models.DeviceDTO;
import com.uniofsurrey.lorawandashboard.repositories.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.Valid;
import java.lang.Iterable;
import java.util.ArrayList;
import java.util.List;

@RestController
public class DeviceController {
    private DeviceRepository deviceRepository;
    private RegionRepository regionRepository;
    private UserRepository userRepository;
    private PairingRepository pairingRepository;
    private ReportRepository reportRepository;
    private WebhookRepository webhookRepository;

    @Autowired
    public DeviceController(DeviceRepository deviceRepository, RegionRepository regionRepository, UserRepository userRepository, PairingRepository pairingRepository, ReportRepository reportRepository, WebhookRepository webhookRepository) {
        this.deviceRepository = deviceRepository;
        this.regionRepository = regionRepository;
        this.userRepository = userRepository;
        this.pairingRepository = pairingRepository;
        this.reportRepository = reportRepository;
        this.webhookRepository = webhookRepository;
    }

    @PostMapping("/rest/device")
    public void createDevice(@Valid @RequestBody DeviceDTO newDevice) {
        Device device = new Device();
        device.setDeviceName(newDevice.getDeviceName());
        device.setLatitude(newDevice.getLatitude());
        device.setLongitude(newDevice.getLongitude());
        Region region = regionRepository.findByRegionName(newDevice.getRegion());
        device.setRegion(region);
        deviceRepository.save(device);

        if (newDevice.getPairedDevice() != "") {
            Device pairedDevice = deviceRepository.findByDeviceName(newDevice.getPairedDevice());
            Pairing pairing = new Pairing();
            pairing.setDevice1(device);
            pairing.setDevice2(pairedDevice);
            pairingRepository.save(pairing);
        }
    }

    @GetMapping("/rest/nonPairedDevices")
    public List<Device> getAllUnpairedDevices() {
        List<Device> unpairedDevices = new ArrayList<>();
        Iterable<Device> allDevices = deviceRepository.findAll();
        for (Device device : allDevices) {
            Pairing pairing = pairingRepository.findByDevice1OrDevice2(device, device);
            if (pairing == null) {
                unpairedDevices.add(device);
            }
        }
        return unpairedDevices;
    }

    @GetMapping("/rest/devices")
    public Iterable<Device> getDevice() {
        return deviceRepository.findAll();
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
        Pairing pairing = pairingRepository.findByDevice1OrDevice2(device, device);
        if (pairing != null) {
            pairingRepository.delete(pairing);
        }

        deviceRepository.delete(device);
    }
}