package com.chdc.qr.lib;

import java.util.prefs.Preferences;

public class PrefUtils {
    public static void set(String key, String value) {
        try {
            Preferences pref = Preferences.userNodeForPackage(PrefUtils.class);
            pref.put(key, value);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void set(Object clazz, String key, String value) {
        try {
            Preferences pref = Preferences.userNodeForPackage(clazz.getClass());
            pref.put(key, value);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static String get(String key, String def) {
        try {
            Preferences pref = Preferences.userNodeForPackage(PrefUtils.class);
            return pref.get(key, def);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static String get(Object clazz, String key, String def) {
        try {
            Preferences pref = Preferences.userNodeForPackage(clazz.getClass());
            return pref.get(key, def);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
