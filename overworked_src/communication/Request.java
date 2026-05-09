package communication;

import enums.Faculty;
import enums.HelpType;
import enums.RequestStatus;
import enums.UrgencyLevel;

import java.time.LocalDate;

/**
 * A formal request from a user to the administration.
 *
 * Design note: removed HelpForm (paper/electronic) as it added no real logic.
 */
public class Request {
    private static int idCounter = 1;

    private final int id;
    private final String requesterUsername;
    private final HelpType type;
    private final Faculty faculty;
    private final UrgencyLevel urgency;
    private String additionalInfo;
    private RequestStatus status;
    private final LocalDate createdDate;

    public Request(String requesterUsername, HelpType type, Faculty faculty,
                   UrgencyLevel urgency, String additionalInfo) {
        this.id = idCounter++;
        this.requesterUsername = requesterUsername;
        this.type = type;
        this.faculty = faculty;
        this.urgency = urgency;
        this.additionalInfo = additionalInfo;
        this.status = RequestStatus.PENDING;
        this.createdDate = LocalDate.now();
    }

    public void setStatus(RequestStatus status) { this.status = status; }
    public void setAdditionalInfo(String info) { this.additionalInfo = info; }

    public int getId() { return id; }
    public String getRequesterUsername() { return requesterUsername; }
    public HelpType getType() { return type; }
    public Faculty getFaculty() { return faculty; }
    public UrgencyLevel getUrgency() { return urgency; }
    public String getAdditionalInfo() { return additionalInfo; }
    public RequestStatus getStatus() { return status; }
    public LocalDate getCreatedDate() { return createdDate; }

    @Override
    public String toString() {
        return String.format("[REQ-%d] %s | Type: %s | Status: %s | Urgency: %s | Created: %s",
                id, requesterUsername, type, status, urgency, createdDate);
    }
}
