package com.wharvex.nclos;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.*;

public class OutputHelper {
    private static OutputHelper instance;
    private final Logger debugLogger;
    private final Logger mainOutputLogger;

    private OutputHelper() {
        // Set the loggers and log file paths.
        debugLogger = Logger.getLogger("DebugLog");
        mainOutputLogger = Logger.getLogger("MainOutputLog");
        String debugLogFilePath =
                System.getProperty("java.io.tmpdir") + "nclosDebug.log";
        String mainOutputLogFilePath =
                System.getProperty("java.io.tmpdir") + "nclosMainOutput.log";

        // CONFIGURE THE LOGGERS.

        // Ensure neither logger prints to the console.
        mainOutputLogger.setUseParentHandlers(false);
        debugLogger.setUseParentHandlers(false);

        // Configure the main output logger to print to a separate window.
        var logWindowFrame =
                SwingWindowHelperSingleton.getInstance().createLogWindow();
        var mainOutputLoggerStreamHandler =
                new StreamHandler(new OutputStream() {
                    @Override
                    public void write(int b) {
                        var textArea = logWindowFrame.getTextArea();
                        textArea.append(String.valueOf((char) b));
                        textArea.setCaretPosition(
                                textArea.getDocument().getLength());
                    }
                }, new SimpleFormatter());
        mainOutputLogger.addHandler(mainOutputLoggerStreamHandler);

        // Configure both loggers to print to their respective log files.
        try {
            mainOutputLogger.addHandler(
                    new FileHandler(mainOutputLogFilePath));
            debugLogger.addHandler(new FileHandler(debugLogFilePath));
        } catch (IOException e) {
            System.out.println("Failed to create log files -- exiting");
            System.exit(-1);
        }
    }

    public static OutputHelper getInstance() {
        if (instance == null) {
            instance = new OutputHelper();
        }
        return instance;
    }

    public Logger getDebugLogger() {
        return debugLogger;
    }

    public Logger getMainOutputLogger() {
        return mainOutputLogger;
    }

    public String logToAllAndReturnMessage(String message, Level level) {
        getDebugLogger().log(level, message);
        getMainOutputLogger().log(level, message);
        return message;
    }
}
