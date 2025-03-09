package com.wharvex.nclos;

import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import java.awt.*;

public class SwingWindowHelperSingleton {
    private static SwingWindowHelperSingleton instance;

    private SwingWindowHelperSingleton() {
        FlatDarkLaf.setup();
        UIManager.put("TitlePane.foreground", Color.RED);
        UIManager.put("TitlePane.font", new Font("Times New Roman", Font.BOLD, 12));
    }

    public static SwingWindowHelperSingleton getInstance() {
        if (instance == null) {
            instance = new SwingWindowHelperSingleton();
        }
        return instance;
    }

    public JFrameExt createLogWindow() {
        var frame = new JFrameExt("Log Window");
        var textArea = new JTextArea();

        textArea.setFont(new Font("Courier New", Font.PLAIN, 16));
        textArea.setBackground(new Color(0x141414));
        textArea.setForeground(Color.LIGHT_GRAY);

        frame.add(new JScrollPane(textArea), BorderLayout.CENTER);
        frame.setSize(400, 400);
        frame.setTextArea(textArea);

        return frame;
    }
}
