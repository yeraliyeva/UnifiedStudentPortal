package application.usecase.research;

import application.Result;
import domain.enums.PaperFormat;
import domain.repository.ResearchPaperRepository;
import domain.research.PaperId;
import domain.research.ResearchPaper;
import domain.service.CitationFormatter;

public final class GenerateCitation {
    private final ResearchPaperRepository papers;
    private final CitationFormatter formatter;

    public GenerateCitation(ResearchPaperRepository papers, CitationFormatter formatter) {
        this.papers = papers;
        this.formatter = formatter;
    }

    public Result execute(PaperId paperId, PaperFormat format) {
        ResearchPaper paper = papers.findById(paperId).orElse(null);
        if (paper == null) return Result.fail("Paper not found.");
        return Result.ok(formatter.format(paper, format));
    }
}
