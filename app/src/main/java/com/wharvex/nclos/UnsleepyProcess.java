package com.wharvex.nclos;

import java.util.UUID;
import java.util.logging.Level;

public class UnsleepyProcess extends UserlandProcess {
  public UnsleepyProcess() {
    super(UUID.randomUUID().toString().substring(24), "unsleepy");
  }

  @Override
  void main() {
    int i = 0;
    while (true) {
      NclosLogger.logDebug("i = " + (++i));
      cooperate();
      ThreadHelper.threadSleep(1000);
    }
  }
}
