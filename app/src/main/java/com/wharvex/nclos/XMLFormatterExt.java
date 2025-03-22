package com.wharvex.nclos;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.logging.LogRecord;
import java.util.logging.XMLFormatter;

public class XMLFormatterExt extends XMLFormatter {

  @Override
  public String format(LogRecord record) {
    StringBuilder sb = new StringBuilder(500);
    sb.append("<record>\n");

    final Instant instant = record.getInstant();

    sb.append("  <date>");
    DateTimeFormatter.ISO_INSTANT.formatTo(instant, sb);
    sb.append("</date>\n");

//    sb.append("  <millis>");
//    sb.append(instant.toEpochMilli());
//    sb.append("</millis>\n");


//    final int nanoAdjustment = instant.getNano() % 1000_000;
//    sb.append("  <nanos>");
//    sb.append(nanoAdjustment);
//    sb.append("</nanos>\n");

    sb.append("  <sequence>");
    sb.append(record.getSequenceNumber());
    sb.append("</sequence>\n");

    String name = record.getLoggerName();
    if (name != null) {
      sb.append("  <logger>");
      escape(sb, name);
      sb.append("</logger>\n");
    }

    sb.append("  <level>");
    escape(sb, record.getLevel().toString());
    sb.append("</level>\n");

    sb.append("  <prevClass>");
    escape(sb, ((LogRecordExt) record).getPrevClass());
    sb.append("</prevClass>\n");

    sb.append("  <prevMethod>");
    escape(sb, ((LogRecordExt) record).getPrevMethod());
    sb.append("</prevMethod>\n");

    if (record.getSourceClassName() != null) {
      sb.append("  <class>");
      escape(sb, record.getSourceClassName());
      sb.append("</class>\n");
    }

    if (record.getSourceMethodName() != null) {
      sb.append("  <method>");
      escape(sb, record.getSourceMethodName());
      sb.append("</method>\n");
    }

    sb.append("  <thread>");
    sb.append(((LogRecordExt) record).getThreadName());
    sb.append("</thread>\n");

    if (record.getMessage() != null) {
      // Format the message string and its accompanying parameters.
      String message = formatMessage(record);
      sb.append("  <message>");
      escape(sb, message);
      sb.append("</message>");
      sb.append("\n");
    }

    // If the message is being localized, output the key, resource
    // bundle name, and params.
    ResourceBundle bundle = record.getResourceBundle();
    try {
      if (bundle != null && bundle.getString(record.getMessage()) != null) {
        sb.append("  <key>");
        escape(sb, record.getMessage());
        sb.append("</key>\n");
        sb.append("  <catalog>");
        escape(sb, record.getResourceBundleName());
        sb.append("</catalog>\n");
      }
    } catch (Exception ex) {
      // The message is not in the catalog.  Drop through.
    }

    Object parameters[] = record.getParameters();
    //  Check to see if the parameter was not a messagetext format
    //  or was not null or empty
    if (parameters != null && parameters.length != 0
        && record.getMessage().indexOf('{') == -1) {
      for (Object parameter : parameters) {
        sb.append("  <param>");
        try {
          escape(sb, parameter.toString());
        } catch (Exception ex) {
          sb.append("???");
        }
        sb.append("</param>\n");
      }
    }

    if (record.getThrown() != null) {
      // Report on the state of the throwable.
      Throwable th = record.getThrown();
      sb.append("  <exception>\n");
      sb.append("    <message>");
      escape(sb, th.toString());
      sb.append("</message>\n");
      StackTraceElement trace[] = th.getStackTrace();
      for (StackTraceElement frame : trace) {
        sb.append("    <frame>\n");
        sb.append("      <class>");
        escape(sb, frame.getClassName());
        sb.append("</class>\n");
        sb.append("      <method>");
        escape(sb, frame.getMethodName());
        sb.append("</method>\n");
        // Check for a line number.
        if (frame.getLineNumber() >= 0) {
          sb.append("      <line>");
          sb.append(frame.getLineNumber());
          sb.append("</line>\n");
        }
        sb.append("    </frame>\n");
      }
      sb.append("  </exception>\n");
    }

    sb.append("</record>\n");
    return sb.toString();
  }

  // Append to the given StringBuilder an escaped version of the
  // given text string where XML special characters have been escaped.
  // For a null string we append "<null>"
  private void escape(StringBuilder sb, String text) {
    if (text == null) {
      text = "<null>";
    }
    for (int i = 0; i < text.length(); i++) {
      char ch = text.charAt(i);
      if (ch == '<') {
        sb.append("&lt;");
      } else if (ch == '>') {
        sb.append("&gt;");
      } else if (ch == '&') {
        sb.append("&amp;");
      } else {
        sb.append(ch);
      }
    }
  }
}
