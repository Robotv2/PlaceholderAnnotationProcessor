package fr.robotv2.placeholderannotation;

public class PAPDebug {

    private static boolean debug = false;

    public static void debugEnabled(boolean debug) {
        PAPDebug.debug = debug;
    }

    public static void debug(String message) {
        if(PAPDebug.debug) {
            PlaceholderAnnotationProcessor.logger().info(message);
        }
    }
}
