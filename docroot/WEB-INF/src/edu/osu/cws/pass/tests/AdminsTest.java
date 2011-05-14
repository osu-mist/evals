package edu.osu.cws.pass.tests;

import edu.osu.cws.pass.models.Admin;
import edu.osu.cws.pass.util.Admins;
import edu.osu.cws.pass.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.testng.annotations.*;

import java.util.List;

@Test
public class AdminsTest {
    Admins admins = new Admins();

    /**
     * This setup method is run before this class gets executed in order to
     * set the Hibernate environment to TESTING. This will ensure that we use
     * the testing db for tests.
     *
     */
    @BeforeMethod
    public void setUp() {
        HibernateUtil.setEnvironment(HibernateUtil.TESTING);
        DBUnit dbunit = new DBUnit();
        try {
            dbunit.seedDatabase();
        } catch (Exception e) {}
    }

    public void shouldListAdmins() {
        List adminsList = admins.list();
        assert adminsList.size() == 2 : "Invalid list of admins";
        assert ((Admin) adminsList.get(0)).getId() == 1 : "Invalid admin in list";
        assert ((Admin) adminsList.get(1)).getId() == 2 : "Invalid admin in list";

    }
}
