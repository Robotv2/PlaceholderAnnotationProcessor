package fr.robotv2.placeholderannotation;

public class PAPUtil {

    private static boolean debug;

    public static void debug(boolean debug) {
        PAPUtil.debug = debug;
    }

    public static void debug(String message) {
        if(PAPUtil.debug) {
            PlaceholderAnnotationProcessor.logger().info(message);
        }
    }

}
