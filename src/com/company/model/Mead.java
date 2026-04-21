package com.company.model;

public class Mead implements Drink {
    @Override
    public String getName() {
        return "Медовуха";
    }

    @Override
    public int getPrice() {
        return 12;
    }

    @Override
    public int getAddonCount() {
        return 0;
    }
}