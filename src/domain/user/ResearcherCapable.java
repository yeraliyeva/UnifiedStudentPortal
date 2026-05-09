package domain.user;

public interface ResearcherCapable {
    boolean isResearcher();
    void activateResearcher(String field);
    ResearcherProfile researcherProfile();
}
