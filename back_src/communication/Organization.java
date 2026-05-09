package communication;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Student organization.
 * Requirement: "Student organizations. Student can be a member/head."
 */
public class Organization {
    private static int idCounter = 1;

    private final int id;
    private final String name;
    private String headUsername;           // head of the organization
    private final List<String> memberUsernames = new ArrayList<>();

    public Organization(String name, String headUsername) {
        this.id = idCounter++;
        this.name = name;
        this.headUsername = headUsername;
        this.memberUsernames.add(headUsername);
    }

    public void addMember(String username) {
        if (!memberUsernames.contains(username)) {
            memberUsernames.add(username);
        }
    }

    public void removeMember(String username) {
        if (username.equals(headUsername)) {
            System.out.println("Cannot remove the head. Transfer leadership first.");
            return;
        }
        memberUsernames.remove(username);
    }

    public void setHead(String username) {
        if (memberUsernames.contains(username)) {
            this.headUsername = username;
        } else {
            System.out.println("User must be a member first.");
        }
    }

    public boolean isMember(String username) {
        return memberUsernames.contains(username);
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getHeadUsername() { return headUsername; }
    public List<String> getMemberUsernames() { return Collections.unmodifiableList(memberUsernames); }

    @Override
    public String toString() {
        return "[ORG-" + id + "] " + name + " | Head: " + headUsername +
               " | Members: " + memberUsernames.size();
    }
}
