package infrastructure.persistence.inmemory;

import domain.repository.ResearchPaperRepository;
import domain.research.PaperId;
import domain.research.ResearchPaper;
import domain.shared.Username;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class InMemoryResearchPaperRepository implements ResearchPaperRepository {
    private final Map<PaperId, ResearchPaper> store = new LinkedHashMap<>();

    @Override public void save(ResearchPaper p) { store.put(p.id(), p); }
    @Override public Optional<ResearchPaper> findById(PaperId id) { return Optional.ofNullable(store.get(id)); }
    @Override public List<ResearchPaper> findByAuthor(Username author) {
        return store.values().stream().filter(p -> p.author().equals(author)).toList();
    }
    @Override public Collection<ResearchPaper> findAll() { return Collections.unmodifiableCollection(store.values()); }
}
