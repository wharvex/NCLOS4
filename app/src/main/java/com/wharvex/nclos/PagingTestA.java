package com.wharvex.nclos;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class PagingTestA extends UserlandProcess {

  private final List<Integer> allocationIndices = new ArrayList<>();

  public PagingTestA() {
    super(UUID.randomUUID().toString().substring(24), "pagingA");
  }

  @Override
  void main() {
    int i = 0;
    int allocationSizeInPages = 5;
    byte writeByte = 33;
    int virtualAddress = -1;
    while (true) {
      OutputHelper.getInstance().getMainOutputLogger().log(Level.INFO,
          "Hello from PagingTestA " + getDebugPid() + " (times printed: " +
              (++i) + ")");
      switch (i) {
        case 1:
          // Allocate.
          OutputHelper.getInstance().getMainOutputLogger().log(Level.INFO,
              "PagingTestA attempting to allocate " + allocationSizeInPages +
                  " pages of memory.");
          OS.allocateMemory(
              this,
              idx -> allocationIndices.add((int) idx),
              allocationSizeInPages * OS.getPageSize());
          virtualAddress = allocationIndices.getFirst();
          if (virtualAddress >= 0) {
            OutputHelper.getInstance().getMainOutputLogger().log(Level.INFO,
                "PagingTestA successfully allocated " +
                    allocationSizeInPages +
                    " pages of memory starting at virtual address " +
                    virtualAddress);
          } else {
            OutputHelper.getInstance().getMainOutputLogger()
                .log(Level.SEVERE,
                    "PagingTestA failed to allocate.");
          }
          break;
        case 2:
          // Write.
          if (virtualAddress >= 0) {
            OutputHelper.getInstance().getMainOutputLogger().log(Level.INFO,
                "PagingTestA attempting to write " + writeByte +
                    " to virtual address " + virtualAddress);
            write(virtualAddress, writeByte);
          } else {
            OutputHelper.getInstance().getMainOutputLogger().log(Level.INFO,
                "PagingTestA not attempting case 2 write due to case 1" +
                    " allocation failure.");
          }
          break;
        case 3:
          // Read.
          if (virtualAddress >= 0) {
            OutputHelper.getInstance().getMainOutputLogger().log(Level.INFO,
                "PagingTestA attempting to read from virtual address " +
                    virtualAddress);
            byte readByte = read(virtualAddress);
            OutputHelper.getInstance().getMainOutputLogger()
                .log(Level.INFO, "PagingTestA read " +
                    readByte + " from virtual address " + virtualAddress +
                    "; expected " + writeByte);
          } else {
            OutputHelper.getInstance().getMainOutputLogger().log(Level.INFO,
                "PagingTestA not attempting case 3 read due to case 1" +
                    " allocation failure.");
          }
          break;
        case 4:
          // TODO: Free.
          break;
        case 5:
          // TODO: Test more allocation/read/write scenarios.
          break;
        default:
          OutputHelper.getInstance().getMainOutputLogger()
              .log(Level.INFO, "PagingTestA done testing.");
      }
      OutputHelper.getInstance().getDebugLogger()
          .log(Level.INFO, "allocationIndices: " + allocationIndices);
      ThreadHelper.threadSleep(1000);
      cooperate();
    }
  }
}
