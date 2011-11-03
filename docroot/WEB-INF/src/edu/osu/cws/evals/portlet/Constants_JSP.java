/**
 * Class to hold jsp file names and path constants. This used to be stored in
 * portlet.xml, but we were running into an error where the portletDispatcher
 * was null.
 */
package edu.osu.cws.evals.portlet;

public class Constants_JSP {

    public static final String HOME = "/jsp/home/start.jsp";
    public static final String HOME_ADMIN = "/jsp/home/startAdmin.jsp";
    public static final String HOME_SUPERVISOR = "/jsp/home/startSupervisor.jsp";
    public static final String HOME_REVIEWER = "/jsp/home/startReviewer.jsp";
    public static final String MY_INFO = "/jsp/home/myInfo.jsp";

    public static final String ERROR = "/jsp/error.jsp";

    public static final String ADMIN_LIST = "/jsp/admins/list.jsp";
    public static final String ADMIN_DELETE = "/jsp/admins/delete.jsp";

    public static final String REVIEWER_LIST = "/jsp/reviewers/list.jsp";
    public static final String REVIEWER_DELETE = "/jsp/reviewers/delete.jsp";

    public static final String CONFIGURATION_LIST = "/jsp/configurations/list.jsp";

    public static final String CRITERIA_LIST = "/jsp/criteria/list.jsp";
    public static final String CRITERIA_ADD = "/jsp/criteria/add.jsp";
    public static final String CRITERIA_DELETE = "/jsp/criteria/delete.jsp";

    public static final String APPRAISAL = "/jsp/appraisals/appraisal.jsp";
    public static final String REVIEW_LIST = "/jsp/appraisals/reviewList.jsp";
}
