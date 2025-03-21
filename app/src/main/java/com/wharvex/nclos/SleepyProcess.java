package com.wharvex.nclos;

import java.util.UUID;
import java.util.logging.Level;

public class SleepyProcess extends UserlandProcess {
  public SleepyProcess() {
    super(UUID.randomUUID().toString().substring(24), "sleepy");
  }

  @Override
  void main() {
    int i = 0;
    while (true) {
      NclosLogger.logMain("i = " + (++i));
      ThreadHelper.threadSleep(1000);
      cooperate();
    }
  }
}
