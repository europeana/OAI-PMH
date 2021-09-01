package eu.europeana.oaipmh.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Utility class to gather memory (RAM) usage statistics
 * Source was taken from https://stackoverflow.com/a/49408781
 */
public final class MemoryUtils {

    private static final long MEGABYTE_FACTOR = 1024L * 1024L;
    private static final String MB = "MB";
    private static final DecimalFormat ROUNDED_DOUBLE_DECIMALFORMAT;

    static {
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.ENGLISH);
        otherSymbols.setDecimalSeparator('.');
        otherSymbols.setGroupingSeparator(',');
        ROUNDED_DOUBLE_DECIMALFORMAT = new DecimalFormat("####0.0", otherSymbols);
        ROUNDED_DOUBLE_DECIMALFORMAT.setGroupingUsed(false);
    }

    private MemoryUtils() {
        /// empty constructor to prevent initialization
    }

    public static long getMaxMemoryJVM() {
        return Runtime.getRuntime().maxMemory();
    }

    public static long getFreeMemoryJVM() {
        return Runtime.getRuntime().freeMemory();
    }

    public static long getTotalMemoryJVM() {
        return Runtime.getRuntime().totalMemory();
    }

    public static long getFreeMemorySystem() {
        return getFreeMemoryJVM() + getMaxMemoryJVM() - getTotalMemoryJVM();
    }

    public static String getMaxMemoryJVMInMB() {
        double maxMiB = bytesToMB(getMaxMemoryJVM());
        return String.format("%s %s", ROUNDED_DOUBLE_DECIMALFORMAT.format(maxMiB), MB);
    }

    public static String getFreeMemoryJVMInMB() {
        double freeMiB = bytesToMB(getFreeMemoryJVM());
        return String.format("%s %s", ROUNDED_DOUBLE_DECIMALFORMAT.format(freeMiB), MB);
    }

    public static String getTotalMemoryJVMInMB() {
        double totalMiB = bytesToMB(getTotalMemoryJVM());
        return String.format("%s %s", ROUNDED_DOUBLE_DECIMALFORMAT.format(totalMiB), MB);
    }

    public static String getFreeMemorySystemInMB() {
        double usedMiB = bytesToMB(getFreeMemorySystem());
        return String.format("%s %s", ROUNDED_DOUBLE_DECIMALFORMAT.format(usedMiB), MB);
    }

    public static double getPercentageUsed() {
        return ((double) getTotalMemoryJVM() / getMaxMemoryJVM()) * 100;
    }

    public static String getPercentageUsedFormatted() {
        double usedPercentage = getPercentageUsed();
        return ROUNDED_DOUBLE_DECIMALFORMAT.format(usedPercentage) + "%";
    }

    private static double bytesToMB(long bytes) {
        return ((double) bytes / MEGABYTE_FACTOR);
    }

}

