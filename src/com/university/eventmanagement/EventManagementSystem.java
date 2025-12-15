package com.university.eventmanagement;

import javax.swing.*;

public class EventManagementSystem {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}