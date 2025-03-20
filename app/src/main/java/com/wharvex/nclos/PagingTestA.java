package com.wharvex.nclos;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
      NclosLogger.logMain("hello from PagingTestA, i=" + (++i));
      switch (i) {
        case 1:
          // Allocate.
          NclosLogger.logMain("PagingTestA attempting to allocate " +
              allocationSizeInPages + " pages of memory.");
          OS.allocateMemory(
              this,
              idx -> allocationIndices.add((int) idx),
              allocationSizeInPages * OS.getPageSize());
          virtualAddress = allocationIndices.getFirst();
          if (virtualAddress >= 0) {
            NclosLogger.logMain(
                "PagingTestA allocated " + allocationSizeInPages +
                    " pages of memory starting at virtual address " +
                    virtualAddress);
          } else {
            NclosLogger.logMain(
                "PagingTestA failed to allocate because virtualAddress < 0");
          }
          break;
        case 2:
          // Write.
          if (virtualAddress >= 0) {
            NclosLogger.logMain("PagingTestA attempting to write " +
                writeByte + " to virtual address " + virtualAddress);
            write(virtualAddress, writeByte);
          } else {
            NclosLogger.logMain("PagingTestA not attempting write");
          }
          break;
        case 3:
          // Read.
          if (virtualAddress >= 0) {
            NclosLogger.logMain("PagingTestA attempting to read from " +
                "virtual address " + virtualAddress);
            byte readByte = read(virtualAddress);
            NclosLogger.logMain("PagingTestA read " + readByte +
                " from " + virtualAddress + ", expected " + writeByte);
          } else {
            NclosLogger.logMain("PagingTestA not attempting read");
          }
          break;
        case 4:
          // TODO: Free.
          break;
        case 5:
          // TODO: Test more allocation/read/write scenarios.
          break;
        default:
          NclosLogger.logMain("PagingTestA done testing.");
      }
      NclosLogger.logDebug("allocationIndices: " + allocationIndices);
      ThreadHelper.threadSleep(1000);
      cooperate();
    }
  }
}
