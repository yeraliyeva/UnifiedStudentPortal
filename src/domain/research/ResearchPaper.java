package domain.research;

import domain.shared.Username;

import java.time.LocalDate;
import java.util.Objects;

public final class ResearchPaper {
    private final PaperId id;
    private final String title;
    private final Username author;
    private final JournalName journal;
    private final String abstractText;
    private final int pages;
    private final String doi;
    private final LocalDate publishedDate;
    private int citations;

    public ResearchPaper(PaperId id, String title, Username author, JournalName journal,
                         String abstractText, int pages, String doi) {
        this.id = Objects.requireNonNull(id);
        this.title = Objects.requireNonNull(title);
        this.author = Objects.requireNonNull(author);
        this.journal = Objects.requireNonNull(journal);
        this.abstractText = abstractText == null ? "" : abstractText;
        this.pages = Math.max(0, pages);
        this.doi = doi;
        this.publishedDate = LocalDate.now();
        this.citations = 0;
    }

    public PaperId id() { return id; }
    public String title() { return title; }
    public Username author() { return author; }
    public JournalName journal() { return journal; }
    public String abstractText() { return abstractText; }
    public int pages() { return pages; }
    public String doi() { return doi; }
    public LocalDate publishedDate() { return publishedDate; }
    public int citations() { return citations; }

    public void cite() { citations++; }

    public int length() {
        if (pages > 0) return pages;
        return abstractText.isBlank() ? 0 : abstractText.split("\\s+").length;
    }

    @Override public String toString() {
        return "[" + id + "] \"" + title + "\" by " + author + " | " + journal
                + " | citations=" + citations + " | pages=" + pages + " | " + publishedDate;
    }
}
