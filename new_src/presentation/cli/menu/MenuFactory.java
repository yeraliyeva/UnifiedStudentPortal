package presentation.cli.menu;

import domain.user.Admin;
import domain.user.EmployeeResearcher;
import domain.user.Librarian;
import domain.user.Manager;
import domain.user.Student;
import domain.user.Teacher;
import domain.user.TechSupport;
import domain.user.User;

public interface MenuFactory {
    Menu menuFor(User user);
}
