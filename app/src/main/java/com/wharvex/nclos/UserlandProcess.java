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
    NclosLogger.logDebugSync(ExecutionPathStage.BEFORE_ENTER);
    var ret = getFromPhysicalMemory(idx);
    NclosLogger.logDebugSync(ExecutionPathStage.AFTER_EXIT);
    return ret;
  }

  public static void preSetOnPhysicalMemory(int idx, byte val) {
    NclosLogger.logDebugSync(ExecutionPathStage.BEFORE_ENTER);
    setOnPhysicalMemory(idx, val);
    NclosLogger.logDebugSync(ExecutionPathStage.AFTER_EXIT);
  }

  // todo: define z, p, v, f
  private static synchronized int getFromTlb(int vOrP, int zOrF) {
    var ret = getTlb()[vOrP][zOrF];
    NclosLogger.logDebug(
        "Getting TLB[" + vOrP + "][" + zOrF + "], which is " + ret);
    return ret;
  }

  private static synchronized void setOnTlb(int vOrP, int zOrF, int val) {
    NclosLogger.logDebug(
        "Setting TLB[" + vOrP + "][" + zOrF + "] to " + val);
    getTlb()[vOrP][zOrF] = val;
  }

  public static int preGetFromTlb(int vOrP, int zOrF) {
    NclosLogger.logDebugSync(ExecutionPathStage.BEFORE_ENTER);
    var ret = getFromTlb(vOrP, zOrF);
    NclosLogger.logDebugSync(ExecutionPathStage.AFTER_EXIT);
    return ret;
  }

  public static synchronized void preSetOnTlb(int vOrP, int zOrF, int val) {
    NclosLogger.logDebugSync(ExecutionPathStage.BEFORE_ENTER);
    setOnTlb(vOrP, zOrF, val);
    NclosLogger.logDebugSync(ExecutionPathStage.AFTER_EXIT);
  }

  /**
   * Only called by Timer thread via PCB.
   */
  public void requestStop() {
    preSetStopRequested(true);
  }

  public synchronized boolean isStopRequested() {
    NclosLogger.logDebugSync(ExecutionPathStage.IN,
        "shouldStopFromTimeout is " + shouldStopFromTimeout);
    return shouldStopFromTimeout;
  }

  // todo: make separate "unsetStopRequested" method instead of passing in a
  //  boolean?
  public synchronized void setStopRequested(boolean isRequested) {
    NclosLogger.logDebugSync(ExecutionPathStage.IN,
        "setting shouldStopFromTimeout to " + isRequested);
    shouldStopFromTimeout = isRequested;
  }

  // todo: I don't like this way of naming methods because it's not obvious
  //  what the "pre" applies to.
  public void preSetStopRequested(boolean isRequested) {
    NclosLogger.logDebugSync(ExecutionPathStage.BEFORE_ENTER);
    setStopRequested(isRequested);
    NclosLogger.logDebugSync(ExecutionPathStage.AFTER_EXIT);
  }

  public boolean preIsStopRequested() {
    NclosLogger.logDebugSync(ExecutionPathStage.BEFORE_ENTER);
    var ret = isStopRequested();
    NclosLogger.logDebugSync(ExecutionPathStage.AFTER_EXIT);
    return ret;
  }

  public void cooperate() {
    NclosLogger.logDebug();
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
    NclosLogger.logDebugThread(ThreadLifeStage.STARTING);
    stop();
    main();
  }

  abstract void main();

  public String getDebugPid() {
    return debugPid;
  }

  // todo: Why is this a capital-B boolean??
  public Boolean getShouldStopAfterContextSwitch() {
    return shouldStopAfterContextSwitch;
  }

  public void setShouldStopAfterContextSwitch(
      boolean shouldStopAfterContextSwitch) {
    NclosLogger.logDebug("Setting shouldStopAfterContextSwitch to " +
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
      NclosLogger.logDebug("Waiting for " + getThreadName() + " to stop");
      ThreadHelper.threadSleep(10);
    }
  }

  private int getPhysAddr(int virtAddr) {
    int virtualPageNumber = virtAddr / OS.getPageSize();
    NclosLogger.logDebug("virtualPageNumber is " + virtualPageNumber);
    int pageOffset = virtAddr % OS.getPageSize();
    NclosLogger.logDebug("pageOffset is " + pageOffset);
    int physicalPageNumber =
        matchAndReturnPhys(virtualPageNumber).orElse(-1);
    NclosLogger.logDebug("physicalPageNumber is " + physicalPageNumber);
    int physicalAddress =
        (physicalPageNumber * OS.getPageSize()) + pageOffset;
    NclosLogger.logDebug("physicalAddress is " + physicalAddress);
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
