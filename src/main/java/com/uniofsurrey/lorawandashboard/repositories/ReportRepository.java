package com.uniofsurrey.lorawandashboard.repositories;

import com.uniofsurrey.lorawandashboard.entities.Device;
import com.uniofsurrey.lorawandashboard.entities.Report;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ReportRepository extends CrudRepository<Report, Long> {
    List<Report> findAllByOrderByDateTimeDesc();
    List<Report> findAllByDevice(Device device);
}
