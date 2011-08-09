package edu.osu.cws.pass.tests;

import edu.osu.cws.pass.hibernate.EmployeeMgr;
import edu.osu.cws.pass.models.Employee;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

@Test
public class EmployeesTest {

    EmployeeMgr employeeMgr = new EmployeeMgr();

    /**
     * Given a username, it should be able to find the active employee.
     *
     */
    @Test(groups = {"unittest"})
    public void shouldFindAUserByUsername() throws Exception {
        Employee employee = employeeMgr.findByOnid("cedenoj", null);

        assert employee != null : "The employee object should not be null";
        assert employee.getId() == 12345 : "Id should be valid";
        assert employee.getStatus().equals("A") : "Employee should be active";


    }

    /**
     * Tests that the employees view is not empty
     * Before you run this test method make sure that the beforeMehtod in this class is commented out.
     */
    public void shouldHaveEmployeesInView() {
        List<Employee> results = employeeMgr.list();
        Employee employee;

        // place a breakpoint below if you want to step through the records to make sure
        // we are getting data from the view
        for (int i = 0; i < 5; i++) {
            employee = results.get(i);
            assert employee != null;
        }
        assert results.size() > 0 : "The list of employees should not be empty";
    }
}
