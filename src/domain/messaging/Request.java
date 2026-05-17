package domain.messaging;

import domain.enums.Faculty;
import domain.enums.HelpType;
import domain.enums.RequestStatus;
import domain.enums.UrgencyLevel;
import domain.shared.Username;

import java.time.LocalDateTime;

public final class Request {
    private final int id;
    private final Username requester;
    private final String title;
    private final HelpType type;
    private final Faculty faculty;
    private final UrgencyLevel urgency;
    private final String additionalInfo;
    private final LocalDateTime createdAt;
    private RequestStatus status = RequestStatus.PENDING;

    public Request(int id, Username requester, String title, HelpType type, Faculty faculty,
                   UrgencyLevel urgency, String additionalInfo) {
        this(id, requester, title, type, faculty, urgency, additionalInfo, LocalDateTime.now());
    }

    public Request(int id, Username requester, String title, HelpType type, Faculty faculty,
                   UrgencyLevel urgency, String additionalInfo, LocalDateTime createdAt) {
        this.id = id;
        this.requester = requester;
        this.title = title;
        this.type = type;
        this.faculty = faculty;
        this.urgency = urgency;
        this.additionalInfo = additionalInfo;
        this.createdAt = createdAt;
    }

    public int id() { return id; }
    public Username requester() { return requester; }
    public String title() { return title; }
    public HelpType type() { return type; }
    public Faculty faculty() { return faculty; }
    public UrgencyLevel urgency() { return urgency; }
    public String additionalInfo() { return additionalInfo; }
    public LocalDateTime createdAt() { return createdAt; }
    public RequestStatus status() { return status; }

    public void changeStatus(RequestStatus s) { this.status = s; }

    @Override public String toString() {
        return "[REQ-" + id + "/" + status + "] " + title + " (" + type + ") by " + requester;
    }
}
