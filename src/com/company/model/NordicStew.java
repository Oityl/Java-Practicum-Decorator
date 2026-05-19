package com.company.model;

public class NordicStew implements Dish {
    @Override
    public String getName() {
        return "Нордское рагу";
    }

    @Override
    public int getPrice() {
        return 50;
    }

    @Override
    public int getAddonCount() {
        return 0;
    }
}
