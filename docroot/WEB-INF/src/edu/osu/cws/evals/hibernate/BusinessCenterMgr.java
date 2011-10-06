package edu.osu.cws.evals.hibernate;

import edu.osu.cws.evals.models.BusinessCenter;
import edu.osu.cws.evals.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class BusinessCenterMgr {

    /**
     * Uses hibernate to fetch and return a list of BusinessCenter hibernate POJOs.
     *
     * @return
     */
    public List<BusinessCenter> list() throws Exception {
        List<BusinessCenter> result;
        Session hsession = HibernateUtil.getCurrentSession();
        try {
            result = hsession.createQuery("from edu.osu.cws.evals.models.BusinessCenter order by name").list();
        } catch (Exception e){
            hsession.close();
            throw e;
        }
        return result;
    }

}
