package com.wharvex.nclos;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;

public class OS {

  public static final int EXISTING_SECONDARY_DEVICES = 2;
  public static final int DEVICE_CONTENTS_SIZE = 10;
  private static final int PAGE_SIZE = 1024;
  private static final int MEMORY_MAP_SIZE = 100;
  private static final int FREE_SPACE_SIZE = 1000;
  private static final List<Object> PARAMS = new ArrayList<>();
  private static final int TLB_SIZE = 2;
  private static Kernel kernel;
  private static UnprivilegedContextSwitcher contextSwitcher;
  private static Object retVal;
  private static CallType callType;
  private static List<KernelMessage> messages = new ArrayList<>();

  public static int getPageSize() {
    return PAGE_SIZE;
  }

  public static int getMemoryMapSize() {
    return MEMORY_MAP_SIZE;
  }

  public static int getFreeSpaceSize() {
    return FREE_SPACE_SIZE;
  }

  public static int getPhysicalMemorySize() {
    return getPageSize() * getPageSize();
  }

  public static int getTlbSize() {
    return TLB_SIZE;
  }

  /**
   * Only called by Bootloader thread.
   *
   * @param cs
   * @return
   */
  public static void startup(UnprivilegedContextSwitcher cs) {
    // Create Kernel and start its thread.
    kernel = new Kernel();
    getKernel().init();

    // Create the ProcessCreator process; switch to it; save its pid to the
    // bootloader.
    startupCreateProcess(cs, new ProcessCreator(),
        Scheduler.PriorityType.REALTIME);
  }

  public static void open(
      UnprivilegedContextSwitcher cs, Consumer<Object> retSaver,
      String openCodeAndArg) {
    switchContext(cs, CallType.OPEN, retSaver, openCodeAndArg);
  }

  /**
   * Use Objects.requireNonNull for this method and other getters and setters
   * on OS because OS has no constructor and therefore more null danger.
   *
   * @return
   */
  private static Kernel getKernel() {
    Objects.requireNonNull(kernel,
        "Tried to get OS.kernel but it was null.");
    return kernel;
  }

  public static void sleep(UnprivilegedContextSwitcher cs,
                           long sleepLenInMillis) {
  }

  public static void startupCreateProcess(
      UnprivilegedContextSwitcher cs, UserlandProcess processCreator,
      Scheduler.PriorityType pt) {
    switchContext(cs, CallType.STARTUP_CREATE_PROCESS, cs::addToCsRets,
        processCreator, pt);
  }

  public static void createProcess(
      UnprivilegedContextSwitcher cs,
      UserlandProcess up,
      Scheduler.PriorityType pt,
      Consumer<Object> retSaver) {
    switchContext(cs, CallType.CREATE_PROCESS, retSaver, up, pt);
  }

  public static void sendMessage(UnprivilegedContextSwitcher cs,
                                 KernelMessage km) {
    switchContext(cs, CallType.SEND_MESSAGE, cs::addToCsRets, km);
  }

  public static void waitForMessage(UnprivilegedContextSwitcher cs) {
    switchContext(cs, CallType.WAIT_FOR_MESSAGE, cs::addToCsRets);
  }

