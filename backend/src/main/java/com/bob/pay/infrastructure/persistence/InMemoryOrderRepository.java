package com.bob.pay.infrastructure.persistence;

import com.bob.pay.domain.model.order.Order;
import com.bob.pay.domain.model.order.OrderRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryOrderRepository implements OrderRepository {

    private final ConcurrentHashMap<String, Order> orders = new ConcurrentHashMap<>();

    @Override
    public Order save(Order order) {
        orders.put(order.id(), order);
        return order;
    }

    @Override
    public Optional<Order> findById(String id) {
        return Optional.ofNullable(orders.get(id));
    }

    @Override
    public List<Order> findAll() {
        var result = new ArrayList<>(orders.values());
        result.sort(Comparator.comparing(Order::createdAt).reversed());
        return result;
    }
}
