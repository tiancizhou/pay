package com.bob.pay.interfaces.web;

import com.bob.pay.application.OrderApplicationService;
import com.bob.pay.application.ServiceCatalogApplicationService;
import com.bob.pay.application.TechnicianApplicationService;
import com.bob.pay.domain.model.order.GeoPoint;
import com.bob.pay.domain.model.order.Order;
import com.bob.pay.domain.model.service.ServiceItem;
import com.bob.pay.domain.model.technician.Technician;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/client")
public class ClientApiController {

    private final ServiceCatalogApplicationService serviceCatalogApplicationService;
    private final TechnicianApplicationService technicianApplicationService;
    private final OrderApplicationService orderApplicationService;

    public ClientApiController(
            ServiceCatalogApplicationService serviceCatalogApplicationService,
            TechnicianApplicationService technicianApplicationService,
            OrderApplicationService orderApplicationService
    ) {
        this.serviceCatalogApplicationService = serviceCatalogApplicationService;
        this.technicianApplicationService = technicianApplicationService;
        this.orderApplicationService = orderApplicationService;
    }

    @GetMapping("/services")
    public List<ServiceItem> services() {
        return serviceCatalogApplicationService.listServices();
    }

    @GetMapping("/services/{serviceId}/technicians")
    public List<Technician> technicians(
            @PathVariable String serviceId,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude
    ) {
        return technicianApplicationService.listTechnicians(serviceId, latitude, longitude);
    }

    @PostMapping("/orders")
    public Order createOrder(@Valid @RequestBody OrderApplicationService.CreateOrderCommand command) {
        return orderApplicationService.createOrder(command);
    }

    @GetMapping("/orders/latest")
    public Order latestOrder() {
        return orderApplicationService.latestOrder();
    }

    @PostMapping("/orders/{orderId}/customer-location")
    public Order updateCustomerLocation(@PathVariable String orderId, @RequestBody GeoPoint location) {
        return orderApplicationService.updateCustomerLocation(orderId, location);
    }

    @PostMapping("/orders/{orderId}/pay")
    public Order pay(@PathVariable String orderId) {
        return orderApplicationService.pay(orderId);
    }

    @PostMapping("/orders/{orderId}/cancel")
    public Order cancel(@PathVariable String orderId) {
        return orderApplicationService.cancel(orderId);
    }

    @PostMapping("/orders/{orderId}/refund")
    public Order requestRefund(@PathVariable String orderId) {
        return orderApplicationService.requestRefund(orderId);
    }
}
