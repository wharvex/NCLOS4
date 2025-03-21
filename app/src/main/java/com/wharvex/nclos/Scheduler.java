package com.wharvex.nclos;

import java.util.*;
import java.util.function.Supplier;
import java.util.logging.Level;

/**
 * The problem that we are trying to solve by building an OS is that we have
 * more than one program that we want to run, but only one CPU. We want all
 * programs to make some forward progress over some reasonable period of
 * time; we don’t want one single program to run, and no others get any run
 * time. We want some level of fairness. To get that fairness, each program
 * will get some amount of run time (called a QUANTUM). After that run time
 * is over, another program will get a turn.
 */
public class Scheduler {
  // The priority-appropriate waiting queues.
  private final ArrayList<PCB> wqRealtime;
  private final ArrayList<PCB> wqInteractive;
  private final ArrayList<PCB> wqBackground;
  private final ArrayList<KernelMessage> waitingMessages;
  private final ArrayList<PCB> waitingRecipients;

  // The sleeping queue.
  private final ArrayList<PCB> sleepingQueue;

  // The hashmap for looking up a PCB by its PID that contains all living
  // PCBs.
  private final HashMap<Integer, PCB> pcbByPidComplete;

  // The hashmap for looking up a PCB by its PID that only contains PCBs
  // waiting for a message.
  private final HashMap<Integer, PCB> pcbByPidMessageWaiters;

  // The timer, which simulates the hardware-based timer that interrupts the
  // CPU and makes switching between processes possible.
  private final Timer timer;

  // The currently running process.
  private PCB currentlyRunning;

  public Scheduler() {
    timer = new Timer("timerThread");
    wqRealtime = new ArrayList<>();
    wqInteractive = new ArrayList<>();
    wqBackground = new ArrayList<>();
    sleepingQueue = new ArrayList<>();
    pcbByPidComplete = new HashMap<>();
    pcbByPidMessageWaiters = new HashMap<>();
    waitingMessages = new ArrayList<>();
    waitingRecipients = new ArrayList<>();
  }

  public void populateTlbRand() {
    // Get random integers for zeroth and first virtual-to-physical mappings
    // in the TLB.
    int vz = RandomHelper.getVirtPageNum();
    int pz = RandomHelper.getPhysPageNum();
    int vf = RandomHelper.getVirtPageNum();
    while (vf == vz) {
      vf = RandomHelper.getVirtPageNum();
    }
    int pf = RandomHelper.getPhysPageNum();
    while (pf == pz) {
      pf = RandomHelper.getPhysPageNum();
    }

    populateTlb(vz, pz, vf, pf);
  }

  public void populateTlb(int vz, int pz, int vf, int pf) {
    // Set zeroth mapping.
    UserlandProcess.preSetOnTlb(0, 0, vz);
    UserlandProcess.preSetOnTlb(1, 0, pz);

    // Set first mapping.
    UserlandProcess.preSetOnTlb(0, 1, vf);
    UserlandProcess.preSetOnTlb(1, 1, pf);
  }

  public PCB getCurrentlyRunningSafe() {
    return preGetCurrentlyRunning().orElseThrow(() -> new RuntimeException(
        NclosLogger.logError("sched.currun was null").get()));
  }

  private List<KernelMessage> getWaitingMessagesForPCB(PCB pcb) {
    return getWaitingMessages().stream()
        .filter(km -> km.getTargetPid() == pcb.getPid()).toList();
  }

  private void handleMessages() {
    // Get the message-waiters who have messages now.
    var doneWaiters =
        getWaitingRecipients().stream()
            .filter(pcb -> !getWaitingMessagesForPCB(pcb).isEmpty())
            .toList();
    NclosLogger.logDebug(
        "Initial contents of doneWaiters -> " + doneWaiters);

    // Remove from waitingRecipients the message-waiters who have messages
    // now.
    getWaitingRecipients().removeAll(doneWaiters);
    NclosLogger.logDebug("Contents of waitingRecipients after removing " +
        "doneWaiters -> " + getWaitingRecipients());

    // Add each doneWaiter's messages to its PCB.
    doneWaiters =
        doneWaiters.stream()
            .map(pcb -> pcb.addAllToMessagesAndReturnThis(
                getWaitingMessagesForPCB(pcb)))
            .toList();
    NclosLogger.logDebug(
        "Contents of doneWaiters after adding messages to them -> " +
            doneWaiters);

    // Add all the doneWaiters to the waiting (readyToRun) queue.
    getWQ().addAll(doneWaiters);

    // Remove from waitingMessages all the messages that were waiting for the
    // now doneWaiters.
    doneWaiters.forEach(
        dw -> getWaitingMessages().removeAll(getWaitingMessagesForPCB(dw)));
    NclosLogger.logDebug("waitingMessages -> " + getWaitingMessages());
  }

  public void switchProcess(Supplier<PCB> processChooser) {
    handleMessages();

    // Add CR to WQ if CR is not null.
    // Pro-tip: Set CR to null right before calling switchProcess if you
    // don't want to give it a chance to run again this context switch. Just
    // be aware that if you have unlucky timing, this will cause the Timer
    // thread to skip a quantum if it happens to fire while CR is null.
    // https://os.cs.luc.edu/scheduling.html#preemptive-scheduling-key-terms
    preGetCurrentlyRunning().ifPresent(this::addToWQ);

    // Choose the new process to run.
    PCB chosenProcess = processChooser.get();

    // Save the chosen process' messages to OS.
    OS.setMessages(chosenProcess.getMessages());

    // Reset the PCB's messages.
    chosenProcess.setMessages(new ArrayList<>());

    // Get what will become the old currently running.
    PCB oldCurRun =
        preGetCurrentlyRunning()
            .orElse(getFromPcbByPidComplete(
                getPidByName(OS.getContextSwitcher().getThreadName())));
    NclosLogger.logDebug("oldCurRun is " + oldCurRun.getThreadName());

    // Mark oldCurRun for stopping based on whether chosenProcess ref-equals
    // oldCurRun.
    NclosLogger.logDebug("setting shouldStopAfterContextSwitch");
    oldCurRun.getUserlandProcess()
        .setShouldStopAfterContextSwitch(chosenProcess != oldCurRun);

    // Set currentlyRunning to the chosen process.
    preSetCurrentlyRunning(chosenProcess);
  }

