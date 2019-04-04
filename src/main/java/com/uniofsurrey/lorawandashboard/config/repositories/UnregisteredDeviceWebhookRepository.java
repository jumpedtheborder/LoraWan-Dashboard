package com.uniofsurrey.lorawandashboard.config.repositories;

import com.uniofsurrey.lorawandashboard.config.entities.UnregisteredDeviceWebhook;
import org.springframework.data.repository.CrudRepository;

public interface UnregisteredDeviceWebhookRepository extends CrudRepository<UnregisteredDeviceWebhook, Long> {
}
