package edu.osu.cws.evals.tests;

import edu.osu.cws.evals.hibernate.EmployeeMgr;
import edu.osu.cws.evals.models.Employee;
import edu.osu.cws.evals.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

@Test
public class EmployeesTest {

    EmployeeMgr employeeMgr = new EmployeeMgr();
    Transaction tx;

    @BeforeMethod
    public void setUp() throws Exception {
        DBUnit dbunit = new DBUnit();
        dbunit.seedDatabase();
        Session session = HibernateUtil.getCurrentSession();
        tx = session.beginTransaction();
    }

    @AfterMethod
    public void tearDown() throws Exception {
        tx.commit();
    }

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
    public void shouldHaveEmployeesInView() throws Exception {
        List<Employee> results = EmployeeMgr.list();
        Employee employee;

        // place a breakpoint below if you want to step through the records to make sure
        // we are getting data from the view
        for (int i = 0; i < 5; i++) {
            employee = results.get(i);
            assert employee != null;
        }
        assert results.size() > 0 : "The list of employees should not be empty";
    }

    public void shouldOnlyAccept9DigitsForOsuid() {
        Employee employee = new Employee();

        employee.setOsuid("12345");
        assert employee.validateOsuid() == false;

        employee.setOsuid(" ");
        assert employee.validateOsuid() == false;

        employee.setOsuid("123-55-1234");
        assert employee.validateOsuid() == false;

        employee.setOsuid("123456789");
        assert employee.validateOsuid() == true;

    }
}
