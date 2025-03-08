package com.wharvex.nclos;

import java.awt.BorderLayout;
import java.io.OutputStream;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Creates a separate Swing window to display log messages.
 */
public class SepWinOutWriter {
    private final Logger logger;
    private static SepWinOutWriter instance;

    private SepWinOutWriter() {
        // Set up the Swing window.
        var frame = new JFrame("Log Window");
        var textArea = new JTextArea();
        var scrollPane = new JScrollPane(textArea);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.setSize(800, 400);

        // Create the logger.
        logger = Logger.getLogger("MyLog");

        // We don't want to print to the console.
        logger.setUseParentHandlers(false);

        var handler = new StreamHandler(new OutputStream() {
            @Override
            public void write(int b) {
                textArea.append(String.valueOf((char) b));
                textArea.setCaretPosition(textArea.getDocument().getLength());
            }
        }, new SimpleFormatter());

        logger.addHandler(handler);
        frame.setVisible(true);
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
