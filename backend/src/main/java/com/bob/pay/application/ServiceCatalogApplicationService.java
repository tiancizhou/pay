package com.bob.pay.application;

import com.bob.pay.domain.model.service.ServiceCatalogRepository;
import com.bob.pay.domain.model.service.ServiceItem;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServiceCatalogApplicationService {

    private final ServiceCatalogRepository serviceCatalogRepository;

    public ServiceCatalogApplicationService(ServiceCatalogRepository serviceCatalogRepository) {
        this.serviceCatalogRepository = serviceCatalogRepository;
    }

    public List<ServiceItem> listServices() {
        return serviceCatalogRepository.findAll();
    }

    public ServiceItem updateDetail(String serviceId, ServiceItem.ServiceDetail detail) {
        var service = serviceCatalogRepository.findById(serviceId)
                .orElseThrow(() -> new IllegalArgumentException("服务不存在"));
        return serviceCatalogRepository.save(new ServiceItem(
                service.id(),
                service.name(),
                service.slogan(),
                service.durationMinutes(),
                service.price(),
                service.originalPrice(),
                service.soldCount(),
                service.imageUrl(),
                detail
        ));
    }
}
