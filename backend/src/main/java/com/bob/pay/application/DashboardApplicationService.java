package com.bob.pay.application;

import com.bob.pay.domain.model.order.OrderRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class DashboardApplicationService {

    private final OrderRepository orderRepository;

    public DashboardApplicationService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public DashboardSnapshot merchantDashboard() {
        var orders = orderRepository.findAll();
        var income = orders.stream()
                .map(order -> order.payableAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new DashboardSnapshot(orders.size(), income, 8, "96%");
    }

    public DashboardSnapshot technicianDashboard() {
        var orders = orderRepository.findAll();
        return new DashboardSnapshot(orders.size(), new BigDecimal("1280.00"), 4, "98%");
    }

    public record DashboardSnapshot(int todayOrders, BigDecimal todayIncome, int pendingTasks, String satisfactionRate) {
    }
}
