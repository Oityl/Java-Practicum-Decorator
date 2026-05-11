package com.company.service;

import com.company.decorator.*;
import com.company.model.*;
import com.company.repository.OrderRepository;

import java.util.List;
import java.util.NoSuchElementException;

public class OrderService {

    private static final int MAX_ADDONS = 3;

    private final OrderRepository repo = OrderRepository.getInstance();

    public Order createOrder(int tableNumber, String guestName) {
        if (tableNumber < 1 || tableNumber > 20)
            throw new IllegalArgumentException("Номер стола должен быть от 1 до 20");
        Order order = new Order(tableNumber, guestName);
        return repo.save(order);
    }

    public OrderItem addDish(int orderId, Drink drink, int quantity) {
        Order order = getDraftOrder(orderId);
        if (quantity < 1) throw new IllegalArgumentException("quantity должен быть ≥ 1");
        OrderItem item = new OrderItem(drink, quantity);
        order.getItems().add(item);
        return item;
    }

    public OrderItem addModification(int orderId, int itemId, String modType) {
        Order order = getDraftOrder(orderId);
        OrderItem item = getItem(order, itemId);

        if (item.getAddonCount() >= MAX_ADDONS)
            throw new IllegalStateException("Нельзя добавить более " + MAX_ADDONS + " добавок в одно блюдо");

        Drink decorated = applyDecorator(item.getDrink(), modType);
        item.setDrink(decorated);
        item.addModName(modType);
        return item;
    }

    public OrderItem updateItem(int orderId, int itemId, Drink newDrink, Integer newQuantity, List<String> newMods) {
        Order order = getDraftOrder(orderId);
        OrderItem item = getItem(order, itemId);

        if (newDrink != null) {
            item.setDrink(newDrink);
        }
        if (newQuantity != null) {
            if (newQuantity < 1) throw new IllegalArgumentException("quantity ≥ 1");
            item.setQuantity(newQuantity);
        }
        if (newMods != null) {
            if (newMods.size() > MAX_ADDONS)
                throw new IllegalStateException("Максимум " + MAX_ADDONS + " добавок");

            item.setDrink(item.getDrink());
            for (String mod : newMods) {
                item.setDrink(applyDecorator(item.getDrink(), mod));
                item.addModName(mod);
            }
        }
        return item;
    }

    public Order confirmOrder(int orderId) {
        Order order = getDraftOrder(orderId);
        if (order.getItems().isEmpty())
            throw new IllegalStateException("Нельзя подтвердить пустой заказ");
        order.confirm();
        return order;
    }

    private Order getDraftOrder(int orderId) {
        Order order = repo.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Заказ #" + orderId + " не найден"));
        if (order.getStatus() == Order.Status.CONFIRMED)
            throw new IllegalStateException("Заказ #" + orderId + " уже подтверждён");
        return order;
    }

    private OrderItem getItem(Order order, int itemId) {
        return order.getItems().stream()
                .filter(i -> i.getId() == itemId)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Позиция #" + itemId + " не найдена"));
    }

    private Drink applyDecorator(Drink drink, String type) {
        switch (type.toLowerCase()) {
            case "cinnamon":
            case "корица":
                return new WithCinnamon(drink);
            case "cream":
            case "сливки":
                return new WithCream(drink);
            case "milk":
            case "молоко":
                return new WithMilk(drink);
            case "sugar":
            case "сахар":
                return new WithSugar(drink);
            default:
                throw new IllegalArgumentException("Неизвестная добавка: " + type);
        }
    }
}