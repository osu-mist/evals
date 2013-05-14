package edu.osu.cws.evals.hibernate;

import edu.osu.cws.evals.models.BusinessCenter;
import edu.osu.cws.evals.util.HibernateUtil;
import org.hibernate.Session;

import java.util.List;

public class BusinessCenterMgr {

    /**
     * Uses hibernate to fetch and return a list of BusinessCenter hibernate POJOs.
     *
     * @return
     */
    public static List<BusinessCenter> list() throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        return session.createQuery("from edu.osu.cws.evals.models.BusinessCenter order by name").list();
    }

}
