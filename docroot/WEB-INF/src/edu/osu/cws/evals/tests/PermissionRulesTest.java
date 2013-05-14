package edu.osu.cws.evals.tests;

import edu.osu.cws.evals.hibernate.PermissionRuleMgr;
import edu.osu.cws.evals.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;

@Test
public class PermissionRulesTest {

    Transaction tx;

    /**
     * This setup method is run before this class gets executed in order to
     * set the Hibernate environment to TESTING. This will ensure that we use
     * the testing db for tests.
     *
     */
    @BeforeClass
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

    @Test(groups = {"unittest"})
    public void shouldListAllPermissionRules() throws Exception {
        HashMap rules = PermissionRuleMgr.list();
        assert rules.containsKey("goalsDue-employee") : "Invalid key in permissions rules";
        assert rules.containsKey("goalsDue-immediate-supervisor") : "Invalid key in permissions rules";
        assert rules.size() == 2 :
        "PermissionRuleMgr.list() should find all permission rules";

    }
}
