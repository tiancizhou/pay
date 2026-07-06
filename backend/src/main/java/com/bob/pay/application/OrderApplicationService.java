package com.bob.pay.application;

import com.bob.pay.domain.model.order.Address;
import com.bob.pay.domain.model.order.GeoPoint;
import com.bob.pay.domain.model.order.Order;
import com.bob.pay.domain.model.order.OrderRepository;
import com.bob.pay.domain.model.order.OrderStatus;
import com.bob.pay.domain.model.service.ServiceCatalogRepository;
import com.bob.pay.domain.model.technician.TechnicianRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class OrderApplicationService {

    private static final BigDecimal TRAVEL_FEE = new BigDecimal("4.00");
    private static final BigDecimal TRAVEL_FEE_PER_KM = new BigDecimal("4.00");
    private static final String DEFAULT_MERCHANT_ID = "merchant-yiwu-001";
    private static final String DEFAULT_MERCHANT_ACCOUNT_ID = "mch_settlement_6222";

    private final ServiceCatalogRepository serviceCatalogRepository;
    private final OrderRepository orderRepository;
    private final TechnicianRepository technicianRepository;

    public OrderApplicationService(
            ServiceCatalogRepository serviceCatalogRepository,
            OrderRepository orderRepository,
            TechnicianRepository technicianRepository
    ) {
        this.serviceCatalogRepository = serviceCatalogRepository;
        this.orderRepository = orderRepository;
        this.technicianRepository = technicianRepository;
    }

    public Order createOrder(CreateOrderCommand command) {
        var service = serviceCatalogRepository.findById(command.serviceId())
                .orElseThrow(() -> new IllegalArgumentException("服务不存在"));
        var technician = technicianRepository.findById(command.technicianId())
                .orElseThrow(() -> new IllegalArgumentException("技师不存在"));
        var distanceKm = TechnicianApplicationService.distanceKm(command.customerLocation(), technician.location());
        var travelFee = calculateTravelFee(distanceKm);
        var order = new Order(
                UUID.randomUUID().toString(),
                DEFAULT_MERCHANT_ID,
                DEFAULT_MERCHANT_ACCOUNT_ID,
                command.serviceId(),
                command.technicianId(),
                command.address(),
                command.customerLocation(),
                technician.location(),
                command.serviceTime(),
                travelFee,
                service.price().add(travelFee),
                OrderStatus.WAITING_PAYMENT,
                List.of(new Order.OrderTimelineItem(
                        OrderStatus.WAITING_PAYMENT,
                        "订单已创建，等待模拟支付",
                        LocalDateTime.now()
                )),
                LocalDateTime.now()
        );
        return orderRepository.save(order);
    }

    public List<Order> listOrders() {
        return orderRepository.findAll();
    }

    public Order latestOrder() {
        return orderRepository.findAll().stream().findFirst()
                .orElseGet(this::demoOrder);
    }

    public Order technicianAction(String orderId, TechnicianOrderAction action) {
        var order = orderRepository.findById(orderId).orElseGet(this::demoOrder);
        var next = switch (action) {
            case ACCEPT -> order.moveTo(OrderStatus.TECHNICIAN_ACCEPTED, "技师已接单");
            case CONTACT_CUSTOMER -> order.moveTo(OrderStatus.CONTACTED_CUSTOMER, "技师已与客户电话确认");
            case DEPART -> order.moveTo(OrderStatus.ON_THE_WAY, "技师已出发上门");
            case ARRIVE -> order.moveTo(OrderStatus.ARRIVED, "技师已到达服务地址");
            case CLOCK_IN -> order.moveTo(OrderStatus.CLOCKED_IN, "技师开始上钟，服务计时开始");
            case CLOCK_OUT -> clockOut(order);
            case FINISH -> order.moveTo(OrderStatus.FINISHED, "订单已完成");
        };
        return orderRepository.save(next);
    }

    private Order clockOut(Order order) {
        var service = serviceCatalogRepository.findById(order.serviceId())
                .orElseThrow(() -> new IllegalArgumentException("服务不存在"));
        var clockInTime = order.timeline().stream()
                .filter(item -> item.status() == OrderStatus.CLOCKED_IN)
                .reduce((first, second) -> second)
                .map(Order.OrderTimelineItem::happenedAt)
                .orElseThrow(() -> new IllegalStateException("请先上钟"));
        var expectedClockOutTime = clockInTime.plusMinutes(service.durationMinutes());
        if (LocalDateTime.now().isBefore(expectedClockOutTime)) {
            throw new IllegalStateException("服务时长未满，预计下钟时间：" + expectedClockOutTime);
        }
        return order.moveTo(OrderStatus.CLOCKED_OUT, "技师完成下钟，实际服务时长已满足" + service.durationMinutes() + "分钟");
    }

    public Order pay(String orderId) {
        var order = orderRepository.findById(orderId).orElseGet(this::demoOrder);
        return orderRepository.save(order.moveTo(
                OrderStatus.PAID_TO_MERCHANT,
                "模拟支付成功，资金进入商户结算账户"
        ).moveTo(OrderStatus.WAITING_TECHNICIAN_ACCEPT, "等待技师接单"));
    }

    public Order cancel(String orderId) {
        var order = orderRepository.findById(orderId).orElseGet(this::demoOrder);
        return orderRepository.save(order.moveTo(OrderStatus.CANCELLED, "用户取消订单"));
    }

    public Order requestRefund(String orderId) {
        var order = orderRepository.findById(orderId).orElseGet(this::demoOrder);
        return orderRepository.save(order.moveTo(OrderStatus.REFUND_REQUESTED, "用户发起退款申请"));
    }

    public Order approveRefund(String orderId) {
        var order = orderRepository.findById(orderId).orElseGet(this::demoOrder);
        return orderRepository.save(order.moveTo(OrderStatus.REFUNDED, "商户已模拟退款"));
    }

    public Order updateCustomerLocation(String orderId, GeoPoint location) {
        var order = orderRepository.findById(orderId).orElseGet(this::demoOrder);
        return orderRepository.save(order.updateCustomerLocation(location));
    }

    public Order updateTechnicianLocation(String orderId, GeoPoint location) {
        var order = orderRepository.findById(orderId).orElseGet(this::demoOrder);
        return orderRepository.save(order.updateTechnicianLocation(location));
    }

    private Order demoOrder() {
        var service = serviceCatalogRepository.findById("tuina").orElseThrow();
        var order = new Order(
                "demo-order",
                DEFAULT_MERCHANT_ID,
                DEFAULT_MERCHANT_ACCOUNT_ID,
                service.id(),
                "sha",
                new Address("方已儿", "15058666378", "浙江省金华市义乌市世俊路", "物资市场1210", true),
                new GeoPoint(29.3069, 120.0751, "用户定位"),
                new GeoPoint(29.3103, 120.0801, "技师定位"),
                "今天 23:00",
                TRAVEL_FEE,
                service.price().add(TRAVEL_FEE),
                OrderStatus.WAITING_TECHNICIAN_ACCEPT,
                List.of(new Order.OrderTimelineItem(
                        OrderStatus.PAID_TO_MERCHANT,
                        "用户支付成功，资金进入商户结算账户",
                        LocalDateTime.now().minusMinutes(8)
                )),
                LocalDateTime.now().minusMinutes(10)
        );
        return orderRepository.save(order);
    }

    private BigDecimal calculateTravelFee(double distanceKm) {
        if (distanceKm <= 1) {
            return BigDecimal.ZERO;
        }
        var chargeableKm = Math.ceil(distanceKm - 1);
        return TRAVEL_FEE_PER_KM.multiply(BigDecimal.valueOf(chargeableKm));
    }

    public record CreateOrderCommand(
            @NotBlank String serviceId,
            @NotBlank String technicianId,
            @Valid Address address,
            GeoPoint customerLocation,
            @NotBlank String serviceTime
    ) {
    }

    public enum TechnicianOrderAction {
        ACCEPT,
        CONTACT_CUSTOMER,
        DEPART,
        ARRIVE,
        CLOCK_IN,
        CLOCK_OUT,
        FINISH
    }
}
