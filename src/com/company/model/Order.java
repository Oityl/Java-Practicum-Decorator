package com.company.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Order {

    public enum Status {DRAFT, CONFIRMED}

    private static int idSequence = 1;

    private final int id;
    private final int tableNumber;
    private final String guestName;
    private Status status;
    private final List<OrderItem> items;
    private final LocalDateTime createdAt;
    private LocalDateTime confirmedAt;

    public Order(int tableNumber, String guestName) {
        this.id = idSequence++;
        this.tableNumber = tableNumber;
        this.guestName = guestName;
        this.status = Status.DRAFT;
        this.items = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
    }

    public int getId() {
        return id;
    }

    public int getTableNumber() {
        return tableNumber;
    }

    public String getGuestName() {
        return guestName;
    }

    public Status getStatus() {
        return status;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getConfirmedAt() {
        return confirmedAt;
    }

    public void confirm() {
        this.status = Status.CONFIRMED;
        this.confirmedAt = LocalDateTime.now();
    }

    public int getTotalPrice() {
        return items.stream().mapToInt(OrderItem::getTotalPrice).sum();
    }
}
