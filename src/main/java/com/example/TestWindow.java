package com.example;

import javax.swing.*;

public class TestWindow extends JFrame {

    public TestWindow() {
        setTitle("");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel label = new JLabel("", SwingConstants.CENTER);
        add(label);

        setVisible(true);
    }
}
