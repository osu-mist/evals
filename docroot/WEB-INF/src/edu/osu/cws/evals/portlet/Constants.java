/**
 * Class to hold jsp file names and path constants. This used to be stored in
 * portlet.xml, but we were running into an error where the portletDispatcher
 * was null.
 */
package edu.osu.cws.evals.portlet;

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

    //if appraisalStartDate is before FUL__GOALS_DUE date, create the appraisal these many
    //days before appraialsDue.
    public static final int DAYS_BEFORE_APPRAISAL_DUE_To_CREATE = 60;

    public static final String ROOT_DIR = "WEB-INF/src/";

    // Used by several Hibernate classes when executing hierarchical queries
    public static final String CONNECT_BY = "CONNECT BY " +
            "pyvpasj_supervisor_pidm = prior pyvpasj_pidm AND " +
            "pyvpasj_supervisor_posn = prior pyvpasj_posn AND " +
            "pyvpasj_supervisor_suff = prior pyvpasj_suff ";

    //Appraisal ends on or after EVALS_START_DATE will be handled by this app.
    public static final String EVALS_START_DATE = "10/31/2011";

    public static final Integer MAX_ORG_CODE_DIGITS = 6;
}
