package com.chdc.qr;

import com.chdc.qr.view.QRReaderMainFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        final QRReaderMainFrame reader = new QRReaderMainFrame();

        reader.setLocationRelativeTo(null);
        SwingUtilities.invokeLater(() -> {
            reader.setExtendedState(JFrame.MAXIMIZED_BOTH);
            reader.setVisible(true);
        });
    }
}
