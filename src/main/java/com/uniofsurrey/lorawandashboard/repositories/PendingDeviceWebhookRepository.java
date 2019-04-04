package com.uniofsurrey.lorawandashboard.repositories;

import com.uniofsurrey.lorawandashboard.entities.PendingDeviceWebhook;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface PendingDeviceWebhookRepository extends CrudRepository<PendingDeviceWebhook, Long> {
    List<PendingDeviceWebhook> findAllByDevId(String deviceName);
}
