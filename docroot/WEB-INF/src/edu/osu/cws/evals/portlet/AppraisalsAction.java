package edu.osu.cws.evals.portlet;

import com.liferay.portal.kernel.servlet.HttpHeaders;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.util.ParamUtil;
import edu.osu.cws.evals.hibernate.AppraisalMgr;
import edu.osu.cws.evals.hibernate.CloseOutReasonMgr;
import edu.osu.cws.evals.hibernate.NolijCopies;
import edu.osu.cws.evals.models.*;
import edu.osu.cws.evals.util.EvalsPDF;
import edu.osu.cws.evals.util.HibernateUtil;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;

import javax.portlet.*;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class AppraisalsAction implements ActionInterface {
    private ActionHelper actionHelper;

    private HomeAction homeAction;
    
    /**
     * Handles displaying a list of pending reviews for a given business center.
     *
     * @param request   PortletRequest
     * @param response  PortletResponse
     * @return jsp      JSP file to display (defined in portlet.xml)
     * @throws Exception
     */
    public String reviewList(PortletRequest request, PortletResponse response) throws Exception {
        // Check that the logged in user is admin
        if (!actionHelper.isLoggedInUserReviewer(request)) {
            actionHelper.addErrorsToRequest(request, ActionHelper.ACCESS_DENIED);
            return homeAction.display(request, response);
        }

        ArrayList<Appraisal> appraisals = actionHelper.getReviewsForLoggedInUser(request, -1);
        actionHelper.addToRequestMap("appraisals", appraisals,request);
        actionHelper.addToRequestMap("pageTitle", "pending-reviews",request);
        actionHelper.useMaximizedMenu(request);

        return Constants.JSP_REVIEW_LIST;
    }

    /**
     * Renders a list of appraisals based on the search criteria.
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public String search(PortletRequest request, PortletResponse response) throws Exception {
        List<Appraisal> appraisals = new ArrayList<Appraisal>();
        actionHelper.addToRequestMap("pageTitle", "search-results",request);
        ResourceBundle resource = (ResourceBundle) actionHelper.getPortletContextAttribute("resourceBundle");

        boolean isAdmin = actionHelper.isLoggedInUserAdmin(request);
        boolean isReviewer = actionHelper.isLoggedInUserReviewer(request);
        // reviewer permissions are higher than supervisor
        boolean isSupervisor = !isReviewer && actionHelper.isLoggedInUserSupervisor(request);

        if (!isAdmin && !isReviewer && !isSupervisor)  {
            actionHelper.addErrorsToRequest(request, ActionHelper.ACCESS_DENIED);
            ((ActionResponse) response).setWindowState(WindowState.NORMAL);
            return homeAction.display(request, response);
        }

        int pidm = actionHelper.getLoggedOnUser(request).getId();
        String searchTerm = ParamUtil.getString(request, "searchTerm");
        if (StringUtils.isEmpty(searchTerm)) {
            actionHelper.addErrorsToRequest(request, "Please enter an employee's OSU ID");
        } else {
            String bcName = "";
            if (isReviewer) {
                bcName = actionHelper.getReviewer(pidm).getBusinessCenterName();
            }
            AppraisalMgr appraisalMgr = new AppraisalMgr();

            try {
                appraisals = appraisalMgr.search(searchTerm, pidm, isAdmin, isSupervisor, bcName);

                if (appraisals.isEmpty()) {
                    if (isAdmin) {
                        actionHelper.addErrorsToRequest(request, resource.getString("appraisal-search-no-results-admin"));
                    } else if (isReviewer) {
                        actionHelper.addErrorsToRequest(request, resource.getString("appraisal-search-no-results-reviewer"));
                    } else {
                        actionHelper.addErrorsToRequest(request, resource.getString("appraisal-search-no-results-supervisor"));
                    }
                }
            } catch (ModelException e) {
                actionHelper.addErrorsToRequest(request, e.getMessage());
            }
        }

        actionHelper.addToRequestMap("appraisals", appraisals,request);
        actionHelper.useMaximizedMenu(request);

        return Constants.JSP_REVIEW_LIST;
    }

    /**
     * Handles displaying the appraisal when a user clicks on it. It loads the appraisal
     * object along with the respective permissionRule.
     *
     * @param request   PortletRequest
     * @param response  PortletResponse
     * @return jsp      JSP file to display (defined in portlet.xml)
     * @throws Exception
     */
    public String display(PortletRequest request, PortletResponse response) throws Exception {
        Appraisal appraisal;
        PermissionRule permRule;
        Employee currentlyLoggedOnUser = actionHelper.getLoggedOnUser(request);
        int userId = currentlyLoggedOnUser.getId();

        int appraisalID = ParamUtil.getInteger(request, "id");
        if (appraisalID == 0) {
            actionHelper.addErrorsToRequest(request, ActionHelper.ACCESS_DENIED);
            return homeAction.display(request, response);
        }

        AppraisalMgr appraisalMgr = new AppraisalMgr();
        actionHelper.setAppraisalMgrParameters(currentlyLoggedOnUser, appraisalMgr);

        // 1) Get the appraisal and permission rule
        appraisal = appraisalMgr.getAppraisal(appraisalID);
        permRule = appraisalMgr.getAppraisalPermissionRule(appraisal);

        // Check to see if the logged in user has permission to access the appraisal
        if (permRule == null) {
            actionHelper.addErrorsToRequest(request, ActionHelper.ACCESS_DENIED);
            return homeAction.display(request, response);
        }

        String userRole = appraisalMgr.getRole(appraisal, userId);
        appraisal.setRole(userRole);

        actionHelper.setupMyTeamActiveAppraisals(request, userId);
        if (actionHelper.isLoggedInUserReviewer(request)) {
            ArrayList<Appraisal> reviews = actionHelper.getReviewsForLoggedInUser(request, -1);
            actionHelper.addToRequestMap("pendingReviews", reviews,request);
        }

        if (actionHelper.isLoggedInUserReviewer(request) && appraisal.getEmployeeSignedDate() != null &&
                !appraisal.getRole().equals("employee")) {
            actionHelper.addToRequestMap("displayResendNolij", true,request);
        }
        if ((actionHelper.isLoggedInUserReviewer(request) || actionHelper.isLoggedInUserAdmin(request)) && appraisal.isOpen()
                && !userRole.equals("employee")) {
            actionHelper.addToRequestMap("displayCloseOutAppraisal", true,request);
        }
        String status = appraisal.getStatus();
        if ((actionHelper.isLoggedInUserAdmin(request) || actionHelper.isLoggedInUserReviewer(request)) &&
                status.equals(Appraisal.STATUS_GOALS_APPROVED) && !userRole.equals("employee")) {
            actionHelper.addToRequestMap("displaySetAppraisalStatus", true,request);
        }

        // Initialze lazy appraisal associations
        Job job = appraisal.getJob();
        job.toString();
        Job supervisor = job.getSupervisor();
        if (supervisor != null && supervisor.getEmployee() != null) {
            supervisor.getEmployee().toString();
        }
        if (job.getEmployee() != null) {
            job.getEmployee().toString();
        }
        if (appraisal.getCloseOutReason() != null) {
            appraisal.getCloseOutReason().getReason();
        }
        appraisal.getSortedAssessments().size();
        for (Assessment assessment : appraisal.getAssessments()) {
            assessment.getCriterionDetail().getAreaID();
        }
        // End of initialize lazy appraisal associations

        actionHelper.addToRequestMap("appraisal", appraisal,request);
        actionHelper.addToRequestMap("permissionRule", permRule,request);
        actionHelper.useMaximizedMenu(request);

        return Constants.JSP_APPRAISAL;
    }


    /**
     * Handles updating the appraisal form.
     *
     * @param request   PortletRequest
     * @param response  PortletResponse
     * @return jsp      JSP file to display (defined in portlet.xml)
     * @throws Exception
     */
    public String update(PortletRequest request, PortletResponse response) throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        AppraisalMgr appraisalMgr = new AppraisalMgr();
        CompositeConfiguration config;
        int id = ParamUtil.getInteger(request, "id", 0);
        if (id == 0) {
            actionHelper.addErrorsToRequest(request, ActionHelper.APPRAISAL_NOT_FOUND);
            return homeAction.display(request, response);
        }

        Employee currentlyLoggedOnUser = actionHelper.getLoggedOnUser(request);
        actionHelper.setAppraisalMgrParameters(currentlyLoggedOnUser, appraisalMgr);
        Appraisal appraisal = (Appraisal) session.get(Appraisal.class, id);
        PermissionRule permRule = appraisalMgr.getAppraisalPermissionRule(appraisal);

        // Check to see if the logged in user has permission to access the appraisal
        if (permRule == null) {
            SessionErrors.add(request, "You do  not have permission to view the appraisal");
            return homeAction.display(request, response);
        }

        try {
            appraisalMgr.processUpdateRequest(request.getParameterMap(), appraisal, permRule);

            String signAppraisal = ParamUtil.getString(request, "sign-appraisal");
            if (signAppraisal != null && !signAppraisal.equals("")) {
                config = (CompositeConfiguration) actionHelper.getPortletContextAttribute("environmentProp");
                String nolijDir = config.getString("pdf.nolijDir");
                String env = config.getString("pdf.env");
                createNolijPDF(appraisal, nolijDir, env, appraisalMgr);
            }


            // Creates the first annual appraisal if needed
            Map<String, Configuration> configurationMap =
                    (Map<String, Configuration>) actionHelper.getPortletContextAttribute("configurations");
            Configuration resultsDueConfig = configurationMap.get(Appraisal.STATUS_RESULTS_DUE);
            String action = "";
            if (signAppraisal != null && !signAppraisal.equals("")) {
                action = "sign-appraisal";
            }

            if (appraisal.getRole().equals("supervisor")) {
                actionHelper.setupMyTeamActiveAppraisals(request, currentlyLoggedOnUser.getId());
            } else if (appraisal.getRole().equals("employee")) {
                actionHelper.setupMyActiveAppraisals(request, currentlyLoggedOnUser.getId());
            }
        } catch (ModelException e) {
            SessionErrors.add(request, e.getMessage());
        }


        String status = appraisal.getStatus();
        String[] afterReviewStatus = {Appraisal.STATUS_RELEASE_DUE, Appraisal.STATUS_RELEASE_OVERDUE,
                Appraisal.STATUS_CLOSED};
        if (ArrayUtils.contains(afterReviewStatus, status)
                && actionHelper.isLoggedInUserReviewer(request)) {
            actionHelper.removeReviewAppraisalInSession(request, appraisal);
        } else {
            updateAppraisalInSession(request, appraisal);
        }

        // If the user hit the save draft button, we stay in the same view
        if (request.getParameter("save-draft") != null || request.getParameter("cancel") != null ||
                request.getParameter("close-appraisal") != null) {
            if (request.getParameter("save-draft") != null) {
                SessionMessages.add(request, "draft-saved");
            }
            if (response instanceof ActionResponse) {
                ((ActionResponse) response).setWindowState(WindowState.MAXIMIZED);
            }
            return display(request, response);
        }

        return homeAction.display(request, response);
    }

    private void createNolijPDF(Appraisal appraisal, String dirName, String env,
                                AppraisalMgr appraisalMgr) throws Exception {
        // 1) Compose a file name
        CompositeConfiguration config = (CompositeConfiguration) actionHelper.getPortletContextAttribute("environmentProp");
        String filename = EvalsPDF.getNolijFileName(appraisal, dirName, env);

        // 2) Grab the permissionRule
        PermissionRule permRule = appraisalMgr.getAppraisalPermissionRule(appraisal);

        // 2) Create PDF
        ResourceBundle resource = (ResourceBundle) actionHelper.getPortletContextAttribute("resourceBundle");

        String rootDir = actionHelper.getPortletContext().getRealPath("/");
        EvalsPDF.createPDF(appraisal, permRule, filename, resource, rootDir);

        // 3) Insert a record into the nolij_copies table
        String onlyFilename = filename.replaceFirst(dirName, "");
        NolijCopies.add(appraisal.getId(), onlyFilename);
    }

    /**
     * Allows the end user to download a PDF copy of the appraisal
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public String downloadPDF(PortletRequest request, PortletResponse response) throws Exception {
        AppraisalMgr appraisalMgr = new AppraisalMgr();
        Appraisal appraisal;
        PermissionRule permRule;

        int appraisalID = ParamUtil.getInteger(request, "id");
        if (appraisalID == 0) {
            actionHelper.addErrorsToRequest(request, ActionHelper.ACCESS_DENIED);
            return homeAction.display(request, response);
        }
        Employee currentlyLoggedOnUser = actionHelper.getLoggedOnUser(request);
        actionHelper.setAppraisalMgrParameters(currentlyLoggedOnUser, appraisalMgr);

        // 1) Get the appraisal and permission rule
        appraisal = appraisalMgr.getAppraisal(appraisalID);
        permRule = appraisalMgr.getAppraisalPermissionRule(appraisal);

        // Check to see if the logged in user has permission to access the appraisal
        if (permRule == null) {
            actionHelper.addErrorsToRequest(request, ActionHelper.ACCESS_DENIED);
            return homeAction.display(request, response);
        }

        int userId = currentlyLoggedOnUser.getId();
        String userRole = appraisalMgr.getRole(appraisal, userId);
        appraisal.setRole(userRole);

        // 2) Compose a file name
        CompositeConfiguration config = (CompositeConfiguration) actionHelper.getPortletContextAttribute("environmentProp");
        String tmpDir = config.getString("pdf.tmpDir");
        String filename = EvalsPDF.getNolijFileName(appraisal, tmpDir, "dev2");

        // 3) Create PDF
        ResourceBundle resource = (ResourceBundle) actionHelper.getPortletContextAttribute("resourceBundle");
        String rootDir = actionHelper.getPortletContext().getRealPath("/");
        EvalsPDF.createPDF(appraisal, permRule, filename, resource, rootDir);

        // 4) Read the PDF file and provide to the user as attachment
        if (response instanceof ResourceResponse) {
            String title = appraisal.getJob().getJobTitle().replace(" ", "_");
            String employeeName = appraisal.getJob().getEmployee().getName().replace(" ", "_");
            String downloadFilename = "performance-appraisal-"+ title + "-" +
                     employeeName + "-" + appraisal.getJob().getPositionNumber()
                    + ".pdf";
            ResourceResponse res = (ResourceResponse) response;
            res.setContentType("application/pdf");
            res.addProperty(HttpHeaders.CACHE_CONTROL, "max-age=3600, must-revalidate");
            res.addProperty(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\""+downloadFilename+"\"");

            OutputStream out = res.getPortletOutputStream();
            RandomAccessFile in = new RandomAccessFile(filename, "r");

            byte[] buffer = new byte[4096];
            int len;
            while((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }

            out.flush();
            in.close();
            out.close();

            // 5) Delete the temp PDF file generated
            EvalsPDF.deletePDF(filename);
        }

        return null;
    }

    /***
     * This method updates the status of the appraisal in myTeam or myStatus to reflect the
     * changes from the update method.
     *
     * @param request       PortletRequest
     * @param appraisal     appraisal to update in session
     * @throws Exception
     */
    public void updateAppraisalInSession(PortletRequest request, Appraisal appraisal) throws Exception {
        List<Appraisal>  appraisals;
        Employee loggedOnUser = actionHelper.getLoggedOnUser(request);
        int employeeId = loggedOnUser.getId();
        if (appraisal.getRole().equals("employee")) {
            appraisals = actionHelper.getMyActiveAppraisals(request, employeeId);
        } else if (appraisal.getRole().equals(ActionHelper.ROLE_SUPERVISOR)) {
            appraisals = actionHelper.getMyTeamActiveAppraisals(request, employeeId);
        } else {
            return;
        }

        for (Appraisal appraisalInSession: appraisals) {
            if (appraisalInSession.getId() == appraisal.getId()) {
                appraisalInSession.setStatus(appraisal.getStatus());
                break;
            }
        }
    }

        /**
     * Sends the appraisal to NOLIJ. This is only allowed to reviewers and does not check whether or not
     * the appraisal has been sent to nolij before. It calls createNolijPDF to do the work.
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public String resendAppraisalToNolij(PortletRequest request, PortletResponse response) throws Exception {
        AppraisalMgr appraisalMgr = new AppraisalMgr();
        Appraisal appraisal;
        PermissionRule permRule;
        ResourceBundle resource = (ResourceBundle) actionHelper.getPortletContextAttribute("resourceBundle");

        int appraisalID = ParamUtil.getInteger(request, "id");
        if (appraisalID == 0) {
            actionHelper.addErrorsToRequest(request, ActionHelper.ACCESS_DENIED);
            return homeAction.display(request, response);
        }
        Employee currentlyLoggedOnUser = actionHelper.getLoggedOnUser(request);
        actionHelper.setAppraisalMgrParameters(currentlyLoggedOnUser, appraisalMgr);

        // 1) Get the appraisal and permission rule
        appraisal = appraisalMgr.getAppraisal(appraisalID);
        permRule = appraisalMgr.getAppraisalPermissionRule(appraisal);

        // Permission checks
        if (permRule != null && actionHelper.isLoggedInUserReviewer(request)
                && appraisal.getEmployeeSignedDate() != null && !appraisal.getRole().equals("employee")) {
            permRule = null;
        }

        // Check to see if the logged in user has permission to access the appraisal
        if (permRule == null) {
            actionHelper.addErrorsToRequest(request, ActionHelper.ACCESS_DENIED);
            return homeAction.display(request, response);
        }

        int userId = currentlyLoggedOnUser.getId();
        String userRole = appraisalMgr.getRole(appraisal, userId);
        appraisal.setRole(userRole);

        actionHelper.addToRequestMap("id", appraisal.getId(),request);
        if (!actionHelper.isLoggedInUserReviewer(request)) {
            String errorMsg = resource.getString("appraisal-resend-permission-denied");
            actionHelper.addErrorsToRequest(request, errorMsg);
            return display(request, response);
        }

        // If there is a problem, createNolijPDF will throw an exception
        CompositeConfiguration config = (CompositeConfiguration) actionHelper.getPortletContextAttribute("environmentProp");
        String nolijDir = config.getString("pdf.nolijDir");
        String env = config.getString("pdf.env");
        createNolijPDF(appraisal, nolijDir, env, appraisalMgr);

        SessionMessages.add(request, "appraisal-sent-to-nolij-success");

        return display(request, response);
    }

    /**
     * Handles an admin/reviewer closing an appraisal. We only display the form to close it. The
     * logic to handle closing is done by update method.
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public String closeOutAppraisal(PortletRequest request, PortletResponse response) throws Exception {
        Appraisal appraisal;
        AppraisalMgr appraisalMgr = new AppraisalMgr();
        PermissionRule permRule;
        Employee currentlyLoggedOnUser = actionHelper.getLoggedOnUser(request);
        int userId = currentlyLoggedOnUser.getId();

        int appraisalID = ParamUtil.getInteger(request, "id");
        if (appraisalID == 0) {
            actionHelper.addErrorsToRequest(request, ActionHelper.ACCESS_DENIED);
            return homeAction.display(request, response);
        }

        actionHelper.setAppraisalMgrParameters(currentlyLoggedOnUser, appraisalMgr);

        // 1) Get the appraisal, permission rule and userRole
        appraisal = appraisalMgr.getAppraisal(appraisalID);
        permRule = appraisalMgr.getAppraisalPermissionRule(appraisal);
        String userRole = appraisalMgr.getRole(appraisal, userId);

        // Check to see if the logged in user has permission to access the appraisal
        boolean isAdminOrReviewer = userRole.equals("admin") || userRole.equals("reviewer");
        if (permRule == null || !isAdminOrReviewer) {
            actionHelper.addErrorsToRequest(request, ActionHelper.ACCESS_DENIED);
            return homeAction.display(request, response);
        }

        List<CloseOutReason> reasonList = CloseOutReasonMgr.list(false);
        appraisal.getJob().getEmployee().toString();

        actionHelper.addToRequestMap("reasonsList", reasonList,request);
        actionHelper.addToRequestMap("appraisal", appraisal,request);
        actionHelper.useMaximizedMenu(request);

        return Constants.JSP_APPRAISAL_CLOSEOUT;
    }

    /**
     * Handles setting the status of an appraisal record to results due.
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public String setStatusToResultsDue(PortletRequest request, PortletResponse response) throws Exception {
        Appraisal appraisal;
        AppraisalMgr appraisalMgr = new AppraisalMgr();
        Employee currentlyLoggedOnUser = actionHelper.getLoggedOnUser(request);
        int userId = currentlyLoggedOnUser.getId();

        int appraisalID = ParamUtil.getInteger(request, "id");
        if (appraisalID == 0) {
            actionHelper.addErrorsToRequest(request, ActionHelper.ACCESS_DENIED);
            return homeAction.display(request, response);
        }

        actionHelper.setAppraisalMgrParameters(currentlyLoggedOnUser, appraisalMgr);

        // 1) Get the appraisal and role
        appraisal = appraisalMgr.getAppraisal(appraisalID);
        String userRole = appraisalMgr.getRole(appraisal, userId);
        appraisal.setRole(userRole);
        if (!userRole.equals("admin") && !userRole.equals(ActionHelper.ROLE_REVIEWER)) {
            actionHelper.addErrorsToRequest(request, ActionHelper.ACCESS_DENIED);
            return homeAction.display(request, response);
        }

        if (request instanceof ActionRequest && response instanceof ActionResponse) {
            appraisal.setOriginalStatus(appraisal.getStatus());
            appraisal.setStatus(Appraisal.STATUS_RESULTS_DUE);
            AppraisalMgr.updateAppraisalStatus(appraisal);
            SessionMessages.add(request, "appraisal-set-status-success");
            return display(request, response);
        }

        return homeAction.display(request, response);
    }

    public void setActionHelper(ActionHelper actionHelper) {
        this.actionHelper = actionHelper;
    }

    public void setHomeAction(HomeAction homeAction) {
        this.homeAction = homeAction;
    }
}
