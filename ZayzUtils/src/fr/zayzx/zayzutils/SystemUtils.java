package fr.zayzx.zayzutils;

public final class SystemUtils {

    private SystemUtils() {}

    public static String os() {
        return System.getProperty("os.name");
    }

    public static String javaVersion() {
        return System.getProperty("java.version");
    }

    public static long totalMemoryMB() {
        return Runtime.getRuntime().totalMemory() / (1024 * 1024);
    }

    public static long freeMemoryMB() {
        return Runtime.getRuntime().freeMemory() / (1024 * 1024);
    }

    public static String userHome() {
        return System.getProperty("user.home");
    }
}
