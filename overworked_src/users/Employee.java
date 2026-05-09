package users;

import common.Messages;
import communication.Order;
import data.Database;
import enums.Faculty;
import enums.Gender;

import java.time.LocalDate;

/**
 * Base class for all university staff members.
 */
public abstract class Employee extends User {

    private double salary;
    private final LocalDate hireDate;
    private final String insuranceNumber;

    protected Employee(String firstName, String lastName, String username,
                       String password, Gender gender, LocalDate dateOfBirth,
                       String email, Faculty faculty, double salary,
                       LocalDate hireDate, String insuranceNumber) {
        super(firstName, lastName, username, password, gender, dateOfBirth, email, faculty);
        this.salary = salary;
        this.hireDate = hireDate;
        this.insuranceNumber = insuranceNumber;
    }

    public void addOrder(String description) {
        Order order = new Order(getUsername(), description);
        Database.getInstance().addOrder(order);
        System.out.println(Messages.fmt("employee.order.created", order));
    }

    @Override
    public void viewPersonalInfo() {
        super.viewPersonalInfo();
        System.out.println(Messages.fmt("employee.info.salary", salary));
        System.out.println(Messages.fmt("employee.info.hire", hireDate));
    }

    public double getSalary() { return salary; }
    public void setSalary(double salary) { this.salary = salary; }
    public LocalDate getHireDate() { return hireDate; }
    public String getInsuranceNumber() { return insuranceNumber; }
}
