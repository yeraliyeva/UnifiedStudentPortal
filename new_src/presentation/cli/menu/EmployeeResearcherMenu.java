package presentation.cli.menu;

import domain.user.EmployeeResearcher;
import presentation.cli.Console;

import java.util.ArrayList;
import java.util.List;

public final class EmployeeResearcherMenu extends Menu {
    private final EmployeeResearcher researcher;
    private final BecomeResearcherAction becomeResearcher;
    private final ResearcherMenuExtension researcherMenu;

    public EmployeeResearcherMenu(Console console, EmployeeResearcher researcher,
                                  BecomeResearcherAction becomeResearcher,
                                  ResearcherMenuExtension researcherMenu) {
        super(console);
        this.researcher = researcher;
        this.becomeResearcher = becomeResearcher;
        this.researcherMenu = researcherMenu;
    }

    @Override protected String title() { return "=== EMPLOYEE RESEARCHER MENU (" + researcher.username() + ") ==="; }

    @Override protected List<MenuItem> items() {
        List<MenuItem> items = new ArrayList<>();
        if (!researcher.isResearcher()) {
            items.add(new MenuItem("Become a researcher", () -> becomeResearcher.run(researcher)));
        }
        items.addAll(researcherMenu.itemsFor(researcher));
        return items;
    }
}
