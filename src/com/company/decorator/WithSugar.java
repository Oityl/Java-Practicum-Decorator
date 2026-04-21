package com.company.decorator;

import com.company.model.Drink;

public class WithSugar extends AddonDecorator {
    public WithSugar(Drink drink) {
        super(drink);
    }

    @Override
    public String getName() {
        return wrapped.getName() + " с сахаром";
    }

    @Override
    public int getPrice() {
        return wrapped.getPrice() + 2;
    }
}