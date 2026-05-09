package domain.research;

import domain.shared.Username;

import java.util.*;

public final class ResearchProject {
    private final int id;
    private final JournalName journal;
    private final String topic;
    private final Username supervisor;
    private final Set<Username> participants = new LinkedHashSet<>();
    private final List<PaperId> publishedPapers = new ArrayList<>();

    public ResearchProject(int id, JournalName journal, String topic, Username supervisor) {
        this.id = id;
        this.journal = journal;
        this.topic = topic;
        this.supervisor = supervisor;
    }

    public int id() { return id; }
    public JournalName journal() { return journal; }
    public String topic() { return topic; }
    public Username supervisor() { return supervisor; }
    public Set<Username> participants() { return Collections.unmodifiableSet(participants); }
    public List<PaperId> publishedPapers() { return Collections.unmodifiableList(publishedPapers); }

    public void addParticipant(Username u) { participants.add(u); }
    public void recordPublication(PaperId paperId) { publishedPapers.add(paperId); }

    @Override public String toString() {
        return "[PROJECT-" + id + "] " + journal + " | topic: " + topic + " | supervisor: " + supervisor
                + " | papers: " + publishedPapers.size();
    }
}
