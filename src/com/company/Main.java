package com.company;

import com.company.model.*;
import com.company.decorator.*;

import java.io.PrintStream;

public class Main {

    public static void main(String[] args) {
        try {
            System.setOut(new PrintStream(System.out, true, "UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Гарцующая Кобыла, Вайтран\n");

        Drink order1 = new WithCinnamon(new WithSugar(new WithMilk(new Tea())));
        TavernOrder.printOrder(order1);

        Drink order2 = new WithSugar(new WithCream(new Coffee()));
        TavernOrder.printOrder(order2);

        Drink order3 = new Mead();
        TavernOrder.printOrder(order3);

        Drink order4 = new WithCinnamon(new WithMilk(new Coffee()));
        TavernOrder.printOrder(order4);

        try {
            Drink tooMuch = new WithCream(new WithCinnamon(new WithSugar(new WithMilk(new Tea()))));
            TavernOrder.printOrder(tooMuch);
        } catch (IllegalStateException e) {
            System.out.println("Отказ: " + e.getMessage());
        }
    }
}