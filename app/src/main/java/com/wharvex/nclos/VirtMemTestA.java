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
      OutputHelper.getInstance().getMainOutputLogger()
          .log(Level.INFO, "VirtMemTestA says: " +
              getDebugPid() + " (times printed: " + (++i) + ")");
      switch (i) {
        case 1:
          // Create swapfile.
          OutputHelper.getInstance().getMainOutputLogger().log(Level.INFO,
              "VirtMemTestA says: I'm creating/opening the swapfile.");
          OS.open(this, id -> this.addToFileDescriptors((int) id),
              "file swap");
          break;
        case 2:
          // Print swapfile FD.
          // TODO: Error handling.
          OutputHelper.getInstance().getMainOutputLogger().log(Level.INFO,
              "VirtMemTestA says: Swapfile FD: " +
                  getFromFileDescriptors(0));
          break;
        case 3:
          // Lazy allocate a reasonable amount.
          OutputHelper.getInstance().getMainOutputLogger()
              .log(Level.INFO,
                  "VirtMemTestA says: attempting to allocate "
                      + allocationSizeInPages
                      + " pages of memory.");
          OS.allocateMemory(
              this,
              idx -> allocationIndices.add((int) idx),
              allocationSizeInPages * OS.getPageSize());
          virtualAddress = allocationIndices.getFirst();
          if (virtualAddress >= 0) {
            OutputHelper.getInstance().getMainOutputLogger()
                .log(Level.INFO,
                    "VirtMemTestA says: successfully allocated "
                        + allocationSizeInPages
                        + " pages of memory starting at virtual address "
                        + virtualAddress);
          } else {
            OutputHelper.getInstance().getMainOutputLogger()
                .log(Level.INFO,
                    "VirtMemTestA says: failed to allocate.");
          }
          break;
        case 4:
          // Write.
          if (virtualAddress >= 0) {
            OutputHelper.getInstance().getMainOutputLogger()
                .log(Level.INFO,
                    "VirtMemTestA says: attempting to write "
                        + writeByte
                        + " to virtual address "
                        + virtualAddress);
            write(virtualAddress, writeByte);
          } else {
            OutputHelper.getInstance().getMainOutputLogger()
                .log(Level.INFO,
                    "VirtMemTestA says: not attempting case 4 write due to" +
                        " case 3 allocation failure.");
          }
          break;
        case 5:
          // Read.
          if (virtualAddress >= 0) {
            OutputHelper.getInstance().getMainOutputLogger()
                .log(Level.INFO,
                    "VirtMemTestA says: attempting to read from virtual address "
                        + virtualAddress);
            byte readByte = read(virtualAddress);
            OutputHelper.getInstance().getMainOutputLogger()
                .log(Level.INFO,
                    "VirtMemTestA says: read "
                        + readByte
                        + " from virtual address "
                        + virtualAddress + "; expected " + writeByte);
          } else {
            OutputHelper.getInstance().getMainOutputLogger()
                .log(Level.INFO,
                    "VirtMemTestA says: not attempting case 5 read due to" +
                        " case 3 allocation failure.");
          }
          break;
        case 6:
          // TODO: Free.
          break;
        case 7:
          // TODO: Test more allocation/read/write scenarios.
          break;

        default:
          OutputHelper.getInstance().getMainOutputLogger()
              .log(Level.INFO,
                  "VirtMemTestA says: done testing.");
      }
      ThreadHelper.threadSleep(1000);
      cooperate();
    }
  }
}
