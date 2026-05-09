package presentation.cli;

public interface Console {
    String readLine(String prompt);
    int readInt(String prompt);
    void println(String text);
    void println();
}
