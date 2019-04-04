package com.uniofsurrey.lorawandashboard.config.controllers;

import com.uniofsurrey.lorawandashboard.config.entities.Report;
import com.uniofsurrey.lorawandashboard.config.models.ReportDTO;
import com.uniofsurrey.lorawandashboard.config.repositories.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@RestController
public class ReportController {
    private ReportRepository reportRepository;

    @Autowired
    private ReportController(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    @GetMapping("/rest/topBatteryReports")
    public List<ReportDTO> getAllReports() {
        List<Report> allReports = reportRepository.findAllByOrderByDateTimeDesc();
        List<ReportDTO> mostRecentReports = new ArrayList<>();
        for (int i = 0; i < allReports.size(); i++) {
            Report report = allReports.get(i);
            ReportDTO reportDTO = new ReportDTO();
            reportDTO.setBatteryLevel(report.getBatteryLevel());
            reportDTO.setDeviceName(report.getDevice().getDeviceName());
            if(mostRecentReports.size() == 0) {
                mostRecentReports.add(reportDTO);
            }
            boolean doesExist = false;
            for (int j = 0; j < mostRecentReports.size(); j++) {

                ReportDTO reportDTOAdded = mostRecentReports.get(j);
                if (reportDTOAdded.getDeviceName() == reportDTO.getDeviceName()) {
                    doesExist = true;
                }
            }
            if (doesExist == false) {
                mostRecentReports.add(reportDTO);
            }
        }

        return mostRecentReports;
    }

    @GetMapping("/rest/problemDevices")
    public List<ReportDTO> getProblemDeviceReports() {
        List<Report> allReports = reportRepository.findAllByOrderByDateTimeDesc();
        List<ReportDTO> problemDeviceReports = new ArrayList<>();
        for (int i = 0; i < allReports.size(); i++) {
            Report report = allReports.get(i);
            long timeDifference = ChronoUnit.MINUTES.between(report.getDateTime(), ZonedDateTime.now());
            if (report.getBatteryLevel() < 30 || timeDifference > 60) {
                ReportDTO reportDTO = new ReportDTO();
                reportDTO.setId(report.getDevice().getId());
                reportDTO.setBatteryLevel(report.getBatteryLevel());
                reportDTO.setDeviceName(report.getDevice().getDeviceName());
                reportDTO.setLatitude(report.getDevice().getLatitude());
                reportDTO.setLongitude(report.getDevice().getLongitude());
                if (problemDeviceReports.size() == 0) {
                    problemDeviceReports.add(reportDTO);
                }
                boolean doesExist = false;
                for (int j = 0; j < problemDeviceReports.size(); j++) {

                    ReportDTO reportDTOAdded = problemDeviceReports.get(j);
                    if (reportDTOAdded.getDeviceName() == reportDTO.getDeviceName()) {
                        doesExist = true;
                    }
                }
                if (doesExist == false) {
                    problemDeviceReports.add(reportDTO);
                }
            }
        }

        return problemDeviceReports;
    }
}
