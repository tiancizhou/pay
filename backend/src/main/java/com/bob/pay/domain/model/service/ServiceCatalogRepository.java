package com.bob.pay.domain.model.service;

import java.util.List;
import java.util.Optional;

public interface ServiceCatalogRepository {

    List<ServiceItem> findAll();

    Optional<ServiceItem> findById(String id);

    ServiceItem save(ServiceItem serviceItem);

    boolean deleteById(String id);
}
