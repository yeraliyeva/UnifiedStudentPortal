package presentation.cli.menu;

import application.Result;
import application.usecase.messaging.ProcessRequest;
import domain.enums.RequestStatus;
import domain.repository.RequestRepository;
import domain.user.Dean;
import presentation.cli.Console;

import java.util.List;

public final class DeanMenu extends Menu {
    private final Dean dean;
    private final RequestRepository requests;
    private final ProcessRequest processRequest;
    private final BecomeResearcherAction becomeResearcher;
    private final ResearcherMenuExtension researcherMenu;

    public DeanMenu(Console console, Dean dean, RequestRepository requests,
                    ProcessRequest processRequest,
                    BecomeResearcherAction becomeResearcher, ResearcherMenuExtension researcherMenu) {
        super(console);
        this.dean = dean;
        this.requests = requests;
        this.processRequest = processRequest;
        this.becomeResearcher = becomeResearcher;
        this.researcherMenu = researcherMenu;
    }

    @Override protected String title() { return "=== DEAN MENU (" + dean.username() + ") ==="; }

    @Override protected List<MenuItem> items() {
        List<MenuItem> items = new java.util.ArrayList<>();
        if (!dean.isResearcher()) {
            items.add(new MenuItem("Become a researcher", () -> becomeResearcher.run(dean)));
        }
        items.add(new MenuItem("View incoming requests", this::viewIncoming));
        items.add(new MenuItem("Approve a request", () -> decide(RequestStatus.APPROVED)));
        items.add(new MenuItem("Reject a request", () -> decide(RequestStatus.NOT_APPROVED)));
        items.addAll(researcherMenu.itemsFor(dean));
        return items;
    }

    private void viewIncoming() {
        var incoming = requests.findAll().stream()
                .filter(r -> r.faculty() == dean.faculty() && r.status() == RequestStatus.ACCEPTED)
                .toList();
        if (incoming.isEmpty()) { console.println("No incoming requests."); return; }
        incoming.forEach(r -> console.println("  " + r));
    }

    private void decide(RequestStatus status) {
        viewIncoming();
        int id = console.readInt("Request ID:");
        Result r = processRequest.execute(dean.username(), id, status);
        console.println(r.message());
    }
}
