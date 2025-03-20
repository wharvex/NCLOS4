package com.wharvex.nclos;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;

public abstract class UserlandProcess
    implements Runnable, UnprivilegedContextSwitcher {
  public static final byte[] PHYSICAL_MEMORY =
      new byte[OS.getPhysicalMemorySize()];
  // TLB stands for Translation Lookaside Buffer.
  private static final int[][] TLB =
      new int[OS.getTlbSize()][OS.getTlbSize()];
  private final String debugPid;
  private final Semaphore semaphore;
  private final Thread thread;
  private final List<Object> csRets;
  private final List<Object> swiProRets;
  private final List<KernelMessage> messages;
  private boolean shouldStopFromTimeout;
  private Boolean shouldStopAfterContextSwitch;

  public UserlandProcess(String debugPid, String threadNameBase) {
    this.debugPid = debugPid;
    thread = new Thread(this, threadNameBase + "Thread_" + debugPid);
    semaphore = new Semaphore(0);
    csRets = new ArrayList<>();
    swiProRets = new ArrayList<>();
    messages = new ArrayList<>();
    shouldStopFromTimeout = false;
  }

  private static synchronized int[][] getTlb() {
    return TLB;
  }

  public static synchronized byte getFromPhysicalMemory(int idx) {
    return PHYSICAL_MEMORY[idx];
  }

  public static synchronized void setOnPhysicalMemory(int idx, byte val) {
    PHYSICAL_MEMORY[idx] = val;
  }

  public static byte preGetFromPhysicalMemory(int idx) {
    OutputHelper.getInstance().getDebugLogger()
        .log(Level.INFO,
            "UP says: I'm about to enter getFromPhysicalMemory");
    var ret = getFromPhysicalMemory(idx);
    OutputHelper.getInstance().getDebugLogger()
        .log(Level.INFO,
            "UP says: I have left getFromPhysicalMemory");
    return ret;
  }

  public static void preSetOnPhysicalMemory(int idx, byte val) {
    OutputHelper.getInstance().getDebugLogger()
        .log(Level.INFO,
            "UP says: I'm about to enter setOnPhysicalMemory");
    setOnPhysicalMemory(idx, val);
    OutputHelper.getInstance().getDebugLogger()
        .log(Level.INFO,
            "UP says: I have left setOnPhysicalMemory");
  }

  private static synchronized int getFromTlb(int vOrP, int zOrF) {
    var ret = getTlb()[vOrP][zOrF];
    OutputHelper.getInstance().getDebugLogger()
        .log(Level.INFO, "TLB[" + vOrP + "][" + zOrF + "] is " + ret);
    return ret;
  }

  private static synchronized void setOnTlb(int vOrP, int zOrF, int val) {
    OutputHelper.getInstance().getDebugLogger()
        .log(Level.INFO,
            "Setting TLB[" + vOrP + "][" + zOrF + "] to " + val);
    getTlb()[vOrP][zOrF] = val;
  }

  public static int preGetFromTlb(int vOrP, int zOrF) {
    OutputHelper.getInstance().getDebugLogger()
        .log(Level.INFO, "UP says: I'm about to enter getFromTlb");
    var ret = getFromTlb(vOrP, zOrF);
    OutputHelper.getInstance().getDebugLogger()
        .log(Level.INFO, "UP says: I have left getFromTlb");
    return ret;
  }

  public static synchronized void preSetOnTlb(int vOrP, int zOrF, int val) {
    OutputHelper.getInstance().getDebugLogger()
        .log(Level.INFO, "UP says: I'm about to enter setOnTlb");
    setOnTlb(vOrP, zOrF, val);
    OutputHelper.getInstance().getDebugLogger()
        .log(Level.INFO, "UP says: I have left setOnTlb");
  }

  /**
   * Only called by Timer thread via PCB.
   */
  public void requestStop() {
    preSetStopRequested(true);
  }

  public synchronized boolean isStopRequested() {
    OutputHelper.getInstance()
        .getDebugLogger()
        .log(Level.INFO,
            "UP says: I'm in isStopRequested\nshouldStopFromTimeout is " +
                shouldStopFromTimeout);
    return shouldStopFromTimeout;
  }

  // todo: make separate "unsetStopRequested" method instead of passing in a
  //  boolean?
  public synchronized void setStopRequested(boolean isRequested) {
    OutputHelper.getInstance()
        .getDebugLogger()
        .log(Level.INFO,
            "UP says: I'm in setStopRequested\n" +
                "setting shouldStopFromTimeout to " +
                isRequested);
    shouldStopFromTimeout = isRequested;
  }

  public void preSetStopRequested(boolean isRequested) {
    OutputHelper.getInstance().getDebugLogger()
        .log(Level.INFO,
            "UP says: I'm about to enter setStopRequested");
    setStopRequested(isRequested);
    OutputHelper.getInstance().getDebugLogger()
        .log(Level.INFO,
            "UP says: I have left setStopRequested");
  }

  public boolean preIsStopRequested() {
    OutputHelper.getInstance().getDebugLogger()
        .log(Level.INFO,
            "UP says: I'm about to enter isStopRequested");
    var ret = isStopRequested();
    OutputHelper.getInstance().getDebugLogger()
        .log(Level.INFO,
            "UP says: I have left isStopRequested");
    return ret;
  }

  public void cooperate() {
    OutputHelper.getInstance().getDebugLogger()
        .log(Level.INFO, "UP says: I'm in cooperate");
    if (preIsStopRequested()) {
      OS.switchProcess(this);
    }
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
  public void run() {
    OutputHelper.getInstance()
        .getDebugLogger()
        .log(Level.INFO, "UP says: I'm just getting started.");
    stop();
    main();
  }

  abstract void main();

  public String getDebugPid() {
    return debugPid;
  }

  public Boolean getShouldStopAfterContextSwitch() {
    return shouldStopAfterContextSwitch;
  }

  public void setShouldStopAfterContextSwitch(
      boolean shouldStopAfterContextSwitch) {
    OutputHelper.getInstance().getDebugLogger()
        .log(Level.INFO,
            "UP says: I'm in setShouldStopAfterContextSwitch\n" +
                "Setting shouldStopAfterContextSwitch to " +
                shouldStopAfterContextSwitch);
    this.shouldStopAfterContextSwitch = shouldStopAfterContextSwitch;
  }

  @Override
  public List<Object> getCsRets() {
    return csRets;
  }

  @Override
  public List<KernelMessage> getMessages() {
    return messages;
  }

  public void addAllToMessages(List<KernelMessage> kms) {
    getMessages().addAll(kms);
  }

  // TODO: Can we implement something a little better here?
  //  Not a big fan of sleep in a loop.
  public void waitUntilStoppedByRequest() {
    while (preIsStopRequested()) {
      OutputHelper.getInstance().getDebugLogger()
          .log(Level.INFO,
              "UP says: I'm in waitUntilStoppedByRequest\n" +
                  "Waiting for " + getThreadName() +
                  " to stop from request");
      ThreadHelper.threadSleep(10);
    }
  }

  private int getPhysAddr(int virtAddr) {
    int virtualPageNumber = virtAddr / OS.getPageSize();
    OutputHelper.getInstance().getDebugLogger()
        .log(Level.INFO,
            "UP says: I'm in getPhysAddr\n" +
                "virtualPageNumber is " + virtualPageNumber);
    int pageOffset = virtAddr % OS.getPageSize();
    OutputHelper.getInstance().getDebugLogger()
        .log(Level.INFO,
            "UP says: I'm in getPhysAddr\n" +
                "pageOffset is " + pageOffset);
    int physicalPageNumber =
        matchAndReturnPhys(virtualPageNumber).orElse(-1);
    OutputHelper.getInstance().getDebugLogger()
        .log(Level.INFO,
            "UP says: I'm in getPhysAddr\n" +
                "physicalPageNumber is " + physicalPageNumber);
    int physicalAddress =
        (physicalPageNumber * OS.getPageSize()) + pageOffset;
    OutputHelper.getInstance().getDebugLogger()
        .log(Level.INFO,
            "UP says: I'm in getPhysAddr\n" +
                "physicalAddress is " + physicalAddress);
    return physicalAddress;
  }

  public byte read(int virtualAddress) {
    int physAddr = getPhysAddr(virtualAddress);
    if (physAddr >= 0) {
      return preGetFromPhysicalMemory(physAddr);
    }
    return -1;
  }

  public void write(int virtualAddress, byte value) {
    int physAddr = getPhysAddr(virtualAddress);
    if (physAddr >= 0) {
      preSetOnPhysicalMemory(physAddr, value);
    }
  }

  protected OptionalInt matchAndReturnPhys(int virtualPageNumber) {
    if (preGetFromTlb(0, 0) == virtualPageNumber) {
      return OptionalInt.of(preGetFromTlb(1, 0));
    } else if (preGetFromTlb(0, 1) == virtualPageNumber) {
      return OptionalInt.of(preGetFromTlb(1, 1));
    } else {
      return OptionalInt.empty();
    }
  }
}
