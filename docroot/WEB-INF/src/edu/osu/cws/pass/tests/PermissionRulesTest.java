package edu.osu.cws.pass.tests;

import edu.osu.cws.pass.util.HibernateUtil;
import edu.osu.cws.pass.util.PermissionRules;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Test
public class PermissionRulesTest {
    PermissionRules permissionRules = new PermissionRules();

    /**
     * This setup method is run before this class gets executed in order to
     * set the Hibernate environment to TESTING. This will ensure that we use
     * the testing db for tests.
     *
     */
    @BeforeClass
    public void setUp() throws Exception {
        HibernateUtil.setEnvironment(HibernateUtil.TESTING);
        DBUnit dbunit = new DBUnit();
        dbunit.seedDatabase();
    }

    @Test(groups = {"unittest"})
    public void shouldListAllPermissionRules() {
        HashMap rules = permissionRules.list();
        assert rules.containsKey("goals-due-employee") : "Invalid key in permissions rules";
        assert rules.containsKey("goals-due-immediate-supervisor") : "Invalid key in permissions rules";
        assert rules.size() == 2 :
        "PermissionRules.list() should find all permission rules";

    }
}
