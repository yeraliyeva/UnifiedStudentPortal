package domain.rules;

public record EnrollmentDecision(boolean allowed, String reason) {
    public static EnrollmentDecision allow() { return new EnrollmentDecision(true, ""); }
    public static EnrollmentDecision deny(String reason) { return new EnrollmentDecision(false, reason); }
}
