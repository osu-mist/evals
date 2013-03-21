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
     * Grabs a list of PermissionRule.
     * Then it creates a map of permission rules using "status"-"role" as
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
        Iterator rulesIterator = session.createQuery("from edu.osu.cws.evals.models.PermissionRule").list().iterator();
        while (rulesIterator.hasNext()) {
            rule = (PermissionRule) rulesIterator.next();
            key = rule.getStatus()+"-"+rule.getRole();
            ruleMap.put(key, rule);
        }
        return ruleMap;
    }
}
