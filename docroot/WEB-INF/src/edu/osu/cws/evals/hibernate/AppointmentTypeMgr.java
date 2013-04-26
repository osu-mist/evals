package edu.osu.cws.evals.hibernate;

import edu.osu.cws.evals.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class AppointmentTypeMgr {

    /**
     * Uses hibernate to fetch and return a list of AppointmentType hibernate POJOs.
     *
     * @return
     */
    public static List list() throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        return session.createQuery("from edu.osu.cws.evals.models.AppointmentType").list();
    }

}
