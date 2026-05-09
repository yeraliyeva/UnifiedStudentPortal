package communication;

import exceptions.NotResearcherException;
import interfaces.Researcher;
import interfaces.Subscriber;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A research project hosted in a journal.
 *
 * Observer pattern: subscribers are notified when a new paper is published.
 *
 * Design note: joining the project requires the user to be a Researcher —
 * otherwise NotResearcherException is thrown.
 */
public class ResearchProject {
    private static int idCounter = 1;

    private final int id;
    private final String journalName;
    private final String topic;
    private final String supervisorUsername;

    private final List<String> participantUsernames = new ArrayList<>();
    private final List<ResearchPaper> publishedPapers = new ArrayList<>();
    private final List<Subscriber> subscribers = new ArrayList<>();

    public ResearchProject(String journalName, String topic, String supervisorUsername) {
        this.id = idCounter++;
        this.journalName = journalName;
        this.topic = topic;
        this.supervisorUsername = supervisorUsername;
    }

    // ── participants ──────────────────────────────────────────────

    /**
     * Only Researcher instances may join.
     * @throws NotResearcherException if the user does not implement Researcher
     */
    public void addParticipant(Object user) throws NotResearcherException {
        if (!(user instanceof Researcher)) {
            throw new NotResearcherException(
                    "Only researchers can join a research project.");
        }
        Researcher r = (Researcher) user;
        // store username via reflection-free approach: cast to common user
        // We store just the username string for loose coupling
        String username = extractUsername(user);
        if (!participantUsernames.contains(username)) {
            participantUsernames.add(username);
        }
    }

    /** Helper: tries to get username from a user object via duck-typing approach */
    private String extractUsername(Object user) {
        try {
            return (String) user.getClass().getMethod("getUsername").invoke(user);
        } catch (Exception e) {
            return user.toString();
        }
    }

    // ── papers ────────────────────────────────────────────────────

    /** Publishes a paper and notifies all subscribers. */
    public void publishPaper(ResearchPaper paper) {
        publishedPapers.add(paper);
        notifySubscribers(paper);
    }

    // ── observer pattern ─────────────────────────────────────────

    public void subscribe(Subscriber subscriber) {
        if (!subscribers.contains(subscriber)) {
            subscribers.add(subscriber);
        }
    }

    public void unsubscribe(Subscriber subscriber) {
        subscribers.remove(subscriber);
    }

    private void notifySubscribers(ResearchPaper paper) {
        for (Subscriber s : subscribers) {
            s.notifyNewPaper(journalName, paper);
        }
    }

    // ── getters ───────────────────────────────────────────────────

    public int getId() { return id; }
    public String getJournalName() { return journalName; }
    public String getTopic() { return topic; }
    public String getSupervisorUsername() { return supervisorUsername; }
    public List<String> getParticipantUsernames() { return Collections.unmodifiableList(participantUsernames); }
    public List<ResearchPaper> getPublishedPapers() { return Collections.unmodifiableList(publishedPapers); }
    public List<Subscriber> getSubscribers() { return Collections.unmodifiableList(subscribers); }

    @Override
    public String toString() {
        return String.format("[PROJECT-%d] Journal: %s | Topic: %s | Supervisor: %s | Papers: %d",
                id, journalName, topic, supervisorUsername, publishedPapers.size());
    }
}
