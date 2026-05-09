package test;

import java.util.Objects;

public final class Assert {
    private static int passed = 0;
    private static int failed = 0;

    public static void equals(Object expected, Object actual, String message) {
        if (!Objects.equals(expected, actual)) fail(message + " | Expected: " + expected + ", Actual: " + actual);
        else passed++;
    }
    public static void isTrue(boolean condition, String message) {
        if (!condition) fail(message + " | Expected: true");
        else passed++;
    }
    public static void isFalse(boolean condition, String message) {
        if (condition) fail(message + " | Expected: false");
        else passed++;
    }

    private static void fail(String message) {
        failed++;
        throw new RuntimeException("Assertion failed: " + message);
    }
    public static void reset() { passed = 0; failed = 0; }
    public static int passed() { return passed; }
    public static int failed() { return failed; }
}
