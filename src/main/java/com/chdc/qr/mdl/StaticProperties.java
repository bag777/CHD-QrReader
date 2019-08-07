package com.chdc.qr.mdl;

import java.util.concurrent.atomic.AtomicInteger;

public class StaticProperties {
    private static final class Creator {
        private static StaticProperties inst = new StaticProperties();
    }

    private static int date = 20180811;
    private static AtomicInteger odr = new AtomicInteger(0);

    private StaticProperties() {
    }

    public static StaticProperties getInstance() {
        return Creator.inst;
    }

    public static int getDate() {
        return date;
    }

    public static void setDate(int date) {
        StaticProperties.date = date;
    }

    public static void setOdr(int order) {
        odr.set(order);
    }

    public static int getNextOdr() {
        return odr.incrementAndGet();
    }
}
