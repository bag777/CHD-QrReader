package com.chdc.qr.ctrl;

import com.github.sarxos.webcam.util.jh.JHFilter;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class AdvancedFlipFilter extends JHFilter {
    public static final int FLIP_90CW = 4;
    public static final int FLIP_90CCW = 5;
    public static final int FLIP_180 = 6;
    private int operation;

    public AdvancedFlipFilter(int operation) {
        this.setOperation(operation);
    }

    public AdvancedFlipFilter() {
        this(FLIP_90CW);
    }

    public int getOperation() {
        return operation;
    }

    public void setOperation(int operation) {
        this.operation = operation;
    }

    @Override
    public BufferedImage filter(BufferedImage src, BufferedImage dest) {
        double radians = getRadians();
        int newWidth = src.getHeight();
        int newHeight = src.getWidth();

        BufferedImage rotate = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = rotate.createGraphics();
        int x = (newWidth - src.getWidth()) / 2;
        int y = (newHeight - src.getHeight()) / 2;
        AffineTransform at = new AffineTransform();
        at.setToRotation(radians, x + (src.getWidth() / 2), y + (src.getHeight() / 2));
        at.translate(x, y);
        g2d.setTransform(at);
        g2d.drawImage(src, 0, 0, null);
        g2d.dispose();
        return rotate;
    }

    public double getRadians() {
        switch(this.operation) {
            case FLIP_90CW:
                return Math.toRadians(90);
            case FLIP_180:
                return Math.toRadians(180);
            case FLIP_90CCW:
                return Math.toRadians(270);
            default:
                return Math.toRadians(0);
        }
    }

    public String toString() {
        switch(this.operation) {
            case FLIP_90CW:
                return "Rotate 90";
            case FLIP_90CCW:
                return "Rotate -90";
            case FLIP_180:
                return "Rotate 180";
            default:
                return "Flip";
        }
    }
}
