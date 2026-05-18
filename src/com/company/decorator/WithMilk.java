package com.company.decorator;

import com.company.model.Drink;

public class WithMilk extends AddonDecorator {
    public WithMilk(Drink drink) {
        super(drink);
    }

    @Override
    protected String getAddonName() {
        return " с молоком";
    }

    @Override
    protected int getAddonPrice() {
        return 3;
    }
}