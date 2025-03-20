package com.wharvex.nclos;

import java.util.UUID;

public class PagingTestB extends UserlandProcess {
  public PagingTestB() {
    super(UUID.randomUUID().toString().substring(24), "pagingB");
  }

  @Override
  void main() {
    int i = 0;
    while (true) {
      NclosLogger.logMain("hello from PagingTestB, i=" + (++i));
      ThreadHelper.threadSleep(1000);
      cooperate();
    }
  }
}
