package com.wharvex.nclos;

public class TwoStackFrameWrappers {
  private final StackFrameWrapper first;
  private final StackFrameWrapper second;

  public TwoStackFrameWrappers(int firstDepth, int secondDepth) {
    first = new StackFrameWrapper(firstDepth);
    second = new StackFrameWrapper(secondDepth);
  }

  public String getFirstClassName() {
    return first.getClassName();
  }

  public String getFirstMethodName() {
    return first.getMethodName();
  }

  public String getSecondClassName() {
    return second.getClassName();
  }

  public String getSecondMethodName() {
    return second.getMethodName();
  }
}
