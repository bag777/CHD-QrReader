package com.chdc.qr.lib;

import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;

@Slf4j
public class CustomDispatcher {
    public static void invokeAndWait(Runnable runner) {
        try {
            SwingUtilities.invokeAndWait(runner);
        } catch (InterruptedException e) {
            log.error("InvokeAndWait error", e);
        } catch (InvocationTargetException e) {
            log.error("InvokeAndWait error", e);
        }
    }
}
