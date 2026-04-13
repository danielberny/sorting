package com.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class InitialWindow extends JFrame {
    private JSpinner spinnerLong;
    private JSpinner spinnerInt;
    private JLabel warning;

    public InitialWindow() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(5, 10, 5, 10));

        JLabel titleLabel = new JLabel("");
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));

        JLabel labelLong = new JLabel("Seed:");
        SpinnerNumberModel model1 = new SpinnerNumberModel(2026L, 0L, Long.MAX_VALUE, 1L);
        spinnerLong = new JSpinner(model1);
        spinnerLong.setEditor(new JSpinner.NumberEditor(spinnerLong, "#"));
        spinnerLong.setPreferredSize(new Dimension(200, 25));

        JLabel labelInt = new JLabel("Počet opakování:");
        SpinnerNumberModel model2 = new SpinnerNumberModel(5, 1, 10, 1);
        spinnerInt = new JSpinner(model2);
        spinnerInt.setEditor(new JSpinner.NumberEditor(spinnerInt, "#"));
        spinnerInt.setPreferredSize(new Dimension(40, 25));

        inputPanel.add(labelLong);
        inputPanel.add(spinnerLong);
        inputPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        inputPanel.add(labelInt);
        inputPanel.add(spinnerInt);

        warning = new JLabel(" ");
        warning.setForeground(Color.RED);
        warning.setAlignmentX(Component.CENTER_ALIGNMENT);

        KeyAdapter keyCheck = new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char znak = e.getKeyChar();
                if (!Character.isDigit(znak)
                        && znak != KeyEvent.VK_BACK_SPACE
                        && znak != KeyEvent.VK_DELETE) {
                    e.consume(); // zahození stisknuté klávesy
                    warning.setText("Pouze číslice!");
                } else {
                    warning.setText(" ");
                }
            }
        };

        JFormattedTextField txtField1 = ((JSpinner.DefaultEditor) spinnerLong.getEditor()).getTextField();
        JFormattedTextField txtField2 = ((JSpinner.DefaultEditor) spinnerInt.getEditor()).getTextField();

        txtField1.addKeyListener(keyCheck);
        txtField2.addKeyListener(keyCheck);
        
        JButton btnApprove = new JButton("Potvrdit");
        btnApprove.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnApprove.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    spinnerLong.commitEdit();
                    spinnerInt.commitEdit();
                } catch (java.text.ParseException ex) {
                    JOptionPane.showMessageDialog(
                            InitialWindow.this,
                            "Neplatný vstup!",
                            "Chyba formátu",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                long hodnota1 = ((Number) spinnerLong.getValue()).longValue();
                int hodnota2 = ((Number) spinnerInt.getValue()).intValue();

                int volba = JOptionPane.showConfirmDialog(
                        InitialWindow.this,
                        "Seed " + hodnota1 + " a počet opakování " + hodnota2 + ". Pokračovat?",
                        "Potvrzení",
                        JOptionPane.YES_NO_OPTION);

                if (volba == JOptionPane.YES_OPTION) {
                    dispose();
                }
            }
        });

        mainPanel.add(titleLabel);
//        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainPanel.add(inputPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(warning);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(btnApprove);

        add(mainPanel);
        pack();
        setLocationRelativeTo(null);
    }

    static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new InitialWindow().setVisible(true);
        });
    }
}