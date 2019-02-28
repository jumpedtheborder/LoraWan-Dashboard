package com.uniofsurrey.lorawandashboard.repositories;

import com.uniofsurrey.lorawandashboard.entities.Device;
import com.uniofsurrey.lorawandashboard.entities.Pairing;
import org.springframework.data.repository.CrudRepository;

public interface PairingRepository extends CrudRepository<Pairing, Long> {
    Pairing findByDevice1OrDevice2(Device device1, Device device2);
}
