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
  private final int testChoice;

  public ProcessCreator(int testChoice) {
    super("0", "processCreator");
    pingPids = new ArrayList<>();
    pongPids = new ArrayList<>();
    pagingTestAPids = new ArrayList<>();
    pagingTestBPids = new ArrayList<>();
    virtMemTestAPids = new ArrayList<>();
    virtMemTestBPids = new ArrayList<>();
    this.testChoice = testChoice;
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
    NclosLogger.logDebug("Bootloader is " +
        ThreadHelper.getThreadStateString("bootloaderThread") +
        ", Main is " + ThreadHelper.getThreadStateString("mainThread") +
        ", Kernel is " +
        ThreadHelper.getThreadStateString("kernelThread") +
        ", Timer is " +
        ThreadHelper.getThreadStateString("timerThread"));
  }

  private void testMessages(int iterationCounter) {
    // Choose what to do based on iteration counter.
    switch (iterationCounter) {
      case 1:
        // Create Ping.
        NclosLogger.logMain("Creating Ping");
        OS.createProcess(
            this,
            new Ping(),
            Scheduler.PriorityType.INTERACTIVE,
            pid -> this.addToPingPids((int) pid));
        break;
      case 2:
        // Create Pong.
        NclosLogger.logMain("Creating Pong");
        OS.createProcess(
            this,
            new Pong(),
            Scheduler.PriorityType.INTERACTIVE,
            pid -> this.addToPongPids((int) pid));
        break;
      case 3:
        // Send Pong's pid to Ping.
        NclosLogger.logMain("Sending message to ping");
        OS.sendMessage(
            this,
            new KernelMessage(getPingPids().getFirst(), 1,
                getPongPids().getFirst().toString()));
      case 4:
        // Send Ping's pid to Pong.
        NclosLogger.logMain("Sending message to pong");
        OS.sendMessage(
            this,
            new KernelMessage(getPongPids().getFirst(), 1,
                getPingPids().getFirst().toString()));
      default:
        // Done.
        NclosLogger.logMain("Done testing messages.");
        debugPrintOtherThreads();
    }
  }

  // todo: this is very similar to testVirtMem
  private void testPaging(int iterationCounter) {
    // Choose what to do based on iteration counter.
    // todo: make these user choice as well
    switch (iterationCounter) {
      case 1:
        // Create PagingTestA.
        NclosLogger.logMain("Creating PagingTestA");
        OS.createProcess(
            this,
            new PagingTestA(),
            Scheduler.PriorityType.INTERACTIVE,
            pid -> this.addToPagingTestAPids((int) pid));
        break;
      case 2:
        // Create PagingTestB.
        NclosLogger.logMain("Creating PagingTestB");
        OS.createProcess(
            this,
            new PagingTestB(),
            Scheduler.PriorityType.INTERACTIVE,
            pid -> this.addToPagingTestBPids((int) pid));
        break;
      default:
        // Done.
        NclosLogger.logMain("Done testing paging.");
        debugPrintOtherThreads();
    }
  }

  private void testVirtualMemory(int iterationCounter) {
    // Choose what to do based on iteration counter.
    switch (iterationCounter) {
      case 1:
        // Create VirtMemTestA
        NclosLogger.logMain("Creating VirtMemTestA");
        OS.createProcess(
            this,
            new VirtMemTestA(),
            Scheduler.PriorityType.INTERACTIVE,
            pid -> this.addToVirtMemTestAPids((int) pid));
        break;
      case 2:
        // Create VirtMemTestB.
        NclosLogger.logMain("Creating VirtMemTestB");
        OS.createProcess(
            this,
            new VirtMemTestB(),
            Scheduler.PriorityType.INTERACTIVE,
            pid -> this.addToVirtMemTestBPids((int) pid));
        break;
      default:
        // Done.
        NclosLogger.logMain("Done testing virtual memory.");
        debugPrintOtherThreads();
    }
  }

  @Override
  void main() {
    int i = 0;
    while (true) {
      // Initial announcement.
      NclosLogger.logMain("iteration " + i++);

      // TODO: Make it so the user can choose a new test after the current
      //  one ends. This might require a new method in OS to stop all
      //  processes.

      // TODO: Make testDevices and testPriorityScheduler methods.

      // Choose what to do based on test choice.
      switch (testChoice) {
        case 3:
          testMessages(i);
          break;
        case 4:
          testPaging(i);
          break;
        case 5:
          testVirtualMemory(i);
          break;
        default:
          // Invalid choice.
          NclosLogger.logMain("Invalid test choice.");
          return;
      }

      // Sleep and cooperate.
      // TODO: Why does it sleep before cooperating?
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
    NclosLogger.logDebug("adding " + pid + " to pingPids");
    getPingPids().add(pid);
    NclosLogger.logDebug("contents of pingPids -> " + getPingPids());
  }

  public void addToPongPids(int pid) {
    NclosLogger.logDebug("adding " + pid + " to pongPids");
    getPongPids().add(pid);
    NclosLogger.logDebug("contents of pongPids -> " + getPongPids());
  }

  public void addToPagingTestAPids(int pid) {
    NclosLogger.logDebug("adding " + pid + " to pagingTestAPids");
    getPagingTestAPids().add(pid);
    NclosLogger.logDebug("contents of pagingTestAPids -> " +
        getPagingTestAPids());
  }

  // todo: this is very similar to addToPagingTestAPids
  public void addToPagingTestBPids(int pid) {
    NclosLogger.logDebug("adding " + pid + " to pagingTestBPids");
    getPagingTestBPids().add(pid);
    NclosLogger.logDebug("contents of pagingTestBPids -> " +
        getPagingTestBPids());
  }

  public void addToVirtMemTestAPids(int pid) {
    NclosLogger.logDebug("adding " + pid + " to virtMemTestAPids");
    getVirtMemTestAPids().add(pid);
    NclosLogger.logDebug("contents of virtMemTestAPids -> " +
        getVirtMemTestAPids());
  }

  // todo: this is very similar to addToVirtMemTestAPids
  public void addToVirtMemTestBPids(int pid) {
    NclosLogger.logDebug("adding " + pid + " to virtMemTestBPids");
    getVirtMemTestBPids().add(pid);
    NclosLogger.logDebug("contents of virtMemTestBPids -> " +
        getVirtMemTestBPids());
  }
}
