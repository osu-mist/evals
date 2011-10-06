package edu.osu.cws.evals.hibernate;

import edu.osu.cws.evals.models.EmailType;
import edu.osu.cws.evals.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * Created by IntelliJ IDEA.
 * User: luf
 * Date: 7/5/11
 * Time: 9:17 AM
 * To change this template use File | Settings | File Templates.
 */

import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class EmailTypeMgr {

    /**
     * Returns a map of EmailType objects using the type as the key in the map.
     *
     * @return
     * @throws Exception
     */
    public static Map<String, EmailType>  getMap() throws Exception {
        HashMap<String, EmailType> typeMap = new HashMap<String, EmailType    >();
        Session session = HibernateUtil.getCurrentSession();
        try {
            String query = "from edu.osu.cws.evals.models.EmailType";
            List<EmailType> results = (List<EmailType>) session.createQuery(query).list();

            for (EmailType emailType : results) {
                typeMap.put(emailType.getType(),  emailType);
            }
        } catch (Exception e) {
            session.close();
            throw e;
        }
        return typeMap;
    }
}
