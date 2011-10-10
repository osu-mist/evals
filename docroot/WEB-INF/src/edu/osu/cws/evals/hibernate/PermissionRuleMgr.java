package edu.osu.cws.evals.hibernate;

import edu.osu.cws.evals.models.PermissionRule;
import edu.osu.cws.evals.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class PermissionRuleMgr {

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
        Iterator rulesIterator = this.list(session).iterator();
        while (rulesIterator.hasNext()) {
            rule = (PermissionRule) rulesIterator.next();
            key = rule.getStatus()+"-"+rule.getRole();
            ruleMap.put(key, rule);
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
        List result = session.createQuery("from edu.osu.cws.evals.models.PermissionRule").list();
        return result;
    }
}
