package edu.osu.cws.evals.hibernate;

import edu.osu.cws.evals.models.PermissionRule;
import edu.osu.cws.evals.util.HibernateUtil;
import org.hibernate.Session;

import java.util.HashMap;
import java.util.Iterator;

public class PermissionRuleMgr {

    /**
     * Grabs a list of PermissionRule.
     * Then it creates a map of permission rules using "status"-"role" as
     * the hashmap key.
     *
     * @return ruleMap
     * @throws Exception
     */
    public static HashMap list() throws Exception {
        HashMap ruleMap = new HashMap();
        Session session = HibernateUtil.getCurrentSession();
        Iterator rulesIterator = session
                .createQuery("from edu.osu.cws.evals.models.PermissionRule").list().iterator();
        while (rulesIterator.hasNext()) {
            PermissionRule rule = (PermissionRule) rulesIterator.next();
            String key = rule.getStatus() + "-" + rule.getRole();
            ruleMap.put(key, rule);
        }
        return ruleMap;
    }
}
