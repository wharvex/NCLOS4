package com.wharvex.nclos;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class VirtMemTestA extends UserlandProcess {
  private final List<Integer> fileDescriptors = new ArrayList<>();
  private final List<Integer> allocationIndices = new ArrayList<>();

  public VirtMemTestA() {
    super(UUID.randomUUID().toString().substring(24), "virtMemA");
  }

  public List<Integer> getFileDescriptors() {
    return fileDescriptors;
  }

  public void addToFileDescriptors(int id) {
    getFileDescriptors().add(id);
  }

  public int getFromFileDescriptors(int idx) {
    return getFileDescriptors().get(idx);
  }

  @Override
  void main() {
    int i = 0;
    int allocationSizeInPages = 5;
    byte writeByte = 33;
    int virtualAddress = -1;
    while (true) {
      NclosLogger.logDebug("i = " + (++i));
      switch (i) {
        case 1:
          // Create swapfile.
          NclosLogger.logDebug("Creating/opening swapfile.");
          OS.open(this, id -> this.addToFileDescriptors((int) id),
              "file swap");
          break;
        case 2:
          // Print swapfile FD.
          // TODO: Error handling.
          NclosLogger.logMain("Swapfile FD -> " + getFromFileDescriptors(0));
          break;
        case 3:
          // Lazy allocate a reasonable amount.
          NclosLogger.logMain(
              "Attempting to allocate " + allocationSizeInPages +
                  " pages of memory.");
          OS.allocateMemory(
              this,
              idx -> allocationIndices.add((int) idx),
              allocationSizeInPages * OS.getPageSize());
          virtualAddress = allocationIndices.getFirst();
          if (virtualAddress >= 0) {
            NclosLogger.logMain(
                "successfully allocated " + allocationSizeInPages +
                    " pages of memory starting at virtual address " +
                    virtualAddress);
          } else {
            NclosLogger.logMain("failed to allocate.");
          }
          break;
        case 4:
          // Write.
          if (virtualAddress >= 0) {
            NclosLogger.logMain("attempting to write " + writeByte +
                " to virtual address " + virtualAddress);
            write(virtualAddress, writeByte);
          } else {
            NclosLogger.logMain("not attempting write");
          }
          break;
        case 5:
          // Read.
          if (virtualAddress >= 0) {
            NclosLogger.logMain("attempting to read from virtual address "
                + virtualAddress);
            byte readByte = read(virtualAddress);
            NclosLogger.logMain("read " + readByte + " from virtual address "
                + virtualAddress + ", expected " + writeByte);
          } else {
            NclosLogger.logMain("not attempting read");
          }
          break;
        case 6:
          // TODO: Free.
          break;
        case 7:
          // TODO: Test more allocation/read/write scenarios.
          break;

        default:
          NclosLogger.logMain("done testing.");
      }
      ThreadHelper.threadSleep(1000);
      cooperate();
    }
  }
}
