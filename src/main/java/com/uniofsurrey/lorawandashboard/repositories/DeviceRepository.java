package com.uniofsurrey.lorawandashboard.repositories;

import java.util.List;

import com.uniofsurrey.lorawandashboard.entities.Region;
import org.springframework.data.repository.CrudRepository;
import com.uniofsurrey.lorawandashboard.entities.Device;

public interface DeviceRepository extends CrudRepository<Device, Long> {
    List<Device> findByRegion(Region region);
    Device findByDeviceName(String deviceName);
}
