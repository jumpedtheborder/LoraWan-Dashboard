package com.uniofsurrey.lorawandashboard.config.repositories;

import com.uniofsurrey.lorawandashboard.config.entities.Grouping;
import org.springframework.data.repository.CrudRepository;

public interface GroupingRepository extends CrudRepository<Grouping, Long> {
    Grouping findByGroupName(String groupName);
}
