package edu.osu.cws.pass.util;

import edu.osu.cws.pass.models.Admin;
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
    public HashMap<Integer, Admin> list() throws Exception {
        HashMap<Integer, Admin> admins = new HashMap<Integer, Admin>();
        Session session = HibernateUtil.getCurrentSession();
        try {
            for (Admin admin : this.list(session)) {
                admins.put(admin.getEmployee().getId(),  admin);
            }
        } catch (Exception e) {
            session.close();
            throw e;
        }
        return admins;
    }

    /**
     * Retrieves a list of PermissionRule from the database.
     *
     * @param session
     * @return
     * @throws Exception
     */
    public List<Admin> list(Session session) throws Exception {
        Transaction tx = session.beginTransaction();
        List<Admin> result = session.createQuery("from edu.osu.cws.pass.models.Admin").list();
        tx.commit();
        return result;
    }
}
