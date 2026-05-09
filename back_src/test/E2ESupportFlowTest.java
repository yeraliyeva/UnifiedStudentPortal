package test;

import communication.Message;
import communication.Order;
import data.Database;
import enums.Faculty;
import enums.Gender;
import enums.OrderStatus;
import enums.TeacherPosition;
import users.Teacher;
import users.TechSupport;

import java.time.LocalDate;
import java.util.List;

public class E2ESupportFlowTest {

    public static void runAll() {
        TestRunner.runTest("E2E: Tech Support Flow", E2ESupportFlowTest::testSupportOrderFlow);
    }

    private static void testSupportOrderFlow() {
        Database db = Database.getInstance();

        // 1. Setup actors
        Teacher teacher = new Teacher("A", "B", "teach1", "p", Gender.MALE, LocalDate.now(), "e", Faculty.SITE, 2000, LocalDate.now(), "INS", "PhD", TeacherPosition.PROFESSOR);
        TechSupport tech = new TechSupport("T", "S", "tech1", "p", Gender.MALE, LocalDate.now(), "e", Faculty.SITE, 1500, LocalDate.now(), "INS");
        
        db.addUser(teacher);
        db.addUser(tech);

        // 2. Teacher creates an order
        teacher.addOrder("Projector is broken in room 305");
        
        List<Order> orders = db.getAllOrders();
        Assert.assertFalse(orders.isEmpty(), "Order should exist in DB");
        Order order = orders.get(0);
        Assert.assertEquals(OrderStatus.NEW, order.getStatus(), "Order status should be NEW");

        // 3. Tech Support accepts order
        order.accept(tech.getUsername());
        Assert.assertEquals(OrderStatus.ACCEPTED, order.getStatus(), "Order status should be ACCEPTED");
        Assert.assertEquals(tech.getUsername(), order.getExecutorUsername(), "Tech support username should be assigned to executor");

        // System notification to Teacher
        tech.sendMessage(order.getRequesterUsername(), "Order Accepted", "Working on it", enums.UrgencyLevel.LOW);

        // 4. Tech Support marks order as Done
        order.markDone();
        Assert.assertEquals(OrderStatus.DONE, order.getStatus(), "Order status should be DONE");

        // System notification to Teacher
        tech.sendMessage(order.getRequesterUsername(), "Order Done", "Fixed", enums.UrgencyLevel.LOW);

        // 5. Verify teacher received messages
        List<Message> teacherInbox = db.getMessagesFor("teach1");
        Assert.assertEquals(2, teacherInbox.size(), "Teacher should have received 2 messages");
        Assert.assertEquals("Order Done", teacherInbox.get(1).getSubject(), "Last message should be 'Done'");
    }
}
