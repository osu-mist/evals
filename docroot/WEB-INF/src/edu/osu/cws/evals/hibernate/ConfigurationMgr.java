package edu.osu.cws.evals.hibernate;

import edu.osu.cws.evals.models.Configuration;
import edu.osu.cws.evals.models.ModelException;
import edu.osu.cws.evals.util.HibernateUtil;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.sql.Timestamp;
import java.util.*;

public class ConfigurationMgr {

    /**
     * Returns a hashmap of Configurations using the configuration name as the map key.
     *
     * @return
     * @throws Exception
     */
    public Map<String, Configuration> mapByName() throws Exception {
        HashMap<String, Configuration> configs = new HashMap<String, Configuration>();
        List<Configuration> configsList = new ArrayList<Configuration>();
        Session session = HibernateUtil.getCurrentSession();

        configsList = list(session);
        for (Configuration configuration : configsList) {
            configs.put(configuration.getName(), configuration);
        }
        return configs;
    }

    /**
     * Uses list(session) method to grab a list of configurations.
     *
     * @throws Exception
     * @return
     */
    public List<Configuration> list() throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        return list(session);
    }

    /**
     * Retrieves a list of Admin from the database.
     *
     * @param session
     * @return
     * @throws Exception
     */
    private List<Configuration> list(Session session) throws Exception {
        List<Configuration> result = session.createQuery("from edu.osu.cws.evals.models.Configuration configuration " +
                "order by configuration.section, configuration.sequence").list();
        return result;
    }

    public boolean edit(int id, String value) throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        edit(id, value, session);
        return true;
    }

    private void edit(int id, String value, Session session) throws Exception {
        Configuration configuration = (Configuration) session.get(Configuration.class, id);
        if (configuration == null) {
            throw new ModelException("Configuration Parameter not found");
        }
        configuration.setValue(value);
        session.update(configuration);
    }

    /**
     * returns a map of all the configurations using the name as the key.
     * @return
     */
    public static Map<String, Configuration> getMap()
    {
        Map<String, Configuration> configMap = new HashMap();
        return configMap;
    }

    /**
     * Returns the Timestamp of the last time the context cache data was modified in the db. If the
     * value is not found in the db, calls ConfigurationMgr.updateContextTimestamp and returns its value.
     *
     * @return
     * @throws Exception
     */
    public static Timestamp getContextLastUpdate() throws Exception {
        Timestamp contextLastUpdate;
        Session session = HibernateUtil.getCurrentSession();

        List<Object> results = session.getNamedQuery("configuration.getContextDatetime")
                .setMaxResults(1)
                .list();
        if (!results.isEmpty()) {
            contextLastUpdate = (Timestamp) results.get(0);
        } else {
            contextLastUpdate = new Timestamp(ConfigurationMgr.updateContextTimestamp().getTime());
        }

        return contextLastUpdate;
    }

    /**
     * Updates the context_datetime. This is called only after adding/editing admins, reviewers or
     * configuration values through the admin interface.
     *
     * @return
     * @throws Exception
     */
    public static Date updateContextTimestamp() throws Exception {
        Session session = HibernateUtil.getCurrentSession();

        Date newTimestamp = new Date();
        Query query = session.getNamedQuery("configuration.updateContextDatetime")
                .setTimestamp("now", newTimestamp);
        query.executeUpdate();

        return newTimestamp;
    }
}
