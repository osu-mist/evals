package edu.osu.cws.pass.tests;

import edu.osu.cws.pass.models.Employee;
import edu.osu.cws.pass.util.Employees;
import edu.osu.cws.pass.util.HibernateUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test
public class EmployeesTest {

    Employees employees = new Employees();

    /**
     * This setup method is run before this class gets executed in order to
     * set the Hibernate environment to TESTING. This will ensure that we use
     * the testing db for tests.
     *
     */
    @BeforeClass
    public void setUp() {
        HibernateUtil.setEnvironment(HibernateUtil.TESTING);
    }

    /**
     * Given a username, it should be able to find the active employee.
     *
     */
    @Test(groups = {"unittest"})
    public void shouldFindAUserByUsername() {
        Employee employee = employees.findByOnid("cedenoj");

        assert employee != null : "The employee object should not be null";
        assert employee.getId() == 12345 : "Id should be valid";
        assert employee.isActive() : "Employee should be active";


    }
}
