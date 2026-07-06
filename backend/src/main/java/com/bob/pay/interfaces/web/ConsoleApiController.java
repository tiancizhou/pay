package com.bob.pay.interfaces.web;

import com.bob.pay.application.DashboardApplicationService;
import com.bob.pay.application.OrderApplicationService;
import com.bob.pay.application.ServiceCatalogApplicationService;
import com.bob.pay.application.TechnicianApplicationService;
import com.bob.pay.domain.model.order.GeoPoint;
import com.bob.pay.domain.model.order.Order;
import com.bob.pay.domain.model.service.ServiceItem;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ConsoleApiController {

    private final DashboardApplicationService dashboardApplicationService;
    private final OrderApplicationService orderApplicationService;
    private final ServiceCatalogApplicationService serviceCatalogApplicationService;
    private final TechnicianApplicationService technicianApplicationService;

    public ConsoleApiController(
            DashboardApplicationService dashboardApplicationService,
            OrderApplicationService orderApplicationService,
            ServiceCatalogApplicationService serviceCatalogApplicationService,
            TechnicianApplicationService technicianApplicationService
    ) {
        this.dashboardApplicationService = dashboardApplicationService;
        this.orderApplicationService = orderApplicationService;
        this.serviceCatalogApplicationService = serviceCatalogApplicationService;
        this.technicianApplicationService = technicianApplicationService;
    }

    @GetMapping("/merchant/dashboard")
    public DashboardApplicationService.DashboardSnapshot merchantDashboard() {
        return dashboardApplicationService.merchantDashboard();
    }

    @GetMapping("/technician/dashboard")
    public DashboardApplicationService.DashboardSnapshot technicianDashboard() {
        return dashboardApplicationService.technicianDashboard();
    }

    @GetMapping("/orders")
    public List<Order> orders() {
        return orderApplicationService.listOrders();
    }

    @GetMapping("/technician/orders/current")
    public Order currentTechnicianOrder() {
        return orderApplicationService.latestOrder();
    }

    @PostMapping("/technician/orders/{orderId}/actions/{action}")
    public Order technicianAction(
            @PathVariable String orderId,
            @PathVariable OrderApplicationService.TechnicianOrderAction action
    ) {
        return orderApplicationService.technicianAction(orderId, action);
    }

    @PostMapping("/technician/orders/{orderId}/location")
    public Order updateTechnicianLocation(@PathVariable String orderId, @RequestBody GeoPoint location) {
        return orderApplicationService.updateTechnicianLocation(orderId, location);
    }

    @PostMapping("/technician/{technicianId}/location")
    public Object updateTechnicianBaseLocation(@PathVariable String technicianId, @RequestBody GeoPoint location) {
        return technicianApplicationService.updateLocation(technicianId, location);
    }

    @PostMapping("/merchant/orders/{orderId}/refund/approve")
    public Order approveRefund(@PathVariable String orderId) {
        return orderApplicationService.approveRefund(orderId);
    }

    @PostMapping("/merchant/services/{serviceId}/detail")
    public ServiceItem updateServiceDetail(@PathVariable String serviceId, @RequestBody ServiceItem.ServiceDetail detail) {
        return serviceCatalogApplicationService.updateDetail(serviceId, detail);
    }
}
