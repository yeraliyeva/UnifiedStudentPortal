package domain.repository;

import domain.research.JournalName;
import domain.research.ResearchProject;

import java.util.Collection;
import java.util.Optional;

public interface ResearchProjectRepository {
    void save(ResearchProject project);
    Optional<ResearchProject> findByJournal(JournalName journal);
    Collection<ResearchProject> findAll();
}
