package com.wharvex.nclos;

import java.util.Optional;

public class StackFrameWrapper {
  private final int depth;

  public StackFrameWrapper(int depth) {
    this.depth = depth;
  }

  private Optional<StackWalker.StackFrame> getFrame() {
    return StackWalker.getInstance()
        .walk(stream -> stream.skip(depth).findFirst());
  }

  public String getClassName() {
    return getFrame().map(StackWalker.StackFrame::getClassName)
        .orElse("unknown");
  }

  public String getMethodName() {
    return getFrame().map(StackWalker.StackFrame::getMethodName)
        .orElse("unknown");
  }
}
