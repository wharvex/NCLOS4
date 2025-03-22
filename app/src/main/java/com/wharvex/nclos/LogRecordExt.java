package com.wharvex.nclos;

import java.time.Clock;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.time.Instant;

public class LogRecordExt extends LogRecord {
  public String getThreadName() {
    return threadName;
  }

  public void setThreadName(String threadName) {
    this.threadName = threadName;
  }

  String threadName;

  public String getPrevClass() {
    return prevClass;
  }

  public void setPrevClass(String prevClass) {
    this.prevClass = prevClass;
  }

  public String getPrevMethod() {
    return prevMethod;
  }

  public void setPrevMethod(String prevMethod) {
    this.prevMethod = prevMethod;
  }

  String prevClass;
  String prevMethod;

  /**
   * Construct a LogRecord with the given level and message values.
   * <p>
   * The sequence property will be initialized with a new unique value.
   * These sequence values are allocated in increasing order within a VM.
   * <p>
   * Since JDK 9, the event time is represented by an {@link Instant}.
   * The instant property will be initialized to the {@linkplain
   * Instant#now() current instant}, using the best available
   * {@linkplain Clock#systemUTC() clock} on the system.
   * <p>
   * The thread ID property will be initialized with a unique ID for
   * the current thread.
   * <p>
   * All other properties will be initialized to "null".
   *
   * @param level a logging level value
   * @param msg   the raw non-localized logging message (may be null)
   * @see Clock#systemUTC()
   */
  public LogRecordExt(Level level, String msg) {
    super(level, msg);
  }
}
