package edu.osu.cws.pass.util;

import edu.osu.cws.pass.models.Admin;
import edu.osu.cws.pass.models.AppraisalStep;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class Admins {

    /**
     * Uses list(session) method to grab a list of admins. Then
     * it creates a map of admins using "pidm"as the key and the
     * admin object as the value.
     *
     * @return ruleMap
     */
    public HashMap<Integer, Admin> list() {
        HashMap<Integer, Admin> admins = new HashMap<Integer, Admin>();
        Session session = HibernateUtil.getCurrentSession();
        for (Admin admin : this.list(session)) {
            admins.put(admin.getEmployee().getId(),  admin);
        }
        return admins;
    }

    /**
     * Retrieves a list of PermissionRule from the database.
     *
     * @param session
     * @return
     * @throws org.hibernate.HibernateException
     */
    public List<Admin> list(Session session) throws HibernateException {
        Transaction tx = session.beginTransaction();
        List<Admin> result = session.createQuery("from edu.osu.cws.pass.models.Admin").list();
        tx.commit();
        return result;
    }
}
