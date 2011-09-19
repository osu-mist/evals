package edu.osu.cws.pass.hibernate;

import edu.osu.cws.pass.models.BusinessCenter;
import edu.osu.cws.pass.util.HibernateUtil;
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
            Transaction tx = hsession.beginTransaction();
            result = hsession.createQuery("from edu.osu.cws.pass.models.BusinessCenter order by name").list();
            tx.commit();
        } catch (Exception e){
            hsession.close();
            throw e;
        }
        return result;
    }

}
