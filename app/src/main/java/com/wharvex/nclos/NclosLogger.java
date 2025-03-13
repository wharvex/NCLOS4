package com.wharvex.nclos;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.stream.Stream;

public class NclosLogger {
  private static final String messageBase =
      "THREAD:{0};PREV_FRAME.CLASS:{1};PREV_FRAME.METHOD:{2};";

  private static LogRecord getLogRecord() {
    var logRecord = new LogRecord(Level.INFO, messageBase);
    var thisFrame = new StackFrameWrapper(4);
    var prevFrame = new StackFrameWrapper(5);
    logRecord.setSourceClassName(thisFrame.getClassName());
    logRecord.setSourceMethodName(thisFrame.getMethodName());
    logRecord.setParameters(new Object[]{Thread.currentThread().getName(),
        prevFrame.getClassName(), prevFrame.getMethodName()});
    return logRecord;
  }

  private static LogRecord getLogRecord(LogRecord baseRecord, String message,
                                        Object... params) {
    // Concatenate the parameters.
    Object[] merged =
        Stream.of(baseRecord.getParameters(), params).flatMap(Stream::of)
            .toArray();

    // Set the message and parameters and return.
    baseRecord.setMessage(messageBase + message);
    baseRecord.setParameters(merged);
    return baseRecord;
  }

  public static void logDebug(String message, Object... params) {
    OutputHelper.getInstance().getDebugLogger()
        .log(getLogRecord(getLogRecord(),
            message, params));
  }

  public static void logDebug() {
    OutputHelper.getInstance().getDebugLogger().log(getLogRecord());
  }
}
