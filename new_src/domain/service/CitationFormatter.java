package domain.service;

import domain.enums.PaperFormat;
import domain.research.ResearchPaper;

public final class CitationFormatter {
    public String format(ResearchPaper paper, PaperFormat format) {
        return switch (format) {
            case BIBTEX -> bibtex(paper);
            case PLAIN_TEXT -> plain(paper);
        };
    }

    private String plain(ResearchPaper p) {
        StringBuilder sb = new StringBuilder();
        sb.append("Title: ").append(p.title())
          .append(" | Author: ").append(p.author())
          .append(" | Journal: ").append(p.journal())
          .append(" | Published: ").append(p.publishedDate());
        if (p.pages() > 0) sb.append(" | Pages: ").append(p.pages());
        if (p.doi() != null) sb.append(" | DOI: ").append(p.doi());
        return sb.toString();
    }

    private String bibtex(ResearchPaper p) {
        StringBuilder sb = new StringBuilder();
        sb.append("@article{").append(p.author()).append(p.publishedDate().getYear()).append(",\n")
          .append("  author  = {").append(p.author()).append("},\n")
          .append("  title   = {").append(p.title()).append("},\n")
          .append("  journal = {").append(p.journal()).append("},\n")
          .append("  year    = {").append(p.publishedDate().getYear()).append("}");
        if (p.pages() > 0) sb.append(",\n  pages   = {1--").append(p.pages()).append("}");
        if (p.doi() != null) sb.append(",\n  doi     = {").append(p.doi()).append("}");
        sb.append("\n}");
        return sb.toString();
    }
}
