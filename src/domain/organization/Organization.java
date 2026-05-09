package domain.organization;

import domain.shared.Username;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public final class Organization {
    private final String name;
    private final Username head;
    private final Set<Username> members = new LinkedHashSet<>();

    public Organization(String name, Username head) {
        this.name = name;
        this.head = head;
        this.members.add(head);
    }

    public String name() { return name; }
    public Username head() { return head; }
    public Set<Username> members() { return Collections.unmodifiableSet(members); }
    public boolean isMember(Username u) { return members.contains(u); }
    public void addMember(Username u) { members.add(u); }

    @Override public String toString() { return "[ORG] " + name + " (head: " + head + ", members: " + members.size() + ")"; }
}
