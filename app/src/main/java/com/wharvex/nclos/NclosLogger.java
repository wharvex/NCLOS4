package com.wharvex.nclos;

import java.util.logging.Level;

public class NclosLogger {
  public static void logDebug(String... messages) {
    OutputHelper.getInstance().getDebugLogger()
        .log(Level.INFO, "end execution trace");
  }
}
