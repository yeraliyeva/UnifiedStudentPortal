package application;

public record Result(boolean success, String message) {
    public static Result ok() { return new Result(true, ""); }
    public static Result ok(String message) { return new Result(true, message); }
    public static Result fail(String message) { return new Result(false, message); }
}
