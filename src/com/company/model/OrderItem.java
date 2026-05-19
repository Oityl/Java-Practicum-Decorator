package com.company.model;

import java.util.ArrayList;
import java.util.List;

public class OrderItem {

    private static int idSequence = 1;

    private final int id;
    private Dish dish;
    private int quantity;
    private final List<String> appliedModNames;

    public OrderItem(Dish dish, int quantity) {
        this.id = idSequence++;
        this.dish = dish;
        this.quantity = quantity;
        this.appliedModNames = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public Dish getDish() {
        return dish;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getDishName() {
        return dish.getName();
    }

    public int getUnitPrice() {
        return dish.getPrice();
    }

    public int getTotalPrice() {
        return dish.getPrice() * quantity;
    }

    public int getAddonCount() {
        return dish.getAddonCount();
    }

    public List<String> getAppliedModNames() {
        return appliedModNames;
    }

    public void setDish(Dish dish) {
        this.dish = dish;
        this.appliedModNames.clear();
    }

    public void applyAddon(Dish decorated, String modName) {
        this.dish = decorated;
        this.appliedModNames.add(modName);
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void addModName(String name) {
        this.appliedModNames.add(name);
    }
}