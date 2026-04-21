package com.company.decorator;

import com.company.model.Drink;

public class WithCinnamon extends AddonDecorator {
    public WithCinnamon(Drink drink) {
        super(drink);
    }

    @Override
    public String getName() {
        return wrapped.getName() + " с корицей";
    }

    @Override
    public int getPrice() {
        return wrapped.getPrice() + 4;
    }
}