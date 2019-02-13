package com.uniofsurrey.lorawandashboard.controllers;

import com.uniofsurrey.lorawandashboard.entities.Report;
import com.uniofsurrey.lorawandashboard.models.ReportDTO;
import com.uniofsurrey.lorawandashboard.repositories.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
