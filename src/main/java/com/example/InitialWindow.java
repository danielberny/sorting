package com.example;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class InitialWindow extends JFrame {

    public InitialWindow() {
        setTitle("");
        setSize(400, 150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(1, 2, 5, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JButton btnFile = new JButton("soubor");
        JButton btnTest = new JButton("nové okno");

        btnFile.addActionListener(e -> {
            dispose(); // zavření okna
            chooseFile();
        });

        btnTest.addActionListener(e -> {
            dispose();
            new TestWindow();
        });

        panel.add(btnFile);
        panel.add(btnTest);
        add(panel);

        setVisible(true);
    }

    private void chooseFile() {
        JFileChooser fileChooser = new JFileChooser();
        int vysledek = fileChooser.showOpenDialog(null);

        if (vysledek == JFileChooser.APPROVE_OPTION) {
            File soubor = fileChooser.getSelectedFile();
            JOptionPane.showMessageDialog(null, "" + soubor.getAbsolutePath());
        } else {
            JOptionPane.showMessageDialog(null, "zrušeno");
        }
    }
}
