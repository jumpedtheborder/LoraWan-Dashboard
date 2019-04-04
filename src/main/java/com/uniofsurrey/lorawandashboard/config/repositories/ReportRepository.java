package com.uniofsurrey.lorawandashboard.config.repositories;

import com.uniofsurrey.lorawandashboard.config.entities.Device;
import com.uniofsurrey.lorawandashboard.config.entities.Report;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ReportRepository extends CrudRepository<Report, Long> {
    List<Report> findAllByOrderByDateTimeDesc();
    List<Report> findAllByDevice(Device device);
}
