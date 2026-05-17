package domain.course;

public record Grade(int firstHalf, int secondHalf, int exam) {
    public static final int PASSING_TOTAL = 50;
    public static final int MIN_ATTESTATION_TOTAL = 30;

    public Grade {
        if (firstHalf < 0 || firstHalf > 30) throw new IllegalArgumentException("att1 0..30");
        if (secondHalf < 0 || secondHalf > 30) throw new IllegalArgumentException("att2 0..30");
        if (exam < 0 || exam > 40) throw new IllegalArgumentException("exam 0..40");
    }

    public int attestationTotal() { return firstHalf + secondHalf; }
    public int total() { return firstHalf + secondHalf + exam; }
    public boolean isAdmittedToExam() { return attestationTotal() >= MIN_ATTESTATION_TOTAL; }
    public boolean isPassing() { return isAdmittedToExam() && total() >= PASSING_TOTAL; }

    public String letter() {
        if (!isAdmittedToExam()) return "F";
        int t = total();
        if (t >= 90) return "A";
        if (t >= 80) return "B";
        if (t >= 70) return "C";
        if (t >= 50) return "D";
        return "F";
    }

    @Override public String toString() {
        return "att1=" + firstHalf + ", att2=" + secondHalf + ", exam=" + exam
                + ", total=" + total() + " (" + letter() + ")"
                + (isAdmittedToExam() ? "" : " [NOT_ADMITTED]");
    }
}
