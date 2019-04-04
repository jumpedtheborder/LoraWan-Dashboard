package com.uniofsurrey.lorawandashboard.config.repositories;

import com.uniofsurrey.lorawandashboard.config.entities.Webhook;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface WebhookRepository extends CrudRepository<Webhook, Long> {
    List<Webhook> findByDevId(String id);
    List<Webhook> findByDevIdAndFlagIsFalse(String id);
}
