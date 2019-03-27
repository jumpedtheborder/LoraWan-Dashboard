package com.uniofsurrey.lorawandashboard.repositories;

import java.util.List;

import com.uniofsurrey.lorawandashboard.entities.Device;
import com.uniofsurrey.lorawandashboard.entities.Report;
import org.springframework.data.repository.CrudRepository;

public interface ReportRepository extends CrudRepository<Report, Long> {
    List<Report> findAllByOrderByDateTimeDesc();
    List<Report> findAllByDevice(Device device);
    Report findFirstByDeviceOrderByDateTimeDesc(Device device);
}
