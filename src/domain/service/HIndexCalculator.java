package domain.service;

import domain.research.HIndex;
import domain.research.ResearchPaper;

import java.util.Comparator;
import java.util.List;

public final class HIndexCalculator {
    public HIndex calculate(List<ResearchPaper> papers) {
        List<Integer> sorted = papers.stream()
                .map(ResearchPaper::citations)
                .sorted(Comparator.reverseOrder())
                .toList();
        int h = 0;
        for (int i = 0; i < sorted.size(); i++) {
            if (sorted.get(i) >= i + 1) h = i + 1;
            else break;
        }
        return new HIndex(h);
    }
}
