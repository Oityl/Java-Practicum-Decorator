package com.company.model;

import com.company.model.Drink;

import java.util.ArrayList;
import java.util.List;

public class OrderItem {

    private static int idSequence = 1;

    private final int id;
    private Drink drink;
    private int quantity;
    private final List<String> appliedModNames;

    public OrderItem(Drink drink, int quantity) {
        this.id = idSequence++;
        this.drink = drink;
        this.quantity = quantity;
        this.appliedModNames = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public Drink getDrink() {
        return drink;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getDishName() {
        return drink.getName();
    }

    public int getUnitPrice() {
        return drink.getPrice();
    }

    public int getTotalPrice() {
        return drink.getPrice() * quantity;
    }

    public int getAddonCount() {
        return drink.getAddonCount();
    }

    public List<String> getAppliedModNames() {
        return appliedModNames;
    }

    public void setDrink(Drink drink) {
        this.drink = drink;
        this.appliedModNames.clear();
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void addModName(String name) {
        this.appliedModNames.add(name);
    }
}