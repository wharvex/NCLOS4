package com.wharvex.nclos;

import java.io.IOException;
import java.util.logging.FileHandler;

public class FileHandlerExt extends FileHandler {
  public FileHandlerExt() throws IOException, SecurityException {
  }

  public FileHandlerExt(String pattern)
      throws IOException, SecurityException {
    super(pattern);
  }

  public FileHandlerExt(String pattern, boolean append)
      throws IOException, SecurityException {
    super(pattern, append);
  }

  public FileHandlerExt(String pattern, int limit, int count)
      throws IOException, SecurityException {
    super(pattern, limit, count);
  }

  public FileHandlerExt(String pattern, int limit, int count, boolean append)
      throws IOException, SecurityException {
    super(pattern, limit, count, append);
  }

  public FileHandlerExt(String pattern, long limit, int count,
                        boolean append)
      throws IOException {
    super(pattern, limit, count, append);
  }
}
