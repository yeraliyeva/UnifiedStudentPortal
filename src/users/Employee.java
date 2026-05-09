package users;

import communication.Order;
import data.Database;
import enums.Faculty;
import enums.Gender;

import java.time.LocalDate;

/**
 * Base class for all university staff members.
 * Adds salary, hire date, and the ability to create tech support orders.
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

    /** Creates a tech-support order on behalf of this employee. */
    public void addOrder(String description) {
        Order order = new Order(getUsername(), description);
        Database.getInstance().addOrder(order);
        System.out.println("Order created: " + order);
    }

    @Override
    public void viewPersonalInfo() {
        super.viewPersonalInfo();
        System.out.println("Salary  : " + salary);
        System.out.println("HireDate: " + hireDate);
    }

    public double getSalary() { return salary; }
    public void setSalary(double salary) { this.salary = salary; }
    public LocalDate getHireDate() { return hireDate; }
    public String getInsuranceNumber() { return insuranceNumber; }
}
