package me.gnat008.perworldinventory;

import java.util.logging.Logger;

/**
 * Static logger for PerWorldInventory.
 */
public final class PwiLogger {

    private static Logger logger;
    private static boolean useDebug;

    private PwiLogger() {
    }

    public static void setLogger(Logger logger) {
        PwiLogger.logger = logger;
    }

    public static void setUseDebug(boolean useDebug) {
        PwiLogger.useDebug = useDebug;
    }

    public static void severe(String message) {
        logger.severe(message);
    }

    public static void severe(String message, Throwable cause) {
        logger.severe(message + " " + formatThrowable(cause));
        cause.printStackTrace();
    }

    public static void warning(String message) {
        logger.warning(message);
    }

    public static void warning(String message, Throwable cause) {
        logger.warning(message + " " + formatThrowable(cause));
        cause.printStackTrace();
    }

    public static void info(String message) {
        logger.info(message);
    }

    public static void info(String message, Throwable cause) {
        logger.info(message + " " + formatThrowable(cause));
        cause.printStackTrace();
    }

    public static void debug(String message) {
        if (useDebug) {
            logger.info("[DEBUG] " + message);
        }
    }

    public static void debug(String message, Throwable cause) {
        if (useDebug) {
            debug(message + " " + formatThrowable(cause));
            cause.printStackTrace();
        }
    }

    private static String formatThrowable(Throwable throwable) {
        return "[" + throwable.getClass().getSimpleName() + "] " + throwable.getMessage();
    }
}
