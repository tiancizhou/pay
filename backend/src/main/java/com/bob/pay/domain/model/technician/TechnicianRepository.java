package com.bob.pay.domain.model.technician;

import java.util.List;
import java.util.Optional;

public interface TechnicianRepository {

    List<Technician> findAvailableByService(String serviceId);

    Optional<Technician> findById(String id);

    Technician save(Technician technician);
}
