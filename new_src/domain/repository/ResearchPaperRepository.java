package domain.repository;

import domain.research.PaperId;
import domain.research.ResearchPaper;
import domain.shared.Username;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ResearchPaperRepository {
    void save(ResearchPaper paper);
    Optional<ResearchPaper> findById(PaperId id);
    List<ResearchPaper> findByAuthor(Username author);
    Collection<ResearchPaper> findAll();
}
