package com.uniofsurrey.lorawandashboard.repositories;

import com.uniofsurrey.lorawandashboard.entities.Device;
import com.uniofsurrey.lorawandashboard.entities.Grouping;
import com.uniofsurrey.lorawandashboard.entities.Region;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface DeviceRepository extends CrudRepository<Device, Long> {
    List<Device> findByRegion(Region region);
    Device findByDeviceName(String deviceName);
    Device findByid(String id);
    List<Device> findAllByGrouping(Grouping grouping);
}
