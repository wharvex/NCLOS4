package com.wharvex.nclos;

import javax.swing.*;
import java.io.IOException;
import java.io.OutputStream;

public class OutputStreamExt extends OutputStream {
  private final JTextArea textArea;

  public OutputStreamExt(JTextArea textArea) {
    this.textArea = textArea;
  }

  @Override
  public void write(int b) throws IOException {
    textArea.append(String.valueOf((char) b));
    textArea.setCaretPosition(textArea.getDocument().getLength());
  }
}
