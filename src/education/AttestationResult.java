package education;

/**
 * Stores attestation scores for one student in one course.
 * firstHalf  — score from attestation 1 (out of 30)
 * secondHalf — score from attestation 2 (out of 30)
 * exam       — exam score (out of 40)
 */
public class AttestationResult {
    private int firstHalf;
    private int secondHalf;
    private int exam;

    public AttestationResult(int firstHalf, int secondHalf, int exam) {
        this.firstHalf = firstHalf;
        this.secondHalf = secondHalf;
        this.exam = exam;
    }

    public int getFirstHalf() { return firstHalf; }
    public int getSecondHalf() { return secondHalf; }
    public int getExam() { return exam; }
    public int getTotal() { return firstHalf + secondHalf + exam; }

    public void setFirstHalf(int v) { this.firstHalf = v; }
    public void setSecondHalf(int v) { this.secondHalf = v; }
    public void setExam(int v) { this.exam = v; }

    @Override
    public String toString() {
        return "Attestation1=" + firstHalf + ", Attestation2=" + secondHalf +
               ", Exam=" + exam + ", Total=" + getTotal();
    }
}
