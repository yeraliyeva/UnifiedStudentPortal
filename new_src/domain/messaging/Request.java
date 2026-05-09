package domain.messaging;

import domain.enums.Faculty;
import domain.enums.HelpType;
import domain.enums.RequestStatus;
import domain.enums.UrgencyLevel;
import domain.shared.Username;

public final class Request {
    private final int id;
    private final Username requester;
    private final HelpType type;
    private final Faculty faculty;
    private final UrgencyLevel urgency;
    private final String additionalInfo;
    private RequestStatus status = RequestStatus.PENDING;

    public Request(int id, Username requester, HelpType type, Faculty faculty, UrgencyLevel urgency, String additionalInfo) {
        this.id = id;
        this.requester = requester;
        this.type = type;
        this.faculty = faculty;
        this.urgency = urgency;
        this.additionalInfo = additionalInfo;
    }

    public int id() { return id; }
    public Username requester() { return requester; }
    public HelpType type() { return type; }
    public Faculty faculty() { return faculty; }
    public UrgencyLevel urgency() { return urgency; }
    public String additionalInfo() { return additionalInfo; }
    public RequestStatus status() { return status; }

    public void changeStatus(RequestStatus s) { this.status = s; }

    @Override public String toString() {
        return "[REQ-" + id + "/" + status + "] " + type + " by " + requester + " (" + urgency + ")";
    }
}
