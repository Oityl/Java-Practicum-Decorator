package com.company.decorator;

import com.company.model.Drink;

public class WithCream extends AddonDecorator {
    public WithCream(Drink drink) {
        super(drink);
    }

    @Override
    public String getName() {
        return wrapped.getName() + " со сливками";
    }

    @Override
    public int getPrice() {
        return wrapped.getPrice() + 5;
    }
}