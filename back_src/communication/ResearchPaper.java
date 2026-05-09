package communication;

import enums.PaperFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A published research paper.
 * Requirement: "citations, name, authors, journal, pages, date, doi etc."
 */
public class ResearchPaper {
    private static int idCounter = 1;

    private final int id;
    private final String title;
    private final String authorUsername;
    private final String journalName;
    private final String wording;       // abstract / summary
    private int citations;
    private int pages;                  // number of pages
    private String doi;                 // digital object identifier
    private final LocalDate publishedDate;
    private final List<String> references = new ArrayList<>();

    public ResearchPaper(String title, String authorUsername, String journalName, String wording) {
        this.id = idCounter++;
        this.title = title;
        this.authorUsername = authorUsername;
        this.journalName = journalName;
        this.wording = wording;
        this.citations = 0;
        this.pages = 0;
        this.publishedDate = LocalDate.now();
    }

    public ResearchPaper(String title, String authorUsername, String journalName,
                         String wording, int pages, String doi) {
        this(title, authorUsername, journalName, wording);
        this.pages = pages;
        this.doi = doi;
    }

    public void addReference(String reference) { references.add(reference); }
    public void incrementCitations() { citations++; }

    /** Returns citation string in the requested format. */
    public String getCitation(PaperFormat format) {
        if (format == PaperFormat.BIBTEX) {
            return "@article{" + authorUsername + publishedDate.getYear() + ",\n" +
                   "  author  = {" + authorUsername + "},\n" +
                   "  title   = {" + title + "},\n" +
                   "  journal = {" + journalName + "},\n" +
                   "  year    = {" + publishedDate.getYear() + "},\n" +
                   (pages > 0 ? "  pages   = {1--" + pages + "},\n" : "") +
                   (doi != null ? "  doi     = {" + doi + "}\n" : "") +
                   "}";
        } else {
            return "Title: " + title + " | Author: " + authorUsername +
                   " | Journal: " + journalName + " | Published: " + publishedDate +
                   (pages > 0 ? " | Pages: " + pages : "") +
                   (doi != null ? " | DOI: " + doi : "");
        }
    }

    /** Article length: uses pages if set, otherwise word count of abstract */
    public int getLength() {
        if (pages > 0) return pages;
        return wording == null ? 0 : wording.split("\\s+").length;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthorUsername() { return authorUsername; }
    public String getJournalName() { return journalName; }
    public String getWording() { return wording; }
    public int getCitations() { return citations; }
    public int getPages() { return pages; }
    public void setPages(int pages) { this.pages = pages; }
    public String getDoi() { return doi; }
    public void setDoi(String doi) { this.doi = doi; }
    public LocalDate getPublishedDate() { return publishedDate; }
    public List<String> getReferences() { return Collections.unmodifiableList(references); }

    @Override
    public String toString() {
        return String.format("[PAPER-%d] \"%s\" by %s | Journal: %s | Citations: %d | Pages: %d | %s",
                id, title, authorUsername, journalName, citations, pages, publishedDate);
    }
}
