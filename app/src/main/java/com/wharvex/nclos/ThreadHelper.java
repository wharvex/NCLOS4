package com.wharvex.nclos;

import java.util.Arrays;
import java.util.Optional;

public class ThreadHelper {
  // TODO: Make a method for enforcing semaphore parking space reservations.

  public static Optional<Thread> getThreadByName(String threadName) {
    return Thread.getAllStackTraces().keySet().stream()
        .filter(t -> t.getName().equals(threadName))
        .findFirst();
  }

  public static String getThreadStateString(String threadName) {
    return getThreadByName(threadName).map(t -> t.getState().toString())
        .orElse("non-existent");
  }

  public static void printAllThreads() {
    Thread.getAllStackTraces()
        .keySet()
        .forEach(
            t ->
                OutputHelper.getInstance().getDebugLogger().info(
                    "Thread: " + t.getName() + "\nState: " + t.getState() +
                        "\nStack trace: " +
                        Arrays.toString(t.getStackTrace())));
  }

  public static void threadSleep(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      System.out.println("Sleep interrupted.");
    }
  }
}
