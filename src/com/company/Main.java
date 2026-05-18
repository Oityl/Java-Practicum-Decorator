package com.company;

import com.company.gui.TavernOrderApp;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TavernOrderApp().setVisible(true));
    }
}