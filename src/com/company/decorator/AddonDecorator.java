package com.company.decorator;

import com.company.model.Drink;

public abstract class AddonDecorator implements Drink {

    private static final int MAX_ADDONS = 3;

    private final Drink wrapped;

    protected AddonDecorator(Drink drink) {
        if (drink.getAddonCount() >= MAX_ADDONS) {
            throw new IllegalStateException("Нельзя добавить более " + MAX_ADDONS + " добавок в один заказ!");
        }
        this.wrapped = drink;
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