package edu.osu.cws.evals.tests;

import edu.osu.cws.evals.util.HibernateUtil;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatDtdDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.ext.mysql.MySqlDataTypeFactory;
import org.dbunit.operation.DatabaseOperation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import static org.dbunit.dataset.xml.FlatXmlDataSet.*;

public class DBUnit {

    /**
     * Path to database dtd file. This file is required by the dbunit dataseed file,
     * which is used to populate the test db.
     */
    private static final String DATABASE_DTD = "docroot/WEB-INF/src/edu/osu/cws/evals/tests/database.dtd";

    /**
     * Path to database seed file. This is an xml file with test data.
     */
    private static final String SEED_FILE = "docroot/WEB-INF/src/edu/osu/cws/evals/tests/full.xml";

    /**
     * Config value - name of hibernate development xml config file used to grab dtd.
     */
    public static final String DEVELOPMENT_CONFIG = "hibernate-luminis-dev.cfg.xml";


    /**
     * Hashmap used to store the db credentials to access the test db
     */
    private HashMap<String, String> testDBCredentials = new HashMap<String, String>();

    /**
     * Hashmap used to store the db credentials to access the dev db
     */
    private HashMap<String, String> devDBCredentials = new HashMap<String, String>();

    /**
     * Constructor which calls readConfigs to parse test and dev db credentials.
     */
    public DBUnit() {
        HibernateUtil.setHibernateConfig(HibernateUtil.TEST_CONFIG, "", "");
        readConfigs();
    }

    /**
     * Connects to the dev database and exports a dbunit DTD file. This file is needed
     * by the dubnit data seed which is used to populate the test database.
     *
     * @throws Exception    Throws an exception if there is a problem connection to db
     */
    public void exportDatabaseDTD() throws Exception {
        readConfigs();
        // database connection
        Class<?> driverClass = Class.forName(devDBCredentials.get("connectionDriver"));
        Connection jdbcConnection = DriverManager.getConnection(devDBCredentials.get("connectionURL"),
                devDBCredentials.get("username"), devDBCredentials.get("password"));
        IDatabaseConnection connection = new DatabaseConnection(jdbcConnection);

        // write DTD file
        FlatDtdDataSet.write(connection.createDataSet(), new FileOutputStream(DATABASE_DTD));
    }
    /**
     * Dumps the data in the test db and loads with the the database seed file.
     *
     * @throws Exception    throws exception if there is a problem connection to db
     */
    public void seedDatabase() throws Exception {
        IDatabaseConnection conn = this.getConnection(
                testDBCredentials.get("connectionDriver"), testDBCredentials.get("connectionURL"),
                testDBCredentials.get("username"), testDBCredentials.get("password"));
        DatabaseConfig config = conn.getConfig();
        config.setProperty("http://www.dbunit.org/properties/datatypeFactory",
                new MySqlDataTypeFactory());
        IDataSet data = this.getDataSet(SEED_FILE);

        try {
            DatabaseOperation.CLEAN_INSERT.execute(conn, data);
        }finally {
            conn.close();
        }
    }

    /**
     * Connects to the dev database and exports the data into the SEED_FILE. You
     * can modify this method if you want to pull data from the dev database instead.
     *
     * @throws SQLException
     * @throws IOException
     * @throws DatabaseUnitException
     * @throws ClassNotFoundException
     */
    public void exportDataset() throws SQLException, IOException,
            DatabaseUnitException, ClassNotFoundException {
        IDatabaseConnection connection = getConnection(
                devDBCredentials.get("connectionDriver"),
                devDBCredentials.get("connectionURL"),
                devDBCredentials.get("username"), devDBCredentials.get("password")
        );

        // exports all the data from tables in the db
        IDataSet fullDataSet = connection.createDataSet();
        DatabaseConfig config = connection.getConfig();
        config.setProperty("http://www.dbunit.org/properties/datatypeFactory",
                new MySqlDataTypeFactory());
        write(fullDataSet, new FileOutputStream(SEED_FILE));
    }

    /**
     * Creates a DatabaseConnection and returns it.
     *
     * @param driver    db driver
     * @param url       connection url
     * @param user      db username
     * @param password  db password
     * @return DatabaseConnection
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws DatabaseUnitException
     */
    private IDatabaseConnection getConnection(String driver,
                                              String url, String user, String password )
            throws ClassNotFoundException, SQLException, DatabaseUnitException {
        Class.forName(driver);
        Connection jdbcConnection =  DriverManager.getConnection(url, user, password);
        return new DatabaseConnection(jdbcConnection);
    }

    /**
     * Calls readConfigFile to read the dev and test hibernate configs and stores
     * the returned hash maps in devDBCredentials and testDBCredentials.
     */
    private void readConfigs() {
        this.devDBCredentials = readConfigFile(HibernateUtil.CONFIG_PATH +
                DBUnit.DEVELOPMENT_CONFIG);
        this.testDBCredentials = readConfigFile(HibernateUtil.CONFIG_PATH +
                HibernateUtil.TEST_CONFIG);
    }

    /**
     * Takes a path to an xml file, then it uses Apache Commons XMLConfiguration to
     * parse hibernate's db: user, password, connection url, and connection driver.
     * It returns a hashmap with these values.
     *
     * @param filePath      path to Hibernate xml configuration file
     * @return HashMap
     */
    public HashMap<String, String> readConfigFile(String filePath) {
        HashMap<String, String> configCredentials = new HashMap<String, String>();
        try
        {
            // Load hibernate xml configuration
            XMLConfiguration config = new XMLConfiguration();
            config.setFileName(filePath);
            // set  validation to off, because doc-type makes it fail
            config.setValidating(false);
            config.load();

            List properties = (List) config.getProperty("session-factory.property");
            List propertiesKeys = (List) config.getProperty("session-factory.property[@name]");
            for (int i =0; i < properties.size(); i++) {
                if (propertiesKeys.get(i).equals("hibernate.connection.username")) {
                    configCredentials.put("username", (String) properties.get(i));
                }
                if (propertiesKeys.get(i).equals("hibernate.connection.password")) {
                    configCredentials.put("password", (String) properties.get(i));
                }
                if (propertiesKeys.get(i).equals("hibernate.connection.url")) {
                    configCredentials.put("connectionURL", (String) properties.get(i));
                }
                if (propertiesKeys.get(i).equals("hibernate.connection.driver_class")) {
                    configCredentials.put("connectionDriver", (String) properties.get(i));
                }
            }
        }
        catch(ConfigurationException cex) { }
        return configCredentials;
    }

    /**
     * Reads the xml SEED_FILE parses the data and returns the IDataSet.
     *
     * @param path  path to database seed file
     * @return
     * @throws IOException
     * @throws DataSetException
     */
    private IDataSet getDataSet(String path) throws IOException, DataSetException {
        FlatXmlDataSetBuilder builder = new FlatXmlDataSetBuilder();
        builder.setColumnSensing(true);
        return builder.build(new File(path));
    }
}
