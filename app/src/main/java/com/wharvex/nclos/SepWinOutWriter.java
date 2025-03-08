package com.wharvex.nclos;

import com.formdev.flatlaf.FlatDarkLaf;

import java.awt.*;
import java.io.OutputStream;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import javax.swing.*;

/**
 * Creates a separate Swing window to display log messages.
 */
public class SepWinOutWriter {
    private final Logger logger;
    private static SepWinOutWriter instance;

    private SepWinOutWriter() {
        // Set up the FlatLaf look and feel.
        FlatDarkLaf.setup();
//        UIManager.put("TitlePane.background", Color.BLACK);
        UIManager.put("TitlePane.foreground", Color.RED);

        logger = Logger.getLogger("MyLog");

//        SwingUtilities.invokeLater(() -> {
        // Set up the Swing window.
        var frame = new JFrame("Log Window");
        var textArea = new JTextArea();
        Font newFont = new Font("Courier New", Font.PLAIN, 16);
        textArea.setFont(newFont);
        textArea.setBackground(new Color(0x141414));
        textArea.setForeground(Color.LIGHT_GRAY);
        var scrollPane = new JScrollPane(textArea);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.setSize(1111, 400);

        // Set up the logger.
        logger.setUseParentHandlers(false); // Don't print to console.
        var handler = new StreamHandler(new OutputStream() {
            @Override
            public void write(int b) {
                // Use the Swing window's text area to display log messages.
                textArea.append(String.valueOf((char) b));
                textArea.setCaretPosition(textArea.getDocument().getLength());
            }
        }, new SimpleFormatter());
        logger.addHandler(handler);

        // Display the Swing window.
        frame.setVisible(true);
//        });
    }

    public static SepWinOutWriter getInstance() {
        if (instance == null) {
            instance = new SepWinOutWriter();
        }
        return instance;
    }

    public Logger getLogger() {
        return logger;
    }
}
