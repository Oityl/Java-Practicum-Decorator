package com.company.decorator;

import com.company.model.Drink;

public class WithCinnamon extends AddonDecorator {
    public WithCinnamon(Drink drink) {
        super(drink);
    }

    @Override
    protected String getAddonName() {
        return " с корицей";
    }

    @Override
    protected int getAddonPrice() {
        return 4;
    }
}