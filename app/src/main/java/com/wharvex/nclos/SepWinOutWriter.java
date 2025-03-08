package com.wharvex.nclos;

import java.awt.BorderLayout;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class SepWinOutWriter {
    private Logger logger;
    private static SepWinOutWriter instance;

    private SepWinOutWriter() {
        JFrame frame = new JFrame("Log Window");
        JTextArea textArea = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(textArea);

        frame.add(scrollPane, BorderLayout.CENTER);
        frame.setSize(400, 400);

        logger = Logger.getLogger("MyLog");
        logger.setUseParentHandlers(false);

        Handler handler = new StreamHandler(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                textArea.append(String.valueOf((char) b));
                textArea.setCaretPosition(textArea.getDocument().getLength());
            }
        }, new SimpleFormatter());

        logger.addHandler(handler);

        frame.setVisible(true);

        logger.info("Application started.");
        logger.warning("Something might be wrong.");
        logger.severe("An error occurred!");
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
