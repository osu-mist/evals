package edu.osu.cws.evals.hibernate;

import edu.osu.cws.evals.models.Appraisal;
import edu.osu.cws.evals.models.PermissionRule;
import edu.osu.cws.evals.portlet.ActionHelper;
import edu.osu.cws.evals.util.EvalsUtil;
import edu.osu.cws.evals.util.HibernateUtil;
import org.hibernate.Session;

import java.util.HashMap;
import java.util.Iterator;

public class PermissionRuleMgr {

    /**
     * Grabs a list of PermissionRule.
     * Then it creates a map of permission rules using "status"-"role"-"appointmentType" as
     * the hashmap key. Appointment type can be either: "Default", "Classified", "Classified IT"
     * or "Professional Faculty".
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
            String appointmentType = rule.getAppointmentType().replace(" ", "");
            String key = rule.getStatus() + "-" + rule.getRole() + "-" + appointmentType;
            ruleMap.put(key, rule);
        }
        return ruleMap;
    }

    /**
     * Returns the permission rule from the map. It first looks for a permission rule specific to the appointment type
     * of the appraisal's job. If that is not found, it uses the permission rule for the Default appointment type.
     * The permission rule map is cached and some of the calling code modifies the permission rule, that's why the
     * returned permission rule object is a clone of the entry found in the map.
     *
     * @param permissionRuleMap
     * @param appraisal
     * @param role
     * @return
     * @throws Exception
     */
    public static PermissionRule getPermissionRule(HashMap<String, PermissionRule> permissionRuleMap,
                                                   Appraisal appraisal, String role) throws Exception {
        String status = appraisal.getStatus();
        // Checking for null status, sometimes the appraisal passed in didn't have a status set for some reason
        if (status != null) {
            //Permission rules for statuses “archivedCompleted” and “archivedClose” are the same as “completed” and
            // “closed” respectively
            if (status.contains("archived")) {
                status = status.replace("archived", "").toLowerCase();
            }

            // The permission rules of "Overdue" and "Due" are the same. "Overdue" status are not present in the db.
            status = status.replace("Overdue", "Due");
        }
        String appointmentType = appraisal.getJob().getAppointmentType().replace(" ", "");
        String actionKeyPrefix = status + "-" + role + "-";

        // First check if there are appointment type specific permission rules. If not, use the default permission rule
        PermissionRule rule = permissionRuleMap.get(actionKeyPrefix + appointmentType);
        if (rule == null) {
            rule = permissionRuleMap.get(actionKeyPrefix + "Default");
        }

        if (rule != null) {
            // Get the permission rule from the cache map and clone it. If we modify or set any properties
            // in the original cached permission rule, the modifications are saved on the cached object.
            return (PermissionRule) rule.clone();
        }

        // check if the role was an admin type. If the specific admin role didn't match, check the default "admin" role
        if (EvalsUtil.isOneOfAdminRoles(role)) {
            return getPermissionRule(permissionRuleMap, appraisal, ActionHelper.ROLE_ADMINISTRATOR);
        }

        return null;
    }
}
