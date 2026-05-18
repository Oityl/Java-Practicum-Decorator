package com.company.decorator;

import com.company.model.Drink;

public class WithCream extends AddonDecorator {
    public WithCream(Drink drink) {
        super(drink);
    }

    @Override
    protected String getAddonName() {
        return " со сливками";
    }

    @Override
    protected int getAddonPrice() {
        return 5;
    }
}