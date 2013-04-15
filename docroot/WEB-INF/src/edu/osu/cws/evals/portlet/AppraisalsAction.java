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
import edu.osu.cws.evals.util.Mailer;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import javax.portlet.*;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.util.*;

public class AppraisalsAction implements ActionInterface {
    private ActionHelper actionHelper;

    private HomeAction homeAction;

    private PortletRequest request;

    private Employee loggedInUser;

    private ResourceBundle resource;

    private ErrorHandler errorHandler;

    private Appraisal appraisal = null;

    private PermissionRule permRule = null;

    private String userRole;

    /**
     * Handles displaying a list of pending reviews for a given business center.
     *
     * @param request   PortletRequest
     * @param response  PortletResponse
     * @return jsp      JSP file to display (defined in portlet.xml)
     * @throws Exception
     */
    public String reviewList(PortletRequest request, PortletResponse response) throws Exception {
        initialize(request);

        // Check that the logged in user is admin
        if (!actionHelper.isLoggedInUserReviewer()) {
            return errorHandler.handleAccessDenied(request, response);
        }

        ArrayList<Appraisal> appraisals = actionHelper.getReviewsForLoggedInUser(-1);
        actionHelper.addToRequestMap("appraisals", appraisals);
        actionHelper.addToRequestMap("pageTitle", "pending-reviews");
        actionHelper.useMaximizedMenu();

        return Constants.JSP_REVIEW_LIST;
    }

    /**
     * Initializes some private properties common to many methods.
     *
     * @param request
     * @throws Exception
     */
    private void initialize(PortletRequest request) throws Exception {
        this.request = request;
        this.resource = (ResourceBundle) actionHelper.getPortletContextAttribute("resourceBundle");
        this.loggedInUser = actionHelper.getLoggedOnUser();
        initializeAppraisal();
    }

