package com.uniofsurrey.lorawandashboard.controllers;

import com.uniofsurrey.lorawandashboard.entities.Region;
import com.uniofsurrey.lorawandashboard.repositories.RegionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
