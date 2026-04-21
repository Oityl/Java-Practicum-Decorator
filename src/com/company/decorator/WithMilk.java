package com.company.decorator;

import com.company.model.Drink;

public class WithMilk extends AddonDecorator {
    public WithMilk(Drink drink) {
        super(drink);
    }

    @Override
    public String getName() {
        return wrapped.getName() + " с молоком";
    }

    @Override
    public int getPrice() {
        return wrapped.getPrice() + 3;
    }
}