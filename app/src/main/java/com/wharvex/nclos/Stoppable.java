package com.wharvex.nclos;

import java.util.concurrent.Semaphore;
import java.util.logging.Level;

public interface Stoppable {

  default void init() {
    OutputHelper.getInstance().getDebugLogger()
        .log(Level.INFO, "Initializing " + getThreadName());
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
      throw new RuntimeException(
          OutputHelper.getInstance()
              .logToAllAndReturnMessage("Parking space reserved for " +
                  getThreadName(), Level.SEVERE));
    }
    OutputHelper.getInstance().getDebugLogger()
        .log(Level.INFO, getThreadName() + " says: I'm stopping");
    try {
      getSemaphore().acquire();
    } catch (InterruptedException e) {
      OutputHelper.getInstance().logToAll(
          getThreadName() + " interrupted while parked at its semaphore",
          Level.SEVERE);
      Thread.currentThread().interrupt();
    }
    OutputHelper.getInstance().getDebugLogger()
        .log(Level.INFO, getThreadName() + " says: I'm starting");
  }

  Semaphore getSemaphore();

  Thread getThread();

  default boolean isStopped() {
    return getSemaphore().hasQueuedThreads();
  }

  default void waitUntilStopped() {
    while (!isStopped()) {
      OutputHelper.getInstance().getDebugLogger()
          .log(Level.INFO, "Waiting for " + getThreadName() + " to stop");
      ThreadHelper.threadSleep(10);
    }
  }

  default void start() {
    // Wait until what we want to start is stopped.
    waitUntilStopped();

    // Ensure semaphore remains binary.
    if (getSemaphore().availablePermits() < 1) {
      OutputHelper.getInstance().getDebugLogger()
          .log(Level.INFO, "Releasing " + getThreadName() + "'s semaphore");
      getSemaphore().release();
    } else {
      // This is part of what enforces the semaphores as binary.
      OutputHelper.getInstance().getDebugLogger().log(Level.INFO,
          "Did not release " + getThreadName() +
              "'s semaphore because it is already released");
    }
  }
}
