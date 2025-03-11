package com.wharvex.nclos;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class ProcessCreator extends UserlandProcess {
  private final List<Integer> pingPids;
  private final List<Integer> pongPids;
  private final List<Integer> pagingTestAPids;
  private final List<Integer> pagingTestBPids;
  private final List<Integer> virtMemTestAPids;
  private final List<Integer> virtMemTestBPids;

  public ProcessCreator() {
    super("0", "processCreator");
    pingPids = new ArrayList<>();
    pongPids = new ArrayList<>();
    pagingTestAPids = new ArrayList<>();
    pagingTestBPids = new ArrayList<>();
    virtMemTestAPids = new ArrayList<>();
    virtMemTestBPids = new ArrayList<>();
  }

  public List<Integer> getVirtMemTestAPids() {
    return virtMemTestAPids;
  }

  public List<Integer> getVirtMemTestBPids() {
    return virtMemTestBPids;
  }

  public List<Integer> getPagingTestAPids() {
    return pagingTestAPids;
  }

  public List<Integer> getPagingTestBPids() {
    return pagingTestBPids;
  }

  private void debugPrintOtherThreads() {
    OutputHelper.getInstance().getDebugLogger().log(Level.INFO,
        "ProcessCreator says: Debugging other threads\nBootloader is " +
            ThreadHelper.getThreadStateString("bootloaderThread") +
            "\nMain is " + ThreadHelper.getThreadStateString("mainThread") +
            "\nKernel is " +
            ThreadHelper.getThreadStateString("kernelThread") +
            "\nTimer is " +
            ThreadHelper.getThreadStateString("timerThread"));
  }

  private void testMessages(int iterationCounter) {
    // Choose what to do based on iteration counter.
    switch (iterationCounter) {
      case 1:
        // Create Ping.
        OutputHelper.getInstance().getMainOutputLogger().log(Level.INFO,
            "ProcessCreator creating Ping");
        OS.createProcess(
            this,
            new Ping(),
            Scheduler.PriorityType.INTERACTIVE,
            pid -> this.addToPingPids((int) pid));
        break;
      case 2:
        // Create Pong.
        OutputHelper.getInstance().getMainOutputLogger().log(Level.INFO,
            "ProcessCreator creating Pong");
        OS.createProcess(
            this,
            new Pong(),
            Scheduler.PriorityType.INTERACTIVE,
            pid -> this.addToPongPids((int) pid));
        break;
      case 3:
        // Send Pong's pid to Ping.
        OutputHelper.getInstance().getMainOutputLogger().log(Level.INFO,
            "ProcessCreator sending message to ping with content: Pong's" +
                " pid");
        OS.sendMessage(
            this,
            new KernelMessage(getPingPids().getFirst(), 1,
                getPongPids().getFirst().toString()));
      case 4:
        // Send Ping's pid to Pong.
        OutputHelper.getInstance().getMainOutputLogger().log(Level.INFO,
            "ProcessCreator sending message to pong with content: Ping's" +
                " pid");
        OS.sendMessage(
            this,
            new KernelMessage(getPongPids().getFirst(), 1,
                getPingPids().getFirst().toString()));
      default:
        // Done.
        OutputHelper.getInstance().getMainOutputLogger().log(Level.INFO,
            "ProcessCreator done testing messages.");
        debugPrintOtherThreads();
    }
  }

  private void testPaging(int iterationCounter) {
    // Choose what to do based on iteration counter.
    switch (iterationCounter) {
      case 1:
        // Create PagingTestA.
        OutputHelper.getInstance().getMainOutputLogger().log(Level.INFO,
            "ProcessCreator creating PagingTestA");
        OS.createProcess(
            this,
            new PagingTestA(),
            Scheduler.PriorityType.INTERACTIVE,
            pid -> this.addToPagingTestAPids((int) pid));
        break;
      case 2:
        // Create PagingTestB.
        OutputHelper.getInstance().getMainOutputLogger().log(Level.INFO,
            "ProcessCreator creating PagingTestB");
        OS.createProcess(
            this,
            new PagingTestB(),
            Scheduler.PriorityType.INTERACTIVE,
            pid -> this.addToPagingTestBPids((int) pid));
        break;
      default:
        // Done.
        OutputHelper.getInstance().getMainOutputLogger().log(Level.INFO,
            "ProcessCreator says: I'm done testing paging.\n" +
                "ProcessCreator says: PagingTestA pid: " +
                getPagingTestAPids().getFirst() + "\n" +
                "ProcessCreator says: PagingTestB pid: " +
                getPagingTestBPids().getFirst());
        debugPrintOtherThreads();
    }
  }

  private void testVirtualMemory(int iterationCounter) {
    // Choose what to do based on iteration counter.
    switch (iterationCounter) {
      case 1:
        // Create VirtMemTestA
        OutputHelper.getInstance().getMainOutputLogger().log(Level.INFO,
            "ProcessCreator creating VirtMemTestA");
        OS.createProcess(
            this,
            new VirtMemTestA(),
            Scheduler.PriorityType.INTERACTIVE,
            pid -> this.addToVirtMemTestAPids((int) pid));
        break;
      case 2:
        // Create VirtMemTestB.
        OutputHelper.getInstance().getMainOutputLogger().log(Level.INFO,
            "ProcessCreator says: I'm creating VirtMemTestB");
        OS.createProcess(
            this,
            new VirtMemTestB(),
            Scheduler.PriorityType.INTERACTIVE,
            pid -> this.addToVirtMemTestBPids((int) pid));
        break;
      default:
        // Done.
        OutputHelper.getInstance().getMainOutputLogger().log(Level.INFO,
            "ProcessCreator says: I'm done testing virtual memory.\n" +
                "ProcessCreator says: VirtMemTestA pid: " +
                getVirtMemTestAPids().getFirst() + "\n" +
                "ProcessCreator says: VirtMemTestB pid: " +
                getVirtMemTestBPids().getFirst());
        debugPrintOtherThreads();
    }
  }

  @Override
  void main() {
    int i = 0;
    while (true) {
      // Initial announcement.
      OutputHelper.getInstance().getMainOutputLogger().log(Level.INFO,
          "ProcessCreator says: This is iteration " + i++);

      // Test messages.
      // testMessages(i);

      // Test paging.
      // testPaging(i);

      // Test virtual memory.
      testVirtualMemory(i);

      // Sleep and cooperate.
      ThreadHelper.threadSleep(1000);
      cooperate();
    }
  }

  public List<Integer> getPingPids() {
    return pingPids;
  }

  public List<Integer> getPongPids() {
    return pongPids;
  }

  public void addToPingPids(int pid) {
    OutputHelper.getInstance().getMainOutputLogger().log(Level.INFO,
        "ProcessCreator says: Adding " + pid + " to pingPids");
    getPingPids().add(pid);
    OutputHelper.getInstance().getMainOutputLogger().log(Level.INFO,
        "ProcessCreator says: Contents of pingPids: " + getPingPids());
  }

  public void addToPongPids(int pid) {
    OutputHelper.getInstance().getDebugLogger().log(Level.INFO,
        "ProcessCreator says: Adding " + pid + " to pongPids");
    getPongPids().add(pid);
    OutputHelper.getInstance().getDebugLogger().log(Level.INFO,
        "ProcessCreator says: Contents of pongPids: " + getPongPids());
  }

  public void addToPagingTestAPids(int pid) {
    OutputHelper.getInstance().getDebugLogger().log(Level.INFO,
        "ProcessCreator says: Adding " + pid + " to pagingTestAPids");
    getPagingTestAPids().add(pid);
    OutputHelper.getInstance().getDebugLogger().log(Level.INFO,
        "ProcessCreator says: Contents of pagingTestAPids: " +
            getPagingTestAPids());
  }

  public void addToPagingTestBPids(int pid) {
    OutputHelper.getInstance().getDebugLogger().log(Level.INFO,
        "ProcessCreator says: Adding " + pid + " to pagingTestBPids");
    getPagingTestBPids().add(pid);
    OutputHelper.getInstance().getDebugLogger().log(Level.INFO,
        "ProcessCreator says: Contents of pagingTestBPids: " +
            getPagingTestBPids());
  }

  public void addToVirtMemTestAPids(int pid) {
    OutputHelper.getInstance().getDebugLogger().log(Level.INFO,
        "ProcessCreator says: Adding " + pid + " to virtMemTestAPids");
    getVirtMemTestAPids().add(pid);
    OutputHelper.getInstance().getDebugLogger().log(Level.INFO,
        "ProcessCreator says: Contents of virtMemTestAPids: " +
            getVirtMemTestAPids());
  }

  public void addToVirtMemTestBPids(int pid) {
    OutputHelper.getInstance().getDebugLogger().log(Level.INFO,
        "ProcessCreator says: Adding " + pid + " to virtMemTestBPids");
    getVirtMemTestBPids().add(pid);
    OutputHelper.getInstance().getDebugLogger().log(Level.INFO,
        "ProcessCreator says: Contents of virtMemTestBPids: " +
            getVirtMemTestBPids());
  }
}
