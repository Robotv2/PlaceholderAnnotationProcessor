package fr.robotv2.placeholderannotation.util;

import org.bukkit.plugin.PluginLogger;

public class PAPDebug {

    private static boolean debug = false;

    public static void debugEnabled(boolean debug) {
        PAPDebug.debug = debug;
    }

    public static void debug(String message) {
        if(PAPDebug.debug) {
            PluginLogger.getLogger("PAP").info(message);
        }
    }
}
