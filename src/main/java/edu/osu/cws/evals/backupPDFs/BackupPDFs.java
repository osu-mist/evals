package edu.osu.cws.evals.backend;

import au.com.bytecode.opencsv.CSVWriter;
import com.google.inject.Inject;
import edu.osu.cws.evals.hibernate.*;
import edu.osu.cws.evals.models.*;
import edu.osu.cws.evals.portlet.Constants;
import edu.osu.cws.evals.util.*;
import edu.osu.cws.evals.portlet.AppraisalsAction;
import edu.osu.cws.util.Logger;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.joda.time.DateTime;
import org.joda.time.Days;

import java.io.*;
import java.util.*;

public class BackupPDFs {
    private MailerInterface mailer;
    private Map<String, Configuration> configMap;
    private Map<String, EmailType> emailTypeMap;
    private LoggingInterface logger;

    private Session session = null;
    private Transaction tx;
    private ResourceBundle resource;

    /**
     * Constructor method that is automatically called by google guice.
     *
     * @param logger
     * @param mailer
     * @param configMap
     * @param emailTypeMap
     */
    @Inject
    BackupPDFs(LoggingInterface logger, MailerInterface mailer, Map<String, Configuration> configMap,
               Map<String, EmailType> emailTypeMap) {
        this.logger = logger;
        this.mailer = mailer;
        this.configMap = configMap;
        this.emailTypeMap = emailTypeMap;
    }

    /**
     * This is the high level method that puts everything together.
     * @throws Exception
     */
    public void process() throws Exception
    {
        try {
            session = HibernateUtil.getCurrentSession();
            tx = session.beginTransaction();

            resource = ResourceBundle.getBundle("content.Language");

            List<Appraisal> appraisals = session.getNamedQuery("appraisal.getNonCompletedAppraisals").list();
                // .setParameterList("ids", employeePidms).list();
            System.out.println(appraisals.size());
            Appraisal app = null;
            // for (Appraisal appraisal : appraisals) {
                // List<String> statuses = new ArrayList<String>();
                // statuses.add("resultsDue");
                // statuses.add("resultsOverDue");
                // if(statuses.contains(appraisal.getStatus())) {
                    // app = appraisal;
                    // break;
                // }
            // }
            // if (app == null) {
                // return;
            // }
            app = appraisals.get(0);
            System.out.println(app.getId());

            // TODO: Loop over appraisals here
            setPermRule(app);

            // PropertiesConfiguration config = actionHelper.getEvalsConfig();
            String nolijDir = "/opt/evals/pdf/";
            String env = "dev2";
            // TODO: This is variable?
            String suffix = "-prof";
            String fileName = GeneratePDF(app, nolijDir, env, suffix);
        } finally {
            if (tx != null) {
                tx.commit();
            }
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    public String GeneratePDF(Appraisal appraisal, String dirName, String env, String suffix) throws Exception {
        // Create PDF
        String rootDir = "/opt/liferay";
        // Map<String, List<Rating>> ratingsMap = (HashMap) actionHelper.getPortletContext().getAttribute("ratings");
        Map<String, List<Rating>> ratingsMap = (HashMap) RatingMgr.mapByAppointmentType();
        List<Rating> ratings = RatingMgr.getRatings(ratingsMap, appraisal.getAppointmentType());
        EvalsPDFBox PdfGenerator = new EvalsPDFBox(rootDir, appraisal, resource, dirName, env, suffix, ratings);
        String filename = PdfGenerator.createPDF();

        // Insert a record into the nolij_copies table and upload to onbase
        String onlyFilename = filename.replaceFirst(dirName, "");
        PropertiesConfiguration config = EvalsUtil.loadEvalsConfig(null);
        System.out.println(config);
        EvalsOnbase onbase = new EvalsOnbase(
            config.getString("onbase.clientId"),
            config.getString("onbase.clientSecret"),
            config.getString("onbase.oauth2Url"),
            config.getString("onbase.onbaseDocsUrl"),
            config.getString("pdf.nolijDir"),
            config.getString("onbase.classifiedDocType"),
            config.getString("onbase.rankedDocType")
        );
        // EvalsOnbase onbase = (EvalsOnbase) actionHelper.getPortletContext().getAttribute("onbase");
        onbase.postPDF(onlyFilename, appraisal.getJob());
        NolijCopyMgr.add(appraisal.getId(), onlyFilename);

        return filename;
    }

    /**
     * Figures out the current user role in the appraisal and returns the respective permission
     * rule for that user role and action in the appraisal. If the appraisal's status is like
     * "archived*" then it will remove the "archived" part of the status.
     *
     * @throws Exception
     */
    public void setPermRule(Appraisal appraisal) throws Exception {
        // HashMap permissionRules = (HashMap) actionHelper.getPortletContext().getAttribute("permissionRules");
        HashMap permissionRules = (HashMap) PermissionRuleMgr.list();
        PermissionRule permRule = PermissionRuleMgr.getPermissionRule(permissionRules, appraisal, "masterAdmin");
        appraisal.setPermissionRule(permRule);

        // Disable the employee/supervisor results if we are in the first round of goals (no approved goals yet)
        // if (appraisal.getStatus().contains("goal") && appraisal.getApprovedGoalsVersions().isEmpty()) {
            // permRule.setResults(null);
            // permRule.setSupervisorResults(null);
        // }
    }
}
