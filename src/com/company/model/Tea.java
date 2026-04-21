package com.company.model;

public class Tea implements Drink {
    @Override
    public String getName() {
        return "Чай";
    }

    @Override
    public int getPrice() {
        return 5;
    }

    @Override
    public int getAddonCount() {
        return 0;
    }
}