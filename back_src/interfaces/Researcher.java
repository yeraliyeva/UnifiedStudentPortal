package interfaces;

import communication.ResearchPaper;
import communication.ResearchProject;

import java.util.Comparator;
import java.util.List;

/**
 * Any user who can do research work.
 * Implemented directly by Teacher (professor) and GraduateStudent,
 * and via decorator (ResearcherDecorator) for others.
 */
public interface Researcher {
    List<ResearchPaper> getMyPapers();
    void addResearchPaper(ResearchPaper paper);
    void createResearchProject(ResearchProject project);
    List<ResearchProject> getResearchProjects();

    /** Prints papers sorted by the given comparator */
    default void printPapers(Comparator<ResearchPaper> comparator) {
        getMyPapers().stream()
                .sorted(comparator)
                .forEach(System.out::println);
    }

    /** H-index: largest h such that h papers have >= h citations */
    default int calculateHIndex() {
        List<Integer> citations = getMyPapers().stream()
                .map(ResearchPaper::getCitations)
                .sorted(Comparator.reverseOrder())
                .toList();
        int h = 0;
        for (int i = 0; i < citations.size(); i++) {
            if (citations.get(i) >= i + 1) h = i + 1;
            else break;
        }
        return h;
    }
}
