package infrastructure.persistence.orm.repository;

import domain.repository.ResearchProjectRepository;
import domain.research.JournalName;
import domain.research.ResearchProject;
import infrastructure.persistence.database.Database;
import infrastructure.persistence.mapper.ResearchProjectMapper;
import infrastructure.persistence.orm.Repository;

import java.util.Collection;
import java.util.Optional;

public final class OrmResearchProjectRepository implements ResearchProjectRepository {
    private final Repository<ResearchProject, Integer> repo;

    public OrmResearchProjectRepository(Database db) {
        this.repo = new Repository<>(db, "projects", new ResearchProjectMapper());
    }

    @Override public void save(ResearchProject p) { repo.save(p); }
    @Override public Optional<ResearchProject> findByJournal(JournalName journal) {
        return repo.findFirst(p -> p.journal().equals(journal));
    }
    @Override public Collection<ResearchProject> findAll() { return repo.findAll(); }
}
