package edu.osu.cws.pass.hibernate;

import edu.osu.cws.pass.models.AppointmentType;
import edu.osu.cws.pass.util.HibernateUtil;
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
            Transaction tx = hsession.beginTransaction();
            result = hsession.createQuery("from edu.osu.cws.pass.models.AppointmentType").list();
            tx.commit();
        } catch (Exception e){
            hsession.close();
            throw e;
        }
        return result;
    }

}