  /**
   * The point of this method is to have one central location where all the
   * data sharing needed for a particular context switch occurs.
   *
   * <p>It is synchronized on the context switcher, so no other context
   * switch can occur at the same time.
   *
   * @param cs
   * @param callType
   * @param params
   */
  public static void switchContext(
      UnprivilegedContextSwitcher cs, CallType callType,

      Consumer<Object> retSaver,

      Object... params) {
    OutputHelper.getInstance().getDebugLogger()
        .log(Level.INFO, "About to enter synchronized block for " + cs +
            " with call type " + callType);

    // TODO: Move this synchronized block into the UCS itself.
    synchronized (cs) {
      OutputHelper.getInstance().getDebugLogger().log(Level.INFO,
          "Entered synchronized block for " + cs + " with call type " +
              callType);

      // Store the Runnable whose thread is calling this method.
      setContextSwitcher(cs);

      // Set the call type for this context switch.
      setCallType(callType);

      // Clear current params; set new ones.
      setParams(params);

      // Start Kernel; stop contextSwitcher.
      startKernel(cs);

      // Save the value returned from the Kernel to the context switcher.
      getRetVal().ifPresent(rv -> cs.setContextSwitchRet(retSaver, rv));
    }
    OutputHelper.getInstance().getDebugLogger()
        .log(Level.INFO, "Left synchronized block for " +
            cs + " with call type " + callType);

    // The following is the logic that stops the context switcher if needed.
    // We can't have this in the sync block because then the cs would stop
    // while holding the lock.
    // TODO: Make this more readable.
    OutputHelper.getInstance().getDebugLogger()
        .log(Level.INFO, "Checking if we should stop the contextSwitcher");
    if (cs instanceof UserlandProcess) {
      OutputHelper.getInstance().getDebugLogger().log(Level.INFO,
          "OS.contextSwitcher is a UserlandProcess");
      ((UserlandProcess) cs).preSetStopRequested(false);
      if (((UserlandProcess) cs).getShouldStopAfterContextSwitch()) {
        OutputHelper.getInstance().getDebugLogger().log(Level.INFO,
            "This is where a UserlandProcess stops due to timeout.");
        ((UserlandProcess) cs).setShouldStopAfterContextSwitch(false);
        cs.stop();
      } else if (((UserlandProcess) cs).getShouldStopAfterContextSwitch() ==
          null) {
        // TODO: This might never happen
        OutputHelper.getInstance().getDebugLogger().log(Level.INFO,
            "OS.contextSwitcher.shouldStopAfterContextSwitch has not been " +
                "set yet; continuing...");
      } else {
        OutputHelper.getInstance().getDebugLogger().log(Level.INFO,
            "OS.contextSwitcher.shouldStopAfterContextSwitch is false; " +
                "continuing...");
      }
    } else {
      OutputHelper.getInstance().getDebugLogger().log(Level.INFO,
          "OS.contextSwitcher is not a UserlandProcess; continuing...");
    }
  }

  private static void setParams(Object... newParams) {
    // The params list will be empty (size = 0) after this call returns.
    PARAMS.clear();

    // Throw an exception if any of the new params are null and log the
    // exception.
    if (Arrays.stream(newParams).anyMatch(Objects::isNull)) {
      throw new RuntimeException(
          OutputHelper.getInstance().logToAllAndReturnMessage(
              "Cannot add any null elements to params.",
              Level.SEVERE));
    }

    // Add new params to params.
    PARAMS.addAll(List.of(newParams));
  }

  /**
   * Only called by Kernel thread.
   *
   * @param idx
   * @return
   */
  public static Object getParam(int idx) {
    // Throw an exception if the param index is out of range and log the
    // exception.
    if (idx < 0 || idx >= PARAMS.size()) {
      throw new RuntimeException(
          OutputHelper.getInstance().logToAllAndReturnMessage(
              "Param index " + idx + " out of range.",
              Level.SEVERE));
    }

    // Get the param.
    Object param = PARAMS.get(idx);

    // Throw an exception if the param is null and log the exception.
    Objects.requireNonNull(
        param,
        OutputHelper.getInstance().logToAllAndReturnMessage(
            "Tried to get param at index " + idx + " but it was null.",
            Level.SEVERE));

    // Return the param.
    return param;
  }

  /**
   * Data field X is guarded by the semaphore whose parking space is reserved
   * for the thread that wants to reliably "see" an update made to X.
   *
   * <p>As such, retVal is guarded by the UnprivilegedContextSwitcher's
   * semaphore (and does not need to be declared synchronized).
   *
   * <p>The UCS's thread is the only thread that needs to "see" updates to
   * retVal. As such, we rely solely on UCS's semaphore for visibility
   * assurance, and leave this method unsynchronized.
   *
   * <p>If retVal is null, it means the kernelland method's return type was
   * void, and we do not save it to the contextSwitcher.
   *
   * @return
   */
  public static Optional<Object> getRetVal() {
    return Optional.ofNullable(retVal);
  }

  /**
   * Only called by contextSwitcher thread.
   */
  private static void startKernel(UnprivilegedContextSwitcher cs) {
    startKernelOnly();
    cs.stop();
  }