  public synchronized Optional<PCB> getCurrentlyRunning() {
    NclosLogger.logDebugSync(ExecutionPathStage.IN, currentlyRunning);
    return Optional.ofNullable(currentlyRunning);
  }

  public synchronized void setCurrentlyRunning(PCB currentlyRunning) {
    NclosLogger.logDebugSync(ExecutionPathStage.IN, currentlyRunning);
    this.currentlyRunning = currentlyRunning;
  }

  public Optional<PCB> preGetCurrentlyRunning() {
    NclosLogger.logDebugSync(ExecutionPathStage.BEFORE_ENTER);
    var ret = getCurrentlyRunning();
    NclosLogger.logDebugSync(ExecutionPathStage.AFTER_EXIT);
    return ret;
  }

  public void preSetCurrentlyRunning(PCB currentlyRunning) {
    NclosLogger.logDebugSync(ExecutionPathStage.BEFORE_ENTER);
    setCurrentlyRunning(currentlyRunning);
    NclosLogger.logDebugSync(ExecutionPathStage.AFTER_EXIT);
  }

  public void addToWQ(PCB pcb) {
    getWQ().add(pcb);
    NclosLogger.logDebug("Added " + pcb.getThreadName() + " to wq");
    getWQ().forEach(NclosLogger::logDebug);
    NclosLogger.logDebug("Size of wq -> " + getWQ().size());
  }

  private void removeFromWQ(int idx) {
    PCB removed = getWQ().remove(idx);
    NclosLogger.logDebug("Removed " + removed.getThreadName() + " from wq");
    getWQ().forEach(NclosLogger::logDebug);
    NclosLogger.logDebug("Size of wq -> " + getWQ().size());
  }

  private List<PCB> getWQ() {
    return wqBackground;
  }

  private PCB getFromWQ(int idx) {
    return getWQ().get(idx);
  }

  public PCB getRandomProcess() {
    Random r = new Random();
    int chosenIdx = r.nextInt(getWQ().size());
    PCB chosenProcess = getFromWQ(chosenIdx);
    NclosLogger.logDebug("chos process -> " + chosenProcess.getThreadName());
    removeFromWQ(chosenIdx);
    return chosenProcess;
  }

  public int getPidByName(String name) {
    return getPcbByPidComplete().entrySet().stream()
        .filter(e -> e.getValue().getThreadName().equals(name))
        .findFirst()
        .orElseThrow(() -> new RuntimeException(
            NclosLogger.logError("no such thread name -> " + name).get()))
        .getKey();
  }

  public int getPid() {
    return getCurrentlyRunningSafe().getPid();
  }

  public void startTimer() {
    NclosLogger.logDebug("Scheduling Timer...");
    timer.schedule(
        new TimerTask() {
          @Override
          public void run() {
            NclosLogger.logDebugThread(ThreadLifeStage.STARTING);
            preGetCurrentlyRunning()
                .ifPresentOrElse(
                    // TODO: What happens if the currentlyRunning changes
                    //  here?
                    // OS will set shouldStopFromTimeout on the
                    // contextSwitcher (which is the currentlyRunning we will
                    // have gotten here) to false, which will release the
                    // Timer from its waiting loop.
                    PCB::stop,
                    () ->
                        NclosLogger.logDebug("currun is null"));
          }
        },
        1000,
        1000);
  }

  public HashMap<Integer, PCB> getPcbByPidComplete() {
    return pcbByPidComplete;
  }

  public PCB getFromPcbByPidComplete(int pid) {
    return getPcbByPidComplete().get(pid);
  }

  public void addToPcbByPidComplete(PCB pcb, int pid) {
    getPcbByPidComplete().put(pid, pcb);
    NclosLogger.logDebug(
        "Added " + pcb.getThreadName() + " to pcbByPidComplete");
    getPcbByPidComplete().forEach(
        (key, value) -> NclosLogger.logDebug(key + ", " + value));
  }

  public ArrayList<KernelMessage> getWaitingMessages() {
    return waitingMessages;
  }

  public ArrayList<PCB> getWaitingRecipients() {
    return waitingRecipients;
  }

  public void addToWaitingMessages(KernelMessage km) {
    NclosLogger.logDebug("Adding " + km + " to waitingMessages");
    getWaitingMessages().add(km);
    NclosLogger.logDebug("waitingMessages -> " + getWaitingMessages());
  }

  public void addToWaitingRecipients(PCB pcb) {
    NclosLogger.logDebug("Adding " + pcb.getThreadName() + " to " +
        "waitingRecipients");
    getWaitingRecipients().add(pcb);
    NclosLogger.logDebug("waitingRecipients -> " + getWaitingRecipients());
  }

  public PCB getFromWaitingRecipients(int idx) {
    return getWaitingRecipients().get(idx);
  }

  public KernelMessage getFromWaitingMessages(int idx) {
    return getWaitingMessages().get(idx);
  }

  public enum PriorityType {
    REALTIME,
    INTERACTIVE,
    BACKGROUND
  }
}
