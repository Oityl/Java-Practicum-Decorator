package com.company.decorator;

import com.company.model.Dish;

public class WithSnowBerries extends AddonDecorator {
    public WithSnowBerries(Dish dish) {
        super(dish);
    }

    @Override
    protected String getAddonName() {
        return "Снежные ягоды";
    }

    @Override
    protected int getAddonPrice() {
        return 6;
    }
}
