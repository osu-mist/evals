package edu.osu.cws.pass.util;

import edu.osu.cws.pass.models.PermissionRule;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PermissionRules {

    /**
     * Uses list(session) method to grab a list of PermissionRule. Then
     * it creates a map of permission rules using "status"-"role" as
     * the hashmap key.
     *
     * @return ruleMap
     * @throws Exception
     */
    public HashMap list() throws Exception {
        HashMap ruleMap = new HashMap();
        PermissionRule rule;
        String key;
        Session session = HibernateUtil.getCurrentSession();
        try {
            Iterator rulesIterator = this.list(session).iterator();
            while (rulesIterator.hasNext()) {
                rule = (PermissionRule) rulesIterator.next();
                key = rule.getStatus()+"-"+rule.getRole();
                ruleMap.put(key, rule);
            }
        } catch (Exception e){
            session.close();
            throw e;
        }

        return ruleMap;
    }

    /**
     * Retrieves a list of PermissionRule from the database.
     *
     * @param session
     * @return
     * @throws Exception
     */
    public List list(Session session) throws Exception {
        Transaction tx = session.beginTransaction();
        List result = session.createQuery("from edu.osu.cws.pass.models.PermissionRule").list();
        tx.commit();
        return result;
    }
}
