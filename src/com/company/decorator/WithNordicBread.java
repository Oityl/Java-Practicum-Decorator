package com.company.decorator;

import com.company.model.Dish;

public class WithNordicBread extends AddonDecorator {
    public WithNordicBread(Dish dish) {
        super(dish);
    }

    @Override
    protected String getAddonName() {
        return "Нордская лепёшка";
    }

    @Override
    protected int getAddonPrice() {
        return 7;
    }
}
