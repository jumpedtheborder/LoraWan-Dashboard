package com.uniofsurrey.lorawandashboard.config.controllers;

import com.uniofsurrey.lorawandashboard.config.entities.Region;
import com.uniofsurrey.lorawandashboard.config.repositories.RegionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;


@RestController
public class RegionController {
    private RegionRepository regionRepository;

    @Autowired
    public RegionController(RegionRepository regionRepository) {
        this.regionRepository = regionRepository;
    }

    @GetMapping("/rest/regions")
    public List<String> getRegion() {

        List<String> regionNameList = new ArrayList<>();
        Iterable<Region> regions = regionRepository.findAll();
        for (Region region : regions) {
            String regionName = region.getRegionName();
            regionNameList.add(regionName);
        }
        java.util.Collections.sort(regionNameList);
        return regionNameList;
    }

}
