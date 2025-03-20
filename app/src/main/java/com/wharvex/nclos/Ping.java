package com.wharvex.nclos;

import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.IntStream;

public class Ping extends UserlandProcess {
  public Ping() {
    super(UUID.randomUUID().toString().substring(24), "ping");
  }

  private Optional<Integer> getPongPid() {
    return getMessages().stream()
        .filter(m -> m.getMessageType() == 1)
        .map(m -> Integer.parseInt(m.getMessageContentString()))
        .findFirst();
  }

  @Override
  void main() {
    int i = 0;
    while (true) {
      NclosLogger.logMain(
          "hello from Ping " + getDebugPid() + ", i=" + (++i) +
              ", waiting for message " + (getMessages().size() + 1));
      OS.waitForMessage(this);
      addAllToMessages(OS.getMessagesAndClear());
      IntStream.range(0, getMessages().size())
          .forEach(
              j ->
                  NclosLogger.logMain("Ping's received message " +
                      (j + 1) + " has content -> " +
                      getMessages().get(j).getMessageContentString()));
      int finalI = i;
      getPongPid()
          .ifPresent(
              p -> {
                NclosLogger.logMain("Ping sees pong's pid as: " + p);
                String messageContent =
                    "baba booey " + finalI + " from ping";
                NclosLogger.logMain("Ping sending message to pong with " +
                    "content: " + messageContent);
                OS.sendMessage(this,
                    new KernelMessage(p, 2, messageContent));
              });
      ThreadHelper.threadSleep(1000);
      cooperate();
    }
  }
}
