package com.company;

import com.company.model.Drink;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class TavernOrder {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    public static String formatOrder(Drink drink) {
        String time = LocalTime.now().format(FMT);
        return String.format("%s %s — %d золотых", time, drink.getName(), drink.getPrice());
    }

    public static void printOrder(Drink drink) {
        System.out.println(formatOrder(drink));
    }
}