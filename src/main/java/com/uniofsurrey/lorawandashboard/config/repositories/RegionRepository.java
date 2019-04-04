package com.uniofsurrey.lorawandashboard.config.repositories;

import com.uniofsurrey.lorawandashboard.config.entities.Region;
import org.springframework.data.repository.CrudRepository;

public interface RegionRepository extends CrudRepository<Region, Long> {
    Region findByRegionName(String regionName);
}