package com.wharvex.nclos;

import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

public class SimpleFormatterExt extends SimpleFormatter {

  @Override
  public String format(LogRecord record) {
    return formatMessage(record) + "\n\n";
  }
}
