package communication;

import enums.PaperFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A published research paper.
 * Citations count is used for h-index calculation and comparators.
 */
public class ResearchPaper {
    private static int idCounter = 1;

    private final int id;
    private final String title;
    private final String authorUsername;
    private final String journalName;
    private final String wording;       // abstract / summary
    private int citations;
    private final LocalDate publishedDate;
    private final List<String> references = new ArrayList<>();

    public ResearchPaper(String title, String authorUsername, String journalName, String wording) {
        this.id = idCounter++;
        this.title = title;
        this.authorUsername = authorUsername;
        this.journalName = journalName;
        this.wording = wording;
        this.citations = 0;
        this.publishedDate = LocalDate.now();
    }

    public void addReference(String reference) { references.add(reference); }
    public void incrementCitations() { citations++; }

    /** Returns citation string in the requested format. */
    public String getCitation(PaperFormat format) {
        if (format == PaperFormat.BIBTEX) {
            return "@article{" + authorUsername + publishedDate.getYear() + ",\n" +
                   "  author = {" + authorUsername + "},\n" +
                   "  title  = {" + title + "},\n" +
                   "  journal= {" + journalName + "},\n" +
                   "  year   = {" + publishedDate.getYear() + "}\n" +
                   "}";
        } else {
            return "Title: " + title + " | Author: " + authorUsername +
                   " | Journal: " + journalName + " | Published: " + publishedDate;
        }
    }

    /** Article length proxy: word count of wording field. */
    public int getLength() {
        return wording == null ? 0 : wording.split("\\s+").length;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthorUsername() { return authorUsername; }
    public String getJournalName() { return journalName; }
    public String getWording() { return wording; }
    public int getCitations() { return citations; }
    public LocalDate getPublishedDate() { return publishedDate; }
    public List<String> getReferences() { return Collections.unmodifiableList(references); }

    @Override
    public String toString() {
        return String.format("[PAPER-%d] \"%s\" by %s | Journal: %s | Citations: %d | %s",
                id, title, authorUsername, journalName, citations, publishedDate);
    }
}
