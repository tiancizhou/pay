package com.bob.pay.application;

import com.bob.pay.domain.model.service.ServiceCatalogRepository;
import com.bob.pay.domain.model.service.ServiceItem;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    public ServiceItem saveService(ServiceItem command) {
        var requestedId = command.id() == null ? "" : command.id().strip();
        var existing = requestedId.isBlank()
                ? Optional.<ServiceItem>empty()
                : serviceCatalogRepository.findById(requestedId);
        var serviceId = existing.map(ServiceItem::id).orElseGet(this::nextServiceId);
        var detail = command.detail() != null
                ? command.detail()
                : existing.map(ServiceItem::detail).orElseGet(ServiceCatalogApplicationService::emptyDetail);
        return serviceCatalogRepository.save(new ServiceItem(
                serviceId,
                command.name(),
                command.slogan(),
                command.durationMinutes(),
                command.price(),
                command.originalPrice(),
                command.soldCount(),
                command.imageUrl(),
                detail
        ));
    }

    private String nextServiceId() {
        String id;
        do {
            id = "service-" + UUID.randomUUID().toString().substring(0, 8);
        } while (serviceCatalogRepository.findById(id).isPresent());
        return id;
    }

    public void deleteService(String serviceId) {
        if (!serviceCatalogRepository.deleteById(serviceId)) {
            throw new IllegalArgumentException("服务不存在");
        }
    }

    private static ServiceItem.ServiceDetail emptyDetail() {
        return new ServiceItem.ServiceDetail(List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
    }
}
