package com.company;

import com.company.api.RestServer;
import com.company.gui.GuiClient;

import java.io.PrintStream;

public class Main {
    public static void main(String[] args) {
        try {
            System.setOut(new PrintStream(System.out, true, "UTF-8"));
            System.setErr(new PrintStream(System.err, true, "UTF-8"));
        } catch (Exception ignored) {
        }

        try {
            RestServer.start(8080);
        } catch (Exception e) {
            System.err.println("Не удалось запустить сервер: " + e.getMessage());
            return;
        }
        GuiClient.launch();
    }
}