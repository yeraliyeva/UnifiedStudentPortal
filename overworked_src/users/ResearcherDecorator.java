package users;

import communication.ResearchPaper;
import communication.ResearchProject;
import data.Database;
import interfaces.Researcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Decorator pattern: wraps any User to give them Researcher capabilities.
 *
 * Used for:
 * - Bachelor students who opt into research
 * - Non-professor teachers who do research
 * - EmployeeResearcher (non-teaching, non-student employee who researches)
 *
 * The wrapped user's identity (username, name) is preserved.
 * Research data is stored in this decorator, not the wrapped user.
 */
public class ResearcherDecorator implements Researcher {

    private final User wrappedUser;
    private final List<ResearchPaper> papers = new ArrayList<>();
    private final List<ResearchProject> projects = new ArrayList<>();

    public ResearcherDecorator(User wrappedUser) {
        this.wrappedUser = wrappedUser;
    }

    @Override
    public List<ResearchPaper> getMyPapers() {
        return Collections.unmodifiableList(papers);
    }

    @Override
    public void addResearchPaper(ResearchPaper paper) {
        papers.add(paper);
        System.out.println("[" + wrappedUser.getUsername() + "] Paper added: " + paper.getTitle());
    }

    @Override
    public void createResearchProject(ResearchProject project) {
        projects.add(project);
        Database.getInstance().addResearchProject(project);
        System.out.println("[" + wrappedUser.getUsername() + "] Project created: " + project.getJournalName());
    }

    @Override
    public List<ResearchProject> getResearchProjects() {
        return Collections.unmodifiableList(projects);
    }

    /** Delegate identity to the wrapped user */
    public String getUsername() {
        return wrappedUser.getUsername();
    }

    public User getWrappedUser() {
        return wrappedUser;
    }

    @Override
    public String toString() {
        return "ResearcherDecorator[" + wrappedUser.getUsername() + "]";
    }
}
