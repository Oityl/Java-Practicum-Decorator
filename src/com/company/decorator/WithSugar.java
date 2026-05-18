package com.company.decorator;

import com.company.model.Drink;

public class WithSugar extends AddonDecorator {
    public WithSugar(Drink drink) {
        super(drink);
    }

    @Override
    protected String getAddonName() {
        return " с сахаром";
    }

    @Override
    protected int getAddonPrice() {
        return 2;
    }
}