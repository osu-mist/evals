package edu.osu.cws.pass.hibernate;

import edu.osu.cws.pass.models.Configuration;
import edu.osu.cws.pass.models.ModelException;
import edu.osu.cws.pass.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        try {
            configsList = list(session);
        } catch (Exception e) {
            session.close();
            throw e;
        }

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
        List<Configuration> configurations = new ArrayList<Configuration>();
        Session session = HibernateUtil.getCurrentSession();

        try {
            configurations = list(session);
        } catch (Exception e) {
            session.close();
            throw e;
        }
        return configurations;
    }

    /**
     * Retrieves a list of Admin from the database.
     *
     * @param session
     * @return
     * @throws Exception
     */
    private List<Configuration> list(Session session) throws Exception {
        Transaction tx = session.beginTransaction();
        List<Configuration> result = session.createQuery("from edu.osu.cws.pass.models.Configuration configuration " +
                "order by configuration.section, configuration.sequence").list();
        tx.commit();
        return result;
    }

    public boolean edit(int id, String value) throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        try {
            edit(id, value, session);
        } catch (Exception e) {
            session.close();
            throw e;
        }

        return true;
    }

    private void edit(int id, String value, Session session) throws Exception {
        Transaction tx = session.beginTransaction();
        Configuration configuration = (Configuration) session.get(Configuration.class, id);
        if (configuration == null) {
            throw new ModelException("Configuration Parameter not found");
        }
        configuration.setValue(value);
        session.update(configuration);
        tx.commit();
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
}
