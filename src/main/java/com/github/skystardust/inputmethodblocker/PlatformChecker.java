package com.github.skystardust.inputmethodblocker;

public class PlatformChecker {

    public static Platform getPlatform() {
        String typeName = System.getProperty("os.arch");
        if (typeName.contains("64")) {
            return Platform.WIN_64;
        }
        return Platform.WIN_64;
    }

    public static enum Platform {
        WIN_64, WIN_32
    }
}
