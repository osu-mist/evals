package edu.osu.cws.pass.tests;

import edu.osu.cws.pass.hibernate.EmployeeMgr;
import edu.osu.cws.pass.models.Employee;
import org.testng.annotations.Test;

@Test
public class EmployeesTest {

    EmployeeMgr employeeMgr = new EmployeeMgr();

    /**
     * Given a username, it should be able to find the active employee.
     *
     */
    @Test(groups = {"unittest"})
    public void shouldFindAUserByUsername() throws Exception {
        Employee employee = employeeMgr.findByOnid("cedenoj");

        assert employee != null : "The employee object should not be null";
        assert employee.getId() == 12345 : "Id should be valid";
        assert employee.getStatus().equals("A") : "Employee should be active";


    }
}
