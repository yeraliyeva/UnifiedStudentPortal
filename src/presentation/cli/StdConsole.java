package presentation.cli;

import java.util.Scanner;

public final class StdConsole implements Console {
    private final Scanner scanner = new Scanner(System.in);

    @Override public String readLine(String prompt) {
        if (prompt != null && !prompt.isEmpty()) System.out.print(prompt + " ");
        return scanner.hasNextLine() ? scanner.nextLine().trim() : "";
    }

    @Override public int readInt(String prompt) {
        try { return Integer.parseInt(readLine(prompt)); }
        catch (NumberFormatException e) { return -1; }
    }

    @Override public void println(String text) { System.out.println(text); }
    @Override public void println() { System.out.println(); }
}
