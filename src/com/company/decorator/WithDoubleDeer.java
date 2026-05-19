package com.company.decorator;

import com.company.model.Dish;

public class WithDoubleDeer extends AddonDecorator {
    public WithDoubleDeer(Dish dish) {
        super(dish);
    }

    @Override
    protected String getAddonName() {
        return "Двойная порция оленины";
    }

    @Override
    protected int getAddonPrice() {
        return 20;
    }
}
