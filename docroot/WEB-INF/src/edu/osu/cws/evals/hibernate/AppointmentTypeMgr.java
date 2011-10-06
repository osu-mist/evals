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
    public List list() throws Exception {
        List result;
        Session hsession = HibernateUtil.getCurrentSession();
        try {
            result = hsession.createQuery("from edu.osu.cws.evals.models.AppointmentType").list();
        } catch (Exception e){
            hsession.close();
            throw e;
        }
        return result;
    }

}
