package com.uniofsurrey.lorawandashboard.controllers;

import com.uniofsurrey.lorawandashboard.config.AuthenticatedUser;
import com.uniofsurrey.lorawandashboard.entities.Device;
import com.uniofsurrey.lorawandashboard.entities.Region;
import com.uniofsurrey.lorawandashboard.entities.User;
import com.uniofsurrey.lorawandashboard.models.DeviceDTO;
import com.uniofsurrey.lorawandashboard.repositories.DeviceRepository;
import com.uniofsurrey.lorawandashboard.repositories.RegionRepository;
import com.uniofsurrey.lorawandashboard.repositories.UserRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.Valid;
import java.lang.Iterable;
import java.lang.RuntimeException;

@RestController
public class DeviceController {
    private DeviceRepository deviceRepository;
    private RegionRepository regionRepository;
    private UserRepository userRepository;

    @Autowired
    public DeviceController(DeviceRepository deviceRepository, RegionRepository regionRepository, UserRepository userRepository) {
        this.deviceRepository = deviceRepository;
        this.regionRepository = regionRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/rest/device")
    public Device createDevice(@Valid @RequestBody DeviceDTO newDevice) {
        Device device = new Device();
        device.setDeviceName(newDevice.getDeviceName());
        device.setLatitude(newDevice.getLatitude());
        device.setLongitude(newDevice.getLongitude());
        Region region = regionRepository.findByRegionName(newDevice.getRegion());
        device.setRegion(region);
        return deviceRepository.save(device);
    }

    @GetMapping("/rest/devices")
    public Iterable<Device> getDevice() {
        return deviceRepository.findAll();
    }

    @GetMapping("/rest/devices/region")
    public Iterable<Device> getDeviceRegion() {
        User user = AuthenticatedUser.getPrincipal();
        return deviceRepository.findByRegion(user.getRegion());
    }
}