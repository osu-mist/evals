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
        Employee employee = EmployeeMgr.findByOnid("cedenoj", null);

        assert employee != null : "The employee object should not be null";
        assert employee.getId() == 12345 : "Id should be valid";
        assert employee.getStatus().equals("A") : "Employee should be active";
    }

    @Test
    public void shouldOnlyAccept9DigitsForOsuid() {
        Employee employee = new Employee();

        employee.setOsuid("12345");
        assert !employee.validateOsuid();

        employee.setOsuid(" ");
        assert !employee.validateOsuid();

        employee.setOsuid("123-55-1234");
        assert !employee.validateOsuid();

        employee.setOsuid("123456789");
        assert employee.validateOsuid();

    }
}
