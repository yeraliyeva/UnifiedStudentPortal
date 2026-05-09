package interfaces;

import communication.ResearchPaper;

/**
 * Observer pattern — users who subscribe to research journals.
 */
public interface Subscriber {
    String getUsername();
    void notifyNewPaper(String journalName, ResearchPaper paper);
}