    /**
     * Initializes an appraisal.
     *
     * @throws Exception
     */
    private void initializeAppraisal() throws Exception {
        AppraisalMgr appraisalMgr = new AppraisalMgr();
        int appraisalID = ParamUtil.getInteger(request, "id");
        if (appraisalID > 0) {
            actionHelper.setAppraisalMgrParameters(appraisalMgr);
            appraisal = appraisalMgr.getAppraisal(appraisalID);
            if(appraisal != null) {
                permRule = appraisalMgr.getAppraisalPermissionRule(appraisal);
                userRole = appraisal.getRole();
            }
        }
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
        initialize(request);
        List<Appraisal> appraisals = new ArrayList<Appraisal>();
        actionHelper.addToRequestMap("pageTitle", "search-results");

        boolean isAdmin = actionHelper.isLoggedInUserAdmin();
        boolean isReviewer = actionHelper.isLoggedInUserReviewer();

        // If a supervisor is also a reviewer, the people he/she supervises will be in the
        // business center he/she is a reviewer of. Because reviewer has broader permissions
        // than supervisor, we will use the reviewer's permission to do search.
        boolean isSupervisor = !isReviewer && actionHelper.isLoggedInUserSupervisor();

        if (!isAdmin && !isReviewer && !isSupervisor)  {
            ((ActionResponse) response).setWindowState(WindowState.NORMAL);
            return errorHandler.handleAccessDenied(request, response);
        }

        int pidm = loggedInUser.getId();
        String searchTerm = ParamUtil.getString(request, "searchTerm");
        if (StringUtils.isEmpty(searchTerm)) {
            actionHelper.addErrorsToRequest(resource.getString("appraisal-search-enter-id"));
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
                        actionHelper.addErrorsToRequest(resource.getString("appraisal-search-no-results-admin"));
                    } else if (isReviewer) {
                        actionHelper.addErrorsToRequest(resource.getString("appraisal-search-no-results-reviewer"));
                    } else {
                        actionHelper.addErrorsToRequest(resource.getString("appraisal-search-no-results-supervisor"));
                    }
                }
            } catch (ModelException e) {
                actionHelper.addErrorsToRequest(e.getMessage());
            }
        }

        actionHelper.addToRequestMap("appraisals", appraisals);
        actionHelper.useMaximizedMenu();

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
        initialize(request);

        // Check to see if the logged in user has permission to access the appraisal
        if (permRule == null) {
            return errorHandler.handleAccessDenied(request, response);
        }

        actionHelper.setupMyTeamActiveAppraisals();
        if (actionHelper.isLoggedInUserReviewer()) {
            ArrayList<Appraisal> reviews = actionHelper.getReviewsForLoggedInUser(-1);
            actionHelper.addToRequestMap("pendingReviews", reviews);
        }

        if (actionHelper.isLoggedInUserReviewer() && appraisal.getEmployeeSignedDate() != null &&
                !appraisal.getRole().equals("employee")) {
            actionHelper.addToRequestMap("displayResendNolij", true);
        }
        if ((actionHelper.isLoggedInUserReviewer() || actionHelper.isLoggedInUserAdmin()) && appraisal.isOpen()
                && !userRole.equals("employee")) {
            actionHelper.addToRequestMap("displayCloseOutAppraisal", true);
        }
        String status = appraisal.getStatus();
        if ((actionHelper.isLoggedInUserAdmin() || actionHelper.isLoggedInUserReviewer()) &&
                status.equals(Appraisal.STATUS_GOALS_APPROVED) && !userRole.equals("employee")) {
            actionHelper.addToRequestMap("displaySetAppraisalStatus", true);
        }

        Map Notices = (Map)actionHelper.getPortletContextAttribute("Notices");
        actionHelper.addToRequestMap("appraisalNotice", Notices.get("Appraisal Notice"));

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

        actionHelper.addToRequestMap("appraisal", appraisal);
        actionHelper.addToRequestMap("permissionRule", permRule);
        actionHelper.useMaximizedMenu();

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
        initialize(request);

        // Check to see if the logged in user has permission to access the appraisal
        if (permRule == null) {
            return errorHandler.handleAccessDenied(request, response);
        }

        PropertiesConfiguration config;
        try {
            processUpdateRequest(request.getParameterMap());

            String signAppraisal = ParamUtil.getString(request, "sign-appraisal");
            if (signAppraisal != null && !signAppraisal.equals("")) {
                config = actionHelper.getEvalsConfig();
                String nolijDir = config.getString("pdf.nolijDir");
                String env = config.getString("pdf.env");
                createNolijPDF(appraisal, nolijDir, env);
            }

            if (appraisal.getRole().equals("supervisor")) {
                actionHelper.setupMyTeamActiveAppraisals();
            } else if (appraisal.getRole().equals("employee")) {
                actionHelper.setupMyActiveAppraisals();
            }
        } catch (ModelException e) {
            SessionErrors.add(request, e.getMessage());
        }

        // If the user hit the save draft button, we stay in the same view
        if (request.getParameter("save-draft") != null || request.getParameter("cancel") != null) {
            if (request.getParameter("save-draft") != null) {
                SessionMessages.add(request, "draft-saved");
            }
            if (response instanceof ActionResponse) {
                ((ActionResponse) response).setWindowState(WindowState.MAXIMIZED);
            }
            return display(request, response);
        }

        String status = appraisal.getStatus();
        String[] afterReviewStatus = {Appraisal.STATUS_RELEASE_DUE, Appraisal.STATUS_RELEASE_OVERDUE,
                Appraisal.STATUS_CLOSED};
        if (ArrayUtils.contains(afterReviewStatus, status)
                && actionHelper.isLoggedInUserReviewer()) {
            actionHelper.removeReviewAppraisalInSession(appraisal);
        } else {
            updateAppraisalInSession(request, appraisal);
        }

        return homeAction.display(request, response);
    }

    /**
     * Processes the processAction request (Map) and tries to save the appraisal. This method
     * moves the appraisal to the next appraisal step and sends any emails if necessary.
     *
     * @param requestMap
     * @throws Exception
     */
    private void processUpdateRequest(Map requestMap)
            throws Exception {
        HashMap appraisalSteps = (HashMap) actionHelper.getPortletContextAttribute("appraisalSteps");
        Map<String, Configuration> configurationMap =
                (Map<String, Configuration>) actionHelper.getPortletContextAttribute("configurations");
        Mailer mailer = (Mailer) actionHelper.getPortletContextAttribute("mailer");


        // set the overdue value before updating the status
        String beforeUpdateStatus = appraisal.getStatus();
        // calculate overdue value & set the appraisal.overdue value
        AppraisalMgr.updateOverdue(appraisal, configurationMap);
        int oldOverdue = appraisal.getOverdue();

        // update appraisal & assessment fields based on permission rules
        setAppraisalFields(requestMap);

        boolean statusChanged = !appraisal.getStatus().equals(beforeUpdateStatus);
        if (statusChanged) {
            // Using the old status value call setStatusOverdue()
            String overdueMethod = StringUtils.capitalize(beforeUpdateStatus);
            overdueMethod = "set" + overdueMethod.replace("Due", "Overdue");
            try {
                // call setStageOverdue method
                Method controllerMethod = appraisal.getClass().getDeclaredMethod(overdueMethod,
                        Integer.class);
                controllerMethod.invoke(appraisal, oldOverdue);
            } catch (NoSuchMethodException e) {
                // don't do anything since some methods might not exist.
            }

            // Assign the new status based on configuration values
            String newStatus = AppraisalMgr.getNewStatus(appraisal, configurationMap);
            if (newStatus != null) {
                appraisal.setStatus(newStatus);
            }

            // If the new status is valid for overdue, refresh the overdue value
            if (appraisal.getStatus().contains(Appraisal.OVERDUE)) {
                AppraisalMgr.updateOverdue(appraisal, configurationMap);
            } else {
                appraisal.setOverdue(-999);
            }
        }

        // save changes to db
        AppraisalMgr.updateAppraisal(appraisal, loggedInUser);

        // Send email if needed
        String appointmentType = appraisal.getJob().getAppointmentType();
        AppraisalStep appraisalStep;
        String employeeResponse = appraisal.getRebuttal();

        // If the employee signs and provides a rebuttal, we want to use a different
        // appraisal step so that we can send an email to the reviewer.
        if (submittedRebuttal(requestMap, employeeResponse)) {
            String appraisalStepKey = "submit-response-" + appointmentType;
            appraisalStep = (AppraisalStep) appraisalSteps.get(appraisalStepKey);
        } else {
            appraisalStep = getAppraisalStep(requestMap, appointmentType);
        }

        EmailType emailType = appraisalStep.getEmailType();
        if (emailType != null) {
            mailer.sendMail(appraisal, emailType);
        }
    }

    /**
     * Handles updating the appraisal fields in the appraisal and assessment objects.
     *
     * @param requestMap
     */
    private void setAppraisalFields(Map<String, String[]> requestMap) throws Exception {
        String parameterKey = "";

        // Save Goals
        if (permRule.getGoals() != null && permRule.getGoals().equals("e")) {
            for (Assessment assessment : appraisal.getAssessments()) {
                String assessmentID = Integer.toString(assessment.getId());
                parameterKey = "appraisal.goal." + assessmentID;
                if (requestMap.get(parameterKey) != null) {
                    assessment.setGoal(requestMap.get(parameterKey)[0]);
                }
            }
            if (requestMap.get("submit-goals") != null) {
                appraisal.setGoalsSubmitDate(new Date());
            }
            if (requestMap.get("approve-goals") != null) {
                appraisal.setGoalApprovedDate(new Date());
                appraisal.setGoalsApprover(loggedInUser);
            }
        }
        // Save newGoals
        if (permRule.getNewGoals() != null && permRule.getNewGoals().equals("e")) {
            for (Assessment assessment : appraisal.getAssessments()) {
                String assessmentID = Integer.toString(assessment.getId());
                parameterKey = "appraisal.newGoal." + assessmentID;
                assessment.setNewGoals(requestMap.get(parameterKey)[0]);
            }
        }
        // Save goalComments
        if (permRule.getGoalComments() != null && permRule.getGoalComments().equals("e")) {
            if (requestMap.get("appraisal.goalsComments") != null) {
                appraisal.setGoalsComments(requestMap.get("appraisal.goalsComments")[0]);
            }
        }
        // Save employee results
        if (permRule.getResults() != null && permRule.getResults().equals("e")) {
            for (Assessment assessment : appraisal.getAssessments()) {
                String assessmentID = Integer.toString(assessment.getId());
                parameterKey = "assessment.employeeResult." + assessmentID;
                if (requestMap.get(parameterKey) != null) {
                    assessment.setEmployeeResult(requestMap.get(parameterKey)[0]);
                }
            }
        }
        // Save Supervisor Results
        if (permRule.getSupervisorResults() != null && permRule.getSupervisorResults().equals("e")) {
            for (Assessment assessment : appraisal.getAssessments()) {
                String assessmentID = Integer.toString(assessment.getId());
                parameterKey = "assessment.supervisorResult." + assessmentID;
                if (requestMap.get(parameterKey) != null) {
                    assessment.setSupervisorResult(requestMap.get(parameterKey)[0]);
                }
            }
        }
        if (requestMap.get("submit-results") != null) {
            appraisal.setResultSubmitDate(new Date());
        }
        // Save evaluation
        if (permRule.getEvaluation() != null && permRule.getEvaluation().equals("e")) {
            if (requestMap.get("appraisal.evaluation") != null) {
                appraisal.setEvaluation(requestMap.get("appraisal.evaluation")[0]);
            }
            if (requestMap.get("appraisal.rating") != null) {
                appraisal.setRating(Integer.parseInt(requestMap.get("appraisal.rating")[0]));
            }
            if (requestMap.get(permRule.getSubmit()) != null) {
                appraisal.setEvaluationSubmitDate(new Date());
                appraisal.setEvaluator(loggedInUser);
            }
        }
        // Save review
        if (permRule.getReview() != null && permRule.getReview().equals("e")) {
            if (requestMap.get("appraisal.review") != null) {
                appraisal.setReview(requestMap.get("appraisal.review")[0]);
            }
            if (requestMap.get(permRule.getSubmit()) != null) {
                appraisal.setReviewer(loggedInUser);
                appraisal.setReviewSubmitDate(new Date());
            }
        }
        if (requestMap.get("sign-appraisal") != null) {
            appraisal.setEmployeeSignedDate(new Date());
        }
        if (requestMap.get("release-appraisal") != null) {
            appraisal.setReleaseDate(new Date());
        }
        // Save employee response
        if (permRule.getEmployeeResponse() != null && permRule.getEmployeeResponse().equals("e")) {
            appraisal.setRebuttal(requestMap.get("appraisal.rebuttal")[0]);
            String employeeResponse = appraisal.getRebuttal();
            if (submittedRebuttal(requestMap, employeeResponse)) {
                appraisal.setRebuttalDate(new Date());
            }
        }
        // Save supervisor rebuttal read
        if (permRule.getRebuttalRead() != null && permRule.getRebuttalRead().equals("e")
                && requestMap.get("read-appraisal-rebuttal") != null) {
            appraisal.setSupervisorRebuttalRead(new Date());
        }

        // Save the close out reason
        if (appraisal.getRole().equals(ActionHelper.ROLE_REVIEWER) || appraisal.getRole().equals("admin")) {
            if (requestMap.get("appraisal.closeOutReasonId") != null) {
                int closeOutReasonId = Integer.parseInt(requestMap.get("appraisal.closeOutReasonId")[0]);
                CloseOutReason reason = CloseOutReasonMgr.get(closeOutReasonId);

                appraisal.setCloseOutBy(loggedInUser);
                appraisal.setCloseOutDate(new Date());
                appraisal.setCloseOutReason(reason);
                appraisal.setOriginalStatus(appraisal.getStatus());
            }
        }

        // If the appraisalStep object has a new status, update the appraisal object
        String appointmentType = appraisal.getJob().getAppointmentType();
        AppraisalStep appraisalStep = getAppraisalStep(requestMap, appointmentType);
        String newStatus = appraisalStep.getNewStatus();
        if (newStatus != null && !newStatus.equals(appraisal.getStatus())) {
            appraisal.setStatus(newStatus);
            String employeeResponse = appraisal.getRebuttal();
            if (submittedRebuttal(requestMap, employeeResponse)) {
                appraisal.setStatus(Appraisal.STATUS_REBUTTAL_READ_DUE);
            }
        }
        if (appraisal.getStatus().equals(Appraisal.STATUS_GOALS_REQUIRED_MODIFICATION)) {
            appraisal.setGoalsRequiredModificationDate(new Date());
        }
    }

    /**
     * Figures out the appraisal step key for the button that the user pressed when the appraisal
     * form was submitted.
     *
     * @param requestMap
     * @param appointmentType
     * @return
     */
    private AppraisalStep getAppraisalStep(Map requestMap, String appointmentType) {
        HashMap appraisalSteps = (HashMap) actionHelper.getPortletContextAttribute("appraisalSteps");
        AppraisalStep appraisalStep;
        String appraisalStepKey;
        ArrayList<String> appraisalButtons = new ArrayList<String>();
        if (permRule.getSaveDraft() != null) {
            appraisalButtons.add(permRule.getSaveDraft());
        }
        if (permRule.getRequireModification() != null) {
            appraisalButtons.add(permRule.getRequireModification());
        }
        if (permRule.getSubmit() != null) {
            appraisalButtons.add(permRule.getSubmit());
        }
        // close out button
        appraisalButtons.add("close-appraisal");

        for (String button : appraisalButtons) {
            // If this button is the one the user clicked, use it to look up the
            // appraisalStepKey
            if (requestMap.get(button) != null) {
                appraisalStepKey = button + "-" + appointmentType;
                appraisalStep = (AppraisalStep) appraisalSteps.get(appraisalStepKey);
                if (appraisalStep != null) {
                    return appraisalStep;
                }
            }
        }

        return new AppraisalStep();
    }

    private void createNolijPDF(Appraisal appraisal, String dirName, String env) throws Exception {
        // 1) Compose a file name
        String filename = EvalsPDF.getNolijFileName(appraisal, dirName, env);

        // 2) Create PDF
        String rootDir = actionHelper.getPortletContext().getRealPath("/");
        EvalsPDF.createPDF(appraisal, permRule, filename, resource, rootDir);

        // 3) Insert a record into the nolij_copies table
        String onlyFilename = filename.replaceFirst(dirName, "");
        NolijCopies.add(appraisal.getId(), onlyFilename);
    }

    /**
     * Specifies whether or not the employee submitted a rebuttal when the appraisal was signed.
     *
     * @param request
     * @param employeeResponse
     * @return
     */
    private boolean submittedRebuttal(Map<String, String[]> request, String employeeResponse) {
        return request.get("sign-appraisal") != null &&
                employeeResponse != null && !employeeResponse.equals("");
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
        initialize(request);

        // Check to see if the logged in user has permission to access the appraisal
        if (permRule == null) {
            return errorHandler.handleAccessDenied(request, response);
        }

        // 2) Compose a file name
        PropertiesConfiguration config = actionHelper.getEvalsConfig();
        String tmpDir = config.getString("pdf.tmpDir");
        String filename = EvalsPDF.getNolijFileName(appraisal, tmpDir, "dev2");

        // 3) Create PDF
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
        initialize(request);
        List<Appraisal>  appraisals;
        int employeeId = loggedInUser.getId();
        if (appraisal.getRole().equals("employee")) {
            appraisals = actionHelper.getMyActiveAppraisals();
        } else if (appraisal.getRole().equals(ActionHelper.ROLE_SUPERVISOR)) {
            appraisals = actionHelper.getMyTeamActiveAppraisals();
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
        initialize(request);

        // Permission checks
        if (!actionHelper.isLoggedInUserReviewer()
                || appraisal.getEmployeeSignedDate() == null
                || appraisal.getRole().equals("employee")
                || !appraisal.getStatus().equals("completed"))
        {
            return errorHandler.handleAccessDenied(request, response);
        }

        AppraisalMgr appraisalMgr = new AppraisalMgr();
        actionHelper.addToRequestMap("id", appraisal.getId());

        if (!actionHelper.isLoggedInUserReviewer()) {
            String errorMsg = resource.getString("appraisal-resend-permission-denied");
            actionHelper.addErrorsToRequest(errorMsg);
            return display(request, response);
        }

        // If there is a problem, createNolijPDF will throw an exception
        PropertiesConfiguration config = actionHelper.getEvalsConfig();
        String nolijDir = config.getString("pdf.nolijDir");
        String env = config.getString("pdf.env");
        createNolijPDF(appraisal, nolijDir, env);

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
        initialize(request);

        // Check to see if the logged in user has permission to access the appraisal
        boolean isAdminOrReviewer = userRole.equals("admin") || userRole.equals("reviewer");
        if (permRule == null || !isAdminOrReviewer) {
            return errorHandler.handleAccessDenied(request, response);
        }

        List<CloseOutReason> reasonList = CloseOutReasonMgr.list(false);
        appraisal.getJob().getEmployee().toString();

        actionHelper.addToRequestMap("reasonsList", reasonList);
        actionHelper.addToRequestMap("appraisal", appraisal);
        actionHelper.useMaximizedMenu();

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
        initialize(request);

        if (!userRole.equals("admin") && !userRole.equals(ActionHelper.ROLE_REVIEWER)) {
            return errorHandler.handleAccessDenied(request, response);
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

    public void setErrorHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }
}