package com.bob.pay.interfaces.web;

import com.bob.pay.application.DashboardApplicationService;
import com.bob.pay.application.OrderApplicationService;
import com.bob.pay.application.ServiceCatalogApplicationService;
import com.bob.pay.application.SiteSettingsApplicationService;
import com.bob.pay.application.TechnicianApplicationService;
import com.bob.pay.application.UploadApplicationService;
import com.bob.pay.application.UserApplicationService;
import com.bob.pay.domain.model.order.GeoPoint;
import com.bob.pay.domain.model.order.Order;
import com.bob.pay.domain.model.service.ServiceItem;
import com.bob.pay.domain.model.technician.Technician;
import com.bob.pay.domain.model.user.AppUser;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ConsoleApiController {

    private final DashboardApplicationService dashboardApplicationService;
    private final OrderApplicationService orderApplicationService;
    private final ServiceCatalogApplicationService serviceCatalogApplicationService;
    private final TechnicianApplicationService technicianApplicationService;
    private final UploadApplicationService uploadApplicationService;
    private final SiteSettingsApplicationService siteSettingsApplicationService;
    private final UserApplicationService userApplicationService;

    public ConsoleApiController(
            DashboardApplicationService dashboardApplicationService,
            OrderApplicationService orderApplicationService,
            ServiceCatalogApplicationService serviceCatalogApplicationService,
            TechnicianApplicationService technicianApplicationService,
            UploadApplicationService uploadApplicationService,
            SiteSettingsApplicationService siteSettingsApplicationService,
            UserApplicationService userApplicationService
    ) {
        this.dashboardApplicationService = dashboardApplicationService;
        this.orderApplicationService = orderApplicationService;
        this.serviceCatalogApplicationService = serviceCatalogApplicationService;
        this.technicianApplicationService = technicianApplicationService;
        this.uploadApplicationService = uploadApplicationService;
        this.siteSettingsApplicationService = siteSettingsApplicationService;
        this.userApplicationService = userApplicationService;
    }

    @GetMapping("/merchant/site-settings")
    public SiteSettingsApplicationService.SiteSettings siteSettings() {
        return siteSettingsApplicationService.getSettings();
    }

    @PostMapping("/merchant/site-settings")
    public SiteSettingsApplicationService.SiteSettings saveSiteSettings(
            @RequestBody SiteSettingsApplicationService.SiteSettings settings
    ) {
        return siteSettingsApplicationService.saveSettings(settings);
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

    @GetMapping("/technicians")
    public List<Technician> technicians() {
        return technicianApplicationService.listAllTechnicians();
    }

    @GetMapping("/users")
    public List<AppUser> users() {
        return userApplicationService.listUsers();
    }

    @PostMapping("/auth/login")
    public AppUser login(@RequestBody UserApplicationService.LoginCommand command) {
        return userApplicationService.login(command);
    }

    @PostMapping("/users")
    public AppUser createUser(@RequestBody UserApplicationService.CreateUserCommand command) {
        return userApplicationService.createUser(command);
    }

    @PostMapping("/users/{userId}/role")
    public AppUser updateUserRole(
            @PathVariable String userId,
            @RequestBody UserApplicationService.UpdateUserRoleCommand command
    ) {
        return userApplicationService.updateRole(userId, command);
    }

    @PostMapping("/users/{userId}/password")
    public AppUser resetPassword(
            @PathVariable String userId,
            @RequestBody UserApplicationService.ResetPasswordCommand command
    ) {
        return userApplicationService.resetPassword(userId, command);
    }

    @GetMapping("/technician/orders/current")
    public Order currentTechnicianOrder() {
        return orderApplicationService.latestOrder();
    }

    @GetMapping("/technician/{technicianId}/orders")
    public List<Order> technicianOrders(@PathVariable String technicianId) {
        return orderApplicationService.listOrdersByTechnician(technicianId);
    }

    @GetMapping("/technician/{technicianId}/orders/current")
    public Order currentTechnicianOrder(@PathVariable String technicianId) {
        return orderApplicationService.latestTechnicianOrder(technicianId);
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

    @PostMapping("/technician/{technicianId}/avatar")
    public Technician updateTechnicianAvatar(
            @PathVariable String technicianId,
            @RequestParam("file") MultipartFile file
    ) {
        var uploaded = uploadApplicationService.saveTechnicianAvatar(file);
        return technicianApplicationService.updatePortrait(technicianId, uploaded.url());
    }

    @PostMapping("/merchant/orders/{orderId}/refund/approve")
    public Order approveRefund(@PathVariable String orderId) {
        return orderApplicationService.approveRefund(orderId);
    }

    @PostMapping("/merchant/services/{serviceId}/detail")
    public ServiceItem updateServiceDetail(@PathVariable String serviceId, @RequestBody ServiceItem.ServiceDetail detail) {
        return serviceCatalogApplicationService.updateDetail(serviceId, detail);
    }

    @PostMapping("/merchant/services")
    public ServiceItem saveService(@RequestBody ServiceItem serviceItem) {
        return serviceCatalogApplicationService.saveService(serviceItem);
    }

    @PostMapping("/merchant/uploads/service-cover")
    public UploadApplicationService.UploadedFile uploadServiceCover(@RequestParam("file") MultipartFile file) {
        return uploadApplicationService.saveServiceCover(file);
    }

    @PostMapping("/merchant/uploads/technician-avatar")
    public UploadApplicationService.UploadedFile uploadTechnicianAvatar(@RequestParam("file") MultipartFile file) {
        return uploadApplicationService.saveTechnicianAvatar(file);
    }

    @PostMapping("/merchant/technicians")
    public Technician saveTechnician(@RequestBody Technician technician) {
        return technicianApplicationService.saveTechnician(technician);
    }

    @DeleteMapping("/merchant/technicians/{technicianId}")
    public void deleteTechnician(@PathVariable String technicianId) {
        technicianApplicationService.deleteTechnician(technicianId);
    }

    @DeleteMapping("/merchant/services/{serviceId}")
    public void deleteService(@PathVariable String serviceId) {
        serviceCatalogApplicationService.deleteService(serviceId);
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<Map<String, String>> handleBusinessException(RuntimeException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", exception.getMessage()));
    }
}
