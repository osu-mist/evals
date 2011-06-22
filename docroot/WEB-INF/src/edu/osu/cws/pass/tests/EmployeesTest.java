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
     * Given a username, it should be able to find the active employee.
     *
     */
    @Test(groups = {"unittest"})
    public void shouldFindAUserByUsername() throws Exception {
        Employee employee = employees.findByOnid("cedenoj");

        assert employee != null : "The employee object should not be null";
        assert employee.getId() == 12345 : "Id should be valid";
        assert employee.isActive() : "Employee should be active";


    }
}
