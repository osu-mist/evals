package edu.osu.cws.evals.hibernate;

import edu.osu.cws.evals.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class HibTest {

    /**
     * Uses hibernate to fetch and return a list of AppointmentType hibernate POJOs.
     *
     * @return
     */

    public static void main(String[] strs) throws Exception
    {
        HibernateUtil.setConfig("hibernate-luminis-dev.cfg.xml");
        for (int i = 0; i < 100; i++)
        {
            Session session = HibernateUtil.getCurrentSession();
            System.out.print(i + ": ");
            Transaction tx = session.beginTransaction();
            subMethod(session);
            tx.commit();
        }
    }

    private static void subMethod(Session session) throws Exception
    {
        Session thisSession = HibernateUtil.getCurrentSession();
        System.out.println(session == thisSession);
        if (thisSession != session)
        {
            thisSession.close();
        }
    }

}