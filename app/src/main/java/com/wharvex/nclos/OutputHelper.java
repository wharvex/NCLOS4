package com.wharvex.nclos;

import java.io.IOException;
import java.util.function.Supplier;
import java.util.logging.*;

public class OutputHelper {
  private static OutputHelper instance;
  private final Logger debugLogger;
  private final Logger mainOutputLogger;
  private final static String debugLogFilePath =
      System.getProperty("java.io.tmpdir") + "nclosDebug.log";
  private final static String mainOutputLogFilePath =
      System.getProperty("java.io.tmpdir") + "nclosMainOutput.log";

  private OutputHelper() {
    // Get the loggers and ensure neither prints to the console.
    debugLogger = Logger.getLogger("DebugLog");
    mainOutputLogger = Logger.getLogger("MainOutputLog");
    mainOutputLogger.setUseParentHandlers(false);
    debugLogger.setUseParentHandlers(false);

    // Configure the main output logger to print to a separate window.
    var mainOutputLogFrame =
        SwingWindowHelperSingleton.getInstance()
            .createLogWindow("Main Output Log");
    var mainOutputLoggerStreamHandler =
        new StreamHandler(
            new OutputStreamExt(mainOutputLogFrame.getTextArea()),
            new SimpleFormatter());
    mainOutputLogger.addHandler(mainOutputLoggerStreamHandler);

    // Show the log window.
    mainOutputLogFrame.setVisible(true);

    // Configure loggers to print to their respective log files.
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
    logToAll(message, level);
    return message;
  }

  public void logToAll(String message, Level level) {
    getDebugLogger().log(level, message);
    getMainOutputLogger().log(level, message);
  }

  public Supplier<String> getErrorMessageSupplier(String message) {
    return () -> logToAllAndReturnMessage(message, Level.SEVERE);
  }
}
