package com.wharvex.nclos;

import javax.swing.*;

public class JFrameExt extends JFrame {
    private JTextArea textArea;

    public JFrameExt(String title) {
        super(title);
    }

    public JTextArea getTextArea() {
        return textArea;
    }

    public void setTextArea(JTextArea textArea) {
        this.textArea = textArea;
    }
}
