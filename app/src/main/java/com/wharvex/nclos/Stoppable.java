package com.wharvex.nclos;

import java.util.concurrent.Semaphore;
import java.util.logging.Level;

public interface Stoppable {

  default void init() {
    NclosLogger.logDebugThread(ThreadLifeStage.STARTING, getThreadName());
    getThread().start();
  }

  default String getThreadName() {
    return getThread().getName();
  }

  default Thread.State getThreadState() {
    return getThread().getState();
  }

  // Every semaphore is binary in this program.
  default void stop() {
    if (Thread.currentThread() != getThread()) {
      throw new RuntimeException(NclosLogger.logError(
          "Parking space reserved for " + getThreadName()).get());
    }
    NclosLogger.logDebugThread(ThreadLifeStage.STOPPING);
    try {
      getSemaphore().acquire();
    } catch (InterruptedException e) {
      NclosLogger.logError(
          getThreadName() + " interrupted while parked at its semaphore");
      Thread.currentThread().interrupt();
    }
    NclosLogger.logDebugThread(ThreadLifeStage.STARTING);
  }

  Semaphore getSemaphore();

  Thread getThread();

  default boolean isStopped() {
    return getSemaphore().hasQueuedThreads();
  }

  default void waitUntilStopped() {
    while (!isStopped()) {
      NclosLogger.logDebug("Waiting for " + getThreadName() + " to stop");
      ThreadHelper.threadSleep(10);
    }
  }

  default void start() {
    // Wait until what we want to start is stopped.
    waitUntilStopped();

    // Ensure semaphore remains binary.
    if (getSemaphore().availablePermits() < 1) {
      NclosLogger.logDebug("Releasing " + getThreadName() + "'s semaphore");
      getSemaphore().release();
    } else {
      // This is part of what enforces the semaphores as binary.
      NclosLogger.logDebug("Not releasing " + getThreadName() + "'s sema4");
    }
  }
}
