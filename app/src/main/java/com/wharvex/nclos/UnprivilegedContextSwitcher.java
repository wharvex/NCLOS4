package com.wharvex.nclos;


import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;

/**
 * MAINLAND
 *
 * <p>An Unprivileged Context Switcher is an entity that can perform a
 * context switch, but is not allowed to execute in "privileged mode"
 * vis-a-vis the CPU. Which is to say, it can perform a context switch, but
 * is not the Kernel.
 *
 * <p>This would be considered part of Userland were it not for the fact that we will consider the
 * Bootloader a UCS, which I did not think of as part of Userland.
 */
public interface UnprivilegedContextSwitcher extends Stoppable {
  List<Object> getCsRets();

  List<KernelMessage> getMessages();

  default Object getFromCsRets(int idx) {
    return getCsRets().get(idx);
  }

  default void addToCsRets(Object ret) {
    getCsRets().add(ret);
    NclosLogger.logDebug("Saved " + ret + " to rets of " + getThreadName());
  }

  default void setContextSwitchRet(Consumer<Object> retSaver, Object ret) {
    retSaver.accept(ret);
  }

  @Override
  default boolean isStopped() {
    // We can add UCS-specific stuff here.
    return Stoppable.super.isStopped();
  }

  default boolean isDone() {
    return !getThread().isAlive();
  }
}
