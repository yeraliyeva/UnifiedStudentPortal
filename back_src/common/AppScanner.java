package common;

import java.util.Scanner;

/**
 * Application-wide single Scanner instance.
 * Having multiple Scanner(System.in) instances causes them to compete
 * for buffered input, causing lines to be "consumed" by the wrong reader.
 */
public final class AppScanner {
    private static final Scanner INSTANCE = new Scanner(System.in);

    private AppScanner() {}

    public static Scanner get() {
        return INSTANCE;
    }
}
