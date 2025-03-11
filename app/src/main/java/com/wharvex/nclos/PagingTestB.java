package com.wharvex.nclos;

import java.util.UUID;
import java.util.logging.Level;

public class PagingTestB extends UserlandProcess {
  public PagingTestB() {
    super(UUID.randomUUID().toString().substring(24), "pagingB");
  }

  @Override
  void main() {
    int i = 0;
    while (true) {
      OutputHelper.getInstance().getMainOutputLogger().log(Level.INFO,
          "Hello from PagingTestB " + getDebugPid() + " (times printed: " +
              (++i) + ")");
      ThreadHelper.threadSleep(1000);
      cooperate();
    }
  }
}
