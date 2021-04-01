package edu.osu.cws.evals.tests;

import edu.osu.cws.evals.hibernate.AppointmentTypeMgr;
import edu.osu.cws.evals.models.AppointmentType;
import edu.osu.cws.evals.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

@Test
public class AppointmentTypesTest {

    @BeforeMethod
    public void initializeObjects() throws Exception {
        DBUnit dbunit = new DBUnit();
        dbunit.seedDatabase();
    }

    @Test(groups = {"unittest"})
    public void testList() throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();
        List types = AppointmentTypeMgr.list();
        assert types.size() == 1 : "Invalid number of appointment types.";
        AppointmentType type = (AppointmentType) types.get(0);
        assert type.getName().equals("Classified") : "Invalid appointment type";
        tx.commit();
    }
}
