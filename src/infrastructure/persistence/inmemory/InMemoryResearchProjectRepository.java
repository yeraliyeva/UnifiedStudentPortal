package infrastructure.persistence.inmemory;

import domain.repository.ResearchProjectRepository;
import domain.research.JournalName;
import domain.research.ResearchProject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class InMemoryResearchProjectRepository implements ResearchProjectRepository {
    private final List<ResearchProject> store = new ArrayList<>();

    @Override public void save(ResearchProject p) {
        store.removeIf(x -> x.id() == p.id());
        store.add(p);
    }
    @Override public Optional<ResearchProject> findByJournal(JournalName journal) {
        return store.stream().filter(p -> p.journal().equals(journal)).findFirst();
    }
    @Override public Collection<ResearchProject> findAll() { return Collections.unmodifiableCollection(store); }
}
