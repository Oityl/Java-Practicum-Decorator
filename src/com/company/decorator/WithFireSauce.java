package com.company.decorator;

import com.company.model.Dish;

public class WithFireSauce extends AddonDecorator {
    public WithFireSauce(Dish dish) {
        super(dish);
    }

    @Override
    protected String getAddonName() {
        return "Огненный соус";
    }

    @Override
    protected int getAddonPrice() {
        return 40;
    }
}
