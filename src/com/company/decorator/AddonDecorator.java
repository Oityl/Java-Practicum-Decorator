package com.company.decorator;

import com.company.model.Dish;

public abstract class AddonDecorator implements Dish {

    private static final int MAX_ADDONS = 3;

    private final Dish wrapped;

    protected AddonDecorator(Dish dish) {
        if (dish.getAddonCount() >= MAX_ADDONS) {
            throw new IllegalStateException("Нельзя добавить более " + MAX_ADDONS + " добавок в один заказ!");
        }
        this.wrapped = dish;
    }

    @Override
    public final String getName() {
        return wrapped.getName() + getAddonName();
    }

    @Override
    public final int getPrice() {
        return wrapped.getPrice() + getAddonPrice();
    }

    @Override
    public final int getAddonCount() {
        return wrapped.getAddonCount() + 1;
    }

    protected abstract String getAddonName();
    protected abstract int getAddonPrice();
}