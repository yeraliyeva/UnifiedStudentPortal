package presentation.cli.menu;

import application.Result;
import application.usecase.research.CreateResearchProject;
import application.usecase.research.GenerateCitation;
import application.usecase.research.PublishPaper;
import application.usecase.research.SubscribeToJournal;
import application.usecase.research.UnsubscribeFromJournal;
import domain.enums.PaperFormat;
import domain.repository.ResearchPaperRepository;
import domain.research.PaperId;
import domain.research.ResearchPaper;
import domain.user.ResearcherCapable;
import domain.user.User;
import presentation.cli.Console;

import java.util.ArrayList;
import java.util.List;

public final class ResearcherMenuExtension {
    private final Console console;
    private final PublishPaper publishPaper;
    private final CreateResearchProject createProject;
    private final SubscribeToJournal subscribe;
    private final UnsubscribeFromJournal unsubscribe;
    private final GenerateCitation generateCitation;
    private final ResearchPaperRepository papers;

    public ResearcherMenuExtension(Console console, PublishPaper publishPaper,
                                   CreateResearchProject createProject,
                                   SubscribeToJournal subscribe,
                                   UnsubscribeFromJournal unsubscribe,
                                   GenerateCitation generateCitation,
                                   ResearchPaperRepository papers) {
        this.console = console;
        this.publishPaper = publishPaper;
        this.createProject = createProject;
        this.subscribe = subscribe;
        this.unsubscribe = unsubscribe;
        this.generateCitation = generateCitation;
        this.papers = papers;
    }

    public List<MenuItem> itemsFor(User user) {
        List<MenuItem> items = new ArrayList<>();
        if (!(user instanceof ResearcherCapable rc)) return items;
        if (!rc.isResearcher()) return items;
        items.add(new MenuItem("View research cabinet", () -> renderCabinet(user)));
        items.add(new MenuItem("Publish a paper", () -> publishInteractive(user)));
        items.add(new MenuItem("Create a research project", () -> createProjectInteractive(user)));
        items.add(new MenuItem("Subscribe to a journal", () -> subscribeInteractive(user)));
        items.add(new MenuItem("Unsubscribe from a journal", () -> unsubscribeInteractive(user)));
        items.add(new MenuItem("View my subscriptions", () -> renderSubscriptions(rc)));
        items.add(new MenuItem("Generate citation for a paper", () -> citeInteractive()));
        return items;
    }

    private void renderCabinet(User user) {
        ResearcherCapable rc = (ResearcherCapable) user;
        console.println("\n=== RESEARCH CABINET ===");
        console.println("Field: " + rc.researcherProfile().field());
        List<ResearchPaper> mine = papers.findByAuthor(user.username());
        console.println("Papers: " + mine.size());
        mine.forEach(p -> console.println("  " + p));
    }

    private void publishInteractive(User user) {
        String title = console.readLine("Paper title:");
        String journal = console.readLine("Journal name:");
        String abs = console.readLine("Abstract:");
        int pages = console.readInt("Pages (0 if unknown):");
        String doi = console.readLine("DOI (or empty):");
        Result r = publishPaper.execute(user, title, journal, abs, Math.max(pages, 0), doi.isBlank() ? null : doi);
        console.println(r.message());
    }

    private void createProjectInteractive(User user) {
        String journal = console.readLine("Journal name:");
        String topic = console.readLine("Topic:");
        Result r = createProject.execute(user, journal, topic);
        console.println(r.message());
    }

    private void subscribeInteractive(User user) {
        String journal = console.readLine("Journal name:");
        Result r = subscribe.execute(user, journal);
        console.println(r.message());
    }

    private void unsubscribeInteractive(User user) {
        String journal = console.readLine("Journal name:");
        Result r = unsubscribe.execute(user, journal);
        console.println(r.message());
    }

    private void renderSubscriptions(ResearcherCapable rc) {
        var subs = rc.researcherProfile().subscribedJournals();
        if (subs.isEmpty()) { console.println("No subscriptions."); return; }
        console.println("\n=== MY SUBSCRIPTIONS ===");
        subs.forEach(s -> console.println("  - " + s));
    }

    private void citeInteractive() {
        int id = console.readInt("Paper ID:");
        if (id <= 0) { console.println("Invalid ID."); return; }
        int fmt = console.readInt("Format: 1=Plain  2=BibTeX:");
        PaperFormat format = (fmt == 2) ? PaperFormat.BIBTEX : PaperFormat.PLAIN_TEXT;
        Result r = generateCitation.execute(new PaperId(id), format);
        console.println(r.message());
    }
}
