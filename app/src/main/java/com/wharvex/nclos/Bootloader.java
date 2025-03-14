package com.wharvex.nclos;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class Bootloader implements UnprivilegedContextSwitcher, Runnable {
  private final Semaphore semaphore;
  private final Thread thread;
  private final List<Object> csRets;
  private final int testChoice;

  public Bootloader(int testChoice) {
    semaphore = new Semaphore(0);
    thread = new Thread(this, "bootloaderThread");
    csRets = new ArrayList<>();
    this.testChoice = testChoice;
  }

  @Override
  public Semaphore getSemaphore() {
    return semaphore;
  }

  @Override
  public Thread getThread() {
    return thread;
  }

  @Override
  public List<Object> getCsRets() {
    return csRets;
  }

  @Override
  public List<KernelMessage> getMessages() {
    return null;
  }

  @Override
  public boolean isStopped() {
    return UnprivilegedContextSwitcher.super.isStopped();
  }

  @Override
  public void run() {
    NclosLogger.logDebugThread(ThreadLifeStage.STARTING);
    OS.startup(this, testChoice);
  }
}
