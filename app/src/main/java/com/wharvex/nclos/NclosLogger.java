package com.wharvex.nclos;

import java.text.MessageFormat;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.stream.Stream;

public class NclosLogger {
  private static final String messageBase =
      "THREAD:{0};PREV_FRAME.CLASS:{1};PREV_FRAME.METHOD:{2};";

  private static LogRecord getLogRecord(Level level, String message,
                                        Object... params) {
    // Create the record, get the frames.
    var ret = new LogRecord(level, messageBase + message);
    var frames = new TwoStackFrameWrappers(4, 5);

    // Set the record's stack "source" with data from the first frame.
    ret.setSourceClassName(frames.getFirstClassName());
    ret.setSourceMethodName(frames.getFirstMethodName());

    // The first three parameters to every log message.
    var baseParams = new Object[]{Thread.currentThread().getName(),
        frames.getSecondClassName(),
        frames.getSecondMethodName()};

    // Merge the parameters.
    Object[] mergedParams =
        Stream.of(baseParams, params).flatMap(Stream::of).toArray();

    // Set the merged parameters and return.
    ret.setParameters(mergedParams);
    return ret;
  }

  public static void logDebug(String message, Object... params) {
    OutputHelper.getInstance().getDebugLogger()
        .log(getLogRecord(Level.INFO, message, params));
  }

  public static void logDebug() {
    OutputHelper.getInstance().getDebugLogger()
        .log(getLogRecord(Level.INFO, ""));
  }

  public static void logMain(String message, Object... params) {
    OutputHelper.getInstance().getMainOutputLogger()
        .log(getLogRecord(Level.INFO, message, params));
    OutputHelper.getInstance().getDebugLogger()
        .log(getLogRecord(Level.INFO, message, params));
  }

  public static void logDebugSync(ExecutionPathStage stage) {
    OutputHelper.getInstance().getDebugLogger().log(getLogRecord(Level.INFO,
        "SYNC_STAGE:{3};", stage.toString().toLowerCase()));
  }

  public static void logDebugThread(ThreadLifeStage stage) {
    OutputHelper.getInstance().getDebugLogger().log(getLogRecord(Level.INFO,
        "THREAD_STAGE:{3};", stage.toString().toLowerCase()));
  }

  public static void logDebugThread(ThreadLifeStage stage,
                                    String targetThreadName) {
    OutputHelper.getInstance().getDebugLogger().log(getLogRecord(Level.INFO,
        "{3}:{4};", stage.toString(), targetThreadName));
  }

  public static Supplier<String> logError(String message, Object... params) {
    return () -> {
      var record = getLogRecord(Level.SEVERE, message, params);
      OutputHelper.getInstance().getDebugLogger().log(record);
      OutputHelper.getInstance().getMainOutputLogger().log(record);
      return MessageFormat.format(record.getMessage(),
          record.getParameters());
    };
  }
}
