package edu.osu.cws.evals.hibernate;

import edu.osu.cws.evals.models.EmailType;
import edu.osu.cws.evals.util.HibernateUtil;
import org.hibernate.Session;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        String query = "from edu.osu.cws.evals.models.EmailType";
        List<EmailType> results = (List<EmailType>) session.createQuery(query).list();

        for (EmailType emailType : results) {
            typeMap.put(emailType.getType(),  emailType);
        }
        return typeMap;
    }
}
