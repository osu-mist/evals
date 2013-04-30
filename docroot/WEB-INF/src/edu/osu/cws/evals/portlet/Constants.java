/**
 * Class to hold jsp file names and path constants. This used to be stored in
 * portlet.xml, but we were running into an error where the portletDispatcher
 * was null.
 */
package edu.osu.cws.evals.portlet;

import java.io.File;

public class Constants {

    public static final String JSP_HOME = "/jsp/home/start.jsp";
    public static final String JSP_HOME_ADMIN = "/jsp/home/startAdmin.jsp";
    public static final String JSP_HOME_SUPERVISOR = "/jsp/home/startSupervisor.jsp";
    public static final String JSP_HOME_REVIEWER = "/jsp/home/startReviewer.jsp";
    public static final String JSP_MY_INFO = "/jsp/home/myInfo.jsp";

    public static final String JSP_ERROR = "/jsp/error.jsp";

    public static final String JSP_ADMIN_LIST = "/jsp/admins/list.jsp";
    public static final String JSP_ADMIN_DELETE = "/jsp/admins/delete.jsp";

    public static final String JSP_REVIEWER_LIST = "/jsp/reviewers/list.jsp";
    public static final String JSP_REVIEWER_DELETE = "/jsp/reviewers/delete.jsp";

    public static final String JSP_CONFIGURATION_LIST = "/jsp/configurations/list.jsp";

    public static final String JSP_CRITERIA_LIST = "/jsp/criteria/list.jsp";
    public static final String JSP_CRITERIA_ADD = "/jsp/criteria/add.jsp";
    public static final String JSP_CRITERIA_DELETE = "/jsp/criteria/delete.jsp";

    public static final String JSP_APPRAISAL = "/jsp/appraisals/appraisal.jsp";
    public static final String JSP_REVIEW_LIST = "/jsp/appraisals/reviewList.jsp";

    public static final String JSP_CLOSEOUT_REASON_LIST = "/jsp/closeout_reasons/list.jsp";
    public static final String JSP_CLOSEOUT_REASON_DELETE = "/jsp/closeout_reasons/delete.jsp";
    public static final String JSP_APPRAISAL_CLOSEOUT = "/jsp/appraisals/closeout.jsp";

    public static final String JSP_REPORT = "/jsp/reports/report.jsp";
    public static final String JSP_REPORT_SEARCH_RESULTS = "/jsp/reports/reportSearchResults.jsp";

    public static final String JSP_NOTICE_LIST = "/jsp/notices/list.jsp";
    public static final String JSP_NOTICE_EDIT = "/jsp/notices/edit.jsp";

    // Used by several Hibernate classes when executing hierarchical queries
    public static final String CONNECT_BY = "CONNECT BY " +
            "pyvpasj_supervisor_pidm = prior pyvpasj_pidm AND " +
            "pyvpasj_supervisor_posn = prior pyvpasj_posn AND " +
            "pyvpasj_supervisor_suff = prior pyvpasj_suff ";

    //Appraisal ends on or after EVALS_START_DATE will be handled by this app.
    public static final String EVALS_START_DATE = "10/31/2011";

    public static final Integer MAX_ORG_CODE_DIGITS = 6;

    public static final Integer ANNUAL_IND = 12;
    public static final Integer TRIAL_IND = 6;

    public static final String BC_NAME = "bcName";

    public static final Integer SEARCH_MAX_RESULTS = 20;

    // The # of blank assessments that are created for a new evaluation
    public static final Integer BLANK_ASSESSMENTS_IN_NEW_EVALUATION = 5;

    public static final long PORTLET_RELOAD_FREQUENCY = 1000 * 60 * 60 * 24; //1 days in ms

    public static final String DATE_FORMAT = "MM/dd/yy";
    public static final String DATE_FORMAT_FULL = "MM/dd/yyyy";

    public static final int MAX_ERROR_COUNT = 500;  //# of errors before aborting
    public static final String EMAIL_BUNDLE_FILE = "edu.osu.cws.evals.portlet.Email";

    public static final String PROPERTIES_FILENAME = "evals.properties";

    // Private constants
    private static final String ROOT_DIR = "WEB-INF/src/";
    private static final String DEFAULT_PROPERTIES_FILE = "backend-config.properties";

    /**
     * The root directory of the EvalS project. The docroot directory is removed by the fabric
     * script. During local dev & testing, we need to append docroot to the root_dir.
     *
     * @return
     */
    public static String getRootDir() {
        if (new File("docroot").exists()) {
            return "docroot/" + ROOT_DIR;
        }

        return ROOT_DIR;
    }

    /**
     * The path to the default properties file. This value depends on ROOT_DIR which is calculated
     * at runtime. That's why we have to provide a method for it.
     *
     * @return
     */
    public static String getDefaultPropertiesFile() {
        return getRootDir() +  DEFAULT_PROPERTIES_FILE;
    }
}
