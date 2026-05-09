package infrastructure.persistence.orm.repository;

import domain.repository.ResearchPaperRepository;
import domain.research.PaperId;
import domain.research.ResearchPaper;
import domain.shared.Username;
import infrastructure.persistence.database.Database;
import infrastructure.persistence.mapper.ResearchPaperMapper;
import infrastructure.persistence.orm.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public final class OrmResearchPaperRepository implements ResearchPaperRepository {
    private final Repository<ResearchPaper, PaperId> repo;

    public OrmResearchPaperRepository(Database db) {
        this.repo = new Repository<>(db, "papers", new ResearchPaperMapper());
    }

    @Override public void save(ResearchPaper p) { repo.save(p); }
    @Override public Optional<ResearchPaper> findById(PaperId id) { return repo.findById(id); }
    @Override public List<ResearchPaper> findByAuthor(Username author) {
        return repo.whereEq("author", author.value()).list();
    }
    @Override public Collection<ResearchPaper> findAll() { return repo.findAll(); }
}
