package com.wharvex.nclos;

import java.text.MessageFormat;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.stream.Stream;

public class NclosLogger {
  private static final String messageBase =
      "THREAD:{0};PREV_FRAME.CLASS:{1};PREV_FRAME.METHOD:{2};";
  private static final String noteExtension = "NOTE:{3};";
  private static final String noteExtension2 = "NOTE:{4};";
  private static final String syncExtension = "SYNC_STAGE:{3};";
  private static final String threadExtension = "THREAD_STAGE:{3};";
  private static final String syncWithNoteExtension = syncExtension +
      noteExtension2;
  private static final String threadWithNoteExtension = threadExtension +
      noteExtension2;

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

  public static void logDebug(Object param) {
    OutputHelper.getInstance().getDebugLogger()
        .log(getLogRecord(Level.INFO, noteExtension, param));
  }

  public static void logDebug() {
    OutputHelper.getInstance().getDebugLogger()
        .log(getLogRecord(Level.INFO, ""));
  }

  public static void logMain(Object param) {
    OutputHelper.getInstance().getMainOutputLogger()
        .log(getLogRecord(Level.INFO, noteExtension, param));
    OutputHelper.getInstance().getDebugLogger()
        .log(getLogRecord(Level.INFO, noteExtension, param));
  }

  public static void logDebugSync(ExecutionPathStage stage) {
    OutputHelper.getInstance().getDebugLogger().log(getLogRecord(Level.INFO,
        syncExtension, stage));
  }

  public static void logDebugSync(ExecutionPathStage stage, Object note) {
    OutputHelper.getInstance().getDebugLogger().log(getLogRecord(Level.INFO,
        syncWithNoteExtension, stage, note));
  }

  public static void logDebugThread(ThreadLifeStage stage) {
    OutputHelper.getInstance().getDebugLogger().log(getLogRecord(Level.INFO,
        threadExtension, stage));
  }

  public static void logDebugThread(ThreadLifeStage stage, Object note) {
    OutputHelper.getInstance().getDebugLogger().log(getLogRecord(Level.INFO,
        threadWithNoteExtension, stage, note));
  }

  public static Supplier<String> logError(Object param) {
    return () -> {
      var record = getLogRecord(Level.SEVERE, noteExtension, param);
      OutputHelper.getInstance().getDebugLogger().log(record);
      OutputHelper.getInstance().getMainOutputLogger().log(record);
      return MessageFormat.format(record.getMessage(),
          record.getParameters());
    };
  }
}
