package test;

import java.util.Objects;

/**
 * A lightweight, custom assertion library for our pure-Java Test Suite.
 */
public class Assert {
    
    private static int passed = 0;
    private static int failed = 0;

    public static void assertEquals(Object expected, Object actual, String message) {
        if (!Objects.equals(expected, actual)) {
            fail(message + " | Expected: " + expected + ", Actual: " + actual);
        } else {
            passed++;
        }
    }

    public static void assertTrue(boolean condition, String message) {
        if (!condition) {
            fail(message + " | Expected: true, Actual: false");
        } else {
            passed++;
        }
    }

    public static void assertFalse(boolean condition, String message) {
        if (condition) {
            fail(message + " | Expected: false, Actual: true");
        } else {
            passed++;
        }
    }

    public static void assertNotNull(Object obj, String message) {
        if (obj == null) {
            fail(message + " | Expected: Not Null, Actual: null");
        } else {
            passed++;
        }
    }
    
    public static void assertNull(Object obj, String message) {
        if (obj != null) {
            fail(message + " | Expected: null, Actual: " + obj);
        } else {
            passed++;
        }
    }

    private static void fail(String message) {
        failed++;
        System.err.println("[FAILED] " + message);
        // We throw RuntimeException to abort the current test block
        throw new RuntimeException("Assertion Failed: " + message);
    }

    public static void reset() {
        passed = 0;
        failed = 0;
    }

    public static int getPassed() { return passed; }
    public static int getFailed() { return failed; }
}