  public static void startKernelOnly() {
    getKernel().start();
  }

  /**
   * Only called by Kernel thread. Set to null if the kernelland method has a
   * void return type.
   *
   * @param rv The kernelland function return value.
   */
  public static void setRetValOnOS(Object rv) {
    OutputHelper.getInstance().getDebugLogger()
        .log(Level.INFO, "Setting OS.retVal to " + rv);
    retVal = rv;
  }

  public static UnprivilegedContextSwitcher getContextSwitcher() {
    Objects.requireNonNull(
        contextSwitcher,
        OutputHelper.getInstance().logToAllAndReturnMessage(
            "Tried to get OS.contextSwitcher but it was null.",
            Level.SEVERE));
    OutputHelper.getInstance().getDebugLogger().log(Level.INFO,
        "OS.contextSwitcher is " + contextSwitcher.getThreadName());
    return contextSwitcher;
  }

  public static void setContextSwitcher(UnprivilegedContextSwitcher cs) {
    Objects.requireNonNull(
        cs,
        OutputHelper.getInstance().logToAllAndReturnMessage(
            "Cannot set OS.contextSwitcher to null", Level.SEVERE));
    OutputHelper.getInstance().getDebugLogger().log(Level.INFO,
        "Setting OS.contextSwitcher to " + cs.getThreadName());
    contextSwitcher = cs;
  }

  public static CallType getCallType() {
    Objects.requireNonNull(callType,
        OutputHelper.getInstance().logToAllAndReturnMessage(
            "Tried to get OS.callType but it was null", Level.SEVERE));
    return callType;
  }

  /**
   * This is called prior to calling "release" on the Kernel's semaphore, so
   * it "happens-before" the Kernel's call to getCallType, which follows the
   * Kernel's successful "acquire" of its UCS-released semaphore (see the
   * Oracle docs on Semaphore).
   *
   * <p>In other words, callType is "guarded by" the Kernel's semaphore,
   * ensuring the Kernel can "see" changes made to it by the UCS thread.
   *
   * @param ct
   */
  public static void setCallType(CallType ct) {
    Objects.requireNonNull(ct, OutputHelper.getInstance()
        .logToAllAndReturnMessage("Cannot set OS.callType to null",
            Level.SEVERE));
    callType = ct;
  }

  public static void switchProcess(UnprivilegedContextSwitcher ucs) {
    switchContext(ucs, CallType.SWITCH_PROCESS, ucs::addToCsRets);
  }

  public static void allocateMemory(
      UnprivilegedContextSwitcher cs,
      Consumer<Object> retSaver,
      int size) {
    switchContext(cs, CallType.ALLOCATE_MEMORY, retSaver, size);
  }

  public static void getMapping(
      UnprivilegedContextSwitcher cs,
      Consumer<Object> retSaver,
      int virtualPageNumber) {
    switchContext(cs, CallType.GET_MAPPING, retSaver, virtualPageNumber);
  }

  public static void freeMemory(
      UnprivilegedContextSwitcher cs,
      Consumer<Object> retSaver,
      int pointer,
      int size) {
    switchContext(cs, CallType.FREE_MEMORY, retSaver, pointer, size);
  }

  public static List<KernelMessage> getMessages() {
    return messages;
  }

  /**
   * This is only set for a process when that process is chosen to run next,
   * so there shouldn't be a danger of the wrong process getting them.
   *
   * @param messages
   */
  public static void setMessages(List<KernelMessage> messages) {
    OutputHelper.getInstance().getDebugLogger()
        .log(Level.INFO, "Setting OS messages to " + messages);
    OS.messages = messages;
  }

  public static List<KernelMessage> getMessagesAndClear() {
    var ret = getMessages();
    setMessages(new ArrayList<>());
    return ret;
  }

  public enum CallType {
    STARTUP_CREATE_PROCESS,
    CREATE_PROCESS,
    SWITCH_PROCESS,
    SLEEP,
    OPEN,
    WAIT_FOR_MESSAGE,
    SEND_MESSAGE,
    GET_MAPPING,
    ALLOCATE_MEMORY,
    FREE_MEMORY,
    EXIT
  }
}
