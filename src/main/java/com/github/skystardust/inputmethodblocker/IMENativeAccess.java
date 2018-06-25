package com.github.skystardust.inputmethodblocker;

public class IMENativeAccess {
    public native static void activeIME();

    public native static void inactiveIME();

    public native static boolean getIMEStatus();
}
