package com.company.repository;

import com.company.model.Order;

import java.util.*;
import java.util.stream.Collectors;

public class OrderRepository {

    private static final OrderRepository INSTANCE = new OrderRepository();
    private final Map<Integer, Order> store = new LinkedHashMap<>();

    private OrderRepository() {
    }

    public static OrderRepository getInstance() {
        return INSTANCE;
    }

    public Order save(Order order) {
        store.put(order.getId(), order);
        return order;
    }

    public Optional<Order> findById(int id) {
        return Optional.ofNullable(store.get(id));
    }

    public List<Order> findAll() {
        return new ArrayList<>(store.values());
    }

    public boolean delete(int id) {
        return store.remove(id) != null;
    }

    public List<Order> findByStatus(Order.Status status) {
        return store.values().stream()
                .filter(o -> o.getStatus() == status)
                .collect(Collectors.toList());
    }
}
