package com.uniofsurrey.lorawandashboard.repositories;

import com.uniofsurrey.lorawandashboard.entities.Device;
import com.uniofsurrey.lorawandashboard.entities.Grouping;
import org.springframework.data.repository.CrudRepository;

public interface GroupingRepository extends CrudRepository<Grouping, Long> {
    Grouping findByGroupName(String groupName);
}
