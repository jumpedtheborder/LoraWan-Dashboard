package com.uniofsurrey.lorawandashboard.repositories;

import com.uniofsurrey.lorawandashboard.entities.Webhook;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface WebhookRepository extends CrudRepository<Webhook, Long> {
    List<Webhook> findByDevId(String id);
    List<Webhook> findByDevIdAndFlagFalseOrderByDateTimeDesc(String id);
}
