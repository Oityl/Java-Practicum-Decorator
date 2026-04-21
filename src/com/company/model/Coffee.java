package com.company.model;

public class Coffee implements Drink {
    @Override
    public String getName() {
        return "Кофе";
    }

    @Override
    public int getPrice() {
        return 8;
    }

    @Override
    public int getAddonCount() {
        return 0;
    }
}