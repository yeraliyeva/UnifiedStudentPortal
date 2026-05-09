package common;

import communication.ResearchPaper;

import java.util.Comparator;

/**
 * Comparator strategies for sorting research papers.
 * Used with Researcher.printPapers(Comparator).
 */
public final class PaperComparators {

    private PaperComparators() {}

    /** Sort by number of citations descending */
    public static final Comparator<ResearchPaper> BY_CITATIONS =
            Comparator.comparingInt(ResearchPaper::getCitations).reversed();

    /** Sort by article length (word count) descending */
    public static final Comparator<ResearchPaper> BY_LENGTH =
            Comparator.comparingInt(ResearchPaper::getLength).reversed();

    /** Sort by published date descending (newest first) */
    public static final Comparator<ResearchPaper> BY_DATE =
            Comparator.comparing(ResearchPaper::getPublishedDate).reversed();

    /** Sort by title alphabetically */
    public static final Comparator<ResearchPaper> BY_TITLE =
            Comparator.comparing(ResearchPaper::getTitle);
}
