package edu.osu.cws.evals.tests;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.liferay.portal.kernel.util.ParamUtil;
import edu.osu.cws.evals.hibernate.AppraisalMgr;
import edu.osu.cws.evals.hibernate.EmployeeMgr;
import edu.osu.cws.evals.hibernate.PermissionRuleMgr;
import edu.osu.cws.evals.models.*;
import edu.osu.cws.evals.portlet.*;
import edu.osu.cws.evals.util.HibernateUtil;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.mockito.Mockito.*;

import javax.portlet.PortletContext;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.swing.*;
import java.util.*;

@Test
public class AppraisalsActionTest {

    private Appraisal appraisal;
    private PortletContext mockedPortletContext;
    private AppraisalsAction appraisalsAction;
    private Map<String, Assessment> dbAssessmentMap;
    public static final String EMPLOYEE_RESULTS = "Test Employee Results";
    public static final String SUPERVISOR_RESULTS = "Test Supervisor Results";

    @BeforeMethod
    public void setup() {
        appraisal = new Appraisal();
        appraisal.setJob(new Job());
        appraisal.getJob().setAppointmentType(AppointmentType.CLASSIFIED);
        dbAssessmentMap = new HashMap<String, Assessment>();

        mockedPortletContext = mock(PortletContext.class);
        appraisalsAction = new AppraisalsAction();
        ActionHelper actionHelper = new ActionHelper();
        actionHelper.setPortletContext(mockedPortletContext);
        appraisalsAction.setActionHelper(actionHelper);
        appraisalsAction.setAppraisal(appraisal);
    }

    public void shouldSetPermCorrectlyForArchivedCompleted() throws Exception{
        HashMap<String, PermissionRule> rules = new HashMap<String, PermissionRule>();
        appraisal.setStatus(Appraisal.STATUS_ARCHIVED_COMPLETED);
        appraisalsAction.setUserRole(ActionHelper.ROLE_EMPLOYEE);

        String key1 = "completed-employee-Default";
        PermissionRule rule1 = new PermissionRule();
        rule1.setAppointmentType("Default");
        rule1.setStatus(Appraisal.STATUS_COMPLETED);
        rule1.setRole(ActionHelper.ROLE_EMPLOYEE);
        rules.put(key1, rule1);

        // Mock Permission rule map used by AppraisalsAction
        when(mockedPortletContext.getAttribute("permissionRules")).thenReturn(rules);
        appraisalsAction.setPermRule();

        assert appraisalsAction.getPermRule().getStatus().equals(Appraisal.STATUS_COMPLETED);
        assert appraisalsAction.getPermRule().getRole().equals(ActionHelper.ROLE_EMPLOYEE);
    }

    public void shouldSetPermRuleCorrectlyForArchivedClosed() throws Exception {
        HashMap<String, PermissionRule> rules = new HashMap<String, PermissionRule>();
        appraisal.setStatus(Appraisal.STATUS_ARCHIVED_CLOSED);
        appraisalsAction.setUserRole(ActionHelper.ROLE_EMPLOYEE);

        String key1 = "closed-employee-Default";
        PermissionRule rule1 = new PermissionRule();
        rule1.setAppointmentType("Default");
        rule1.setStatus(Appraisal.STATUS_CLOSED);
        rule1.setRole(ActionHelper.ROLE_EMPLOYEE);
        rules.put(key1, rule1);

        // Mock Permission rule map used by AppraisalsAction
        when(mockedPortletContext.getAttribute("permissionRules")).thenReturn(rules);
        appraisalsAction.setPermRule();

        assert appraisalsAction.getPermRule().getStatus().equals(Appraisal.STATUS_CLOSED);
        assert appraisalsAction.getPermRule().getRole().equals(ActionHelper.ROLE_EMPLOYEE);
    }

    public void shouldGetCorrectRole() throws Exception {
        Employee employee = new Employee();
        Job employeeJob = new Job();
        employee.setId(59999);
        employeeJob.setEmployee(employee);
        appraisal.setJob(employeeJob);
        appraisalsAction.setLoggedInUser(employee);
        appraisalsAction.setAppraisal(appraisal);
        assert appraisalsAction.getRole().equals(ActionHelper.ROLE_EMPLOYEE);
    }

    public void shouldGetCorrectRoleWhenSuperivsor() throws Exception {
        Employee employee = new Employee();
        Job employeeJob = new Job();
        Employee supervisor = new Employee();
        Job supervisorJob = new Job();
        supervisor.setId(58888);
        supervisorJob.setEmployee(supervisor);
        employee.setId(59999);
        employeeJob.setEmployee(employee);
        employeeJob.setSupervisor(supervisorJob);
        appraisal.setJob(employeeJob);
        appraisalsAction.setLoggedInUser(supervisor);
        appraisalsAction.setAppraisal(appraisal);
        assert appraisalsAction.getRole().equals(ActionHelper.ROLE_SUPERVISOR);
    }

    public void shouldGetCorrectRoleWhenReviewer() throws Exception {
        ActionHelper mockActionHelper = mock(ActionHelper.class);
        Employee loggedInUser = new Employee();
        Employee employee = new Employee();
        Job employeeJob = new Job();
        Reviewer reviewer = new Reviewer();
        reviewer.setBusinessCenterName("ABCD");
        loggedInUser.setId(58888);
        employee.setId(59999);
        employeeJob.setEmployee(employee);
        employeeJob.setBusinessCenterName("ABCD");
        appraisal.setJob(employeeJob);
        appraisalsAction.setLoggedInUser(loggedInUser);
        appraisalsAction.setAppraisal(appraisal);
        when(mockActionHelper.getReviewer()).thenReturn(reviewer);
        appraisalsAction.setActionHelper(mockActionHelper);
        assert appraisalsAction.getRole().equals(ActionHelper.ROLE_REVIEWER);
    }

    public void shouldGetCorrectRoleWhenMasterAdmin() throws Exception {
        ActionHelper mockActionHelper = mock(ActionHelper.class);
        Employee loggedInUser = new Employee();
        Employee employee = new Employee();
        Job employeeJob = new Job();
        Admin admin = new Admin();
        admin.setId(58888);
        loggedInUser.setId(58888);
        employee.setId(59999);
        employeeJob.setEmployee(employee);
        employeeJob.setBusinessCenterName("ABCD");
        appraisal.setJob(employeeJob);
        appraisalsAction.setLoggedInUser(loggedInUser);
        appraisalsAction.setAppraisal(appraisal);
        when(mockActionHelper.getAdmin()).thenReturn(admin);
        when(mockActionHelper.isLoggedInUserMasterAdmin()).thenReturn(true);
        appraisalsAction.setActionHelper(mockActionHelper);
        assert appraisalsAction.getRole().equals(ActionHelper.ROLE_MASTER_ADMIN);
    }

    public void shouldGetCorrectRoleWhenSuperAdmin() throws Exception {
        ActionHelper mockActionHelper = mock(ActionHelper.class);
        Employee loggedInUser = new Employee();
        Employee employee = new Employee();
        Job employeeJob = new Job();
        Admin admin = new Admin();
        admin.setId(58888);
        loggedInUser.setId(58888);
        employee.setId(59999);
        employeeJob.setEmployee(employee);
        employeeJob.setBusinessCenterName("ABCD");
        appraisal.setJob(employeeJob);
        appraisalsAction.setLoggedInUser(loggedInUser);
        appraisalsAction.setAppraisal(appraisal);
        when(mockActionHelper.getAdmin()).thenReturn(admin);
        when(mockActionHelper.isLoggedInUserMasterAdmin()).thenReturn(false);
        appraisalsAction.setActionHelper(mockActionHelper);
        assert appraisalsAction.getRole().equals(ActionHelper.ROLE_SUPER_ADMIN);
    }

    public void shouldGetCorrectRoleWhenUpperSupervisor() throws Exception {
        ActionHelper mockActionHelper = mock(ActionHelper.class);
        Employee loggedInUser = new Employee();
        Employee supervisor = new Employee();
        Employee employee = new Employee();
        Job employeeJob = new Job();
        Job supervisorJob = new Job();
        Job upperSupervisorJob = new Job();
        loggedInUser.setId(58888);
        loggedInUser.setStatus("A");
        upperSupervisorJob.setEmployee(loggedInUser);
        upperSupervisorJob.setStatus("A");
        supervisor.setId(50000);
        supervisor.setStatus("T");
        supervisorJob.setEmployee(supervisor);
        supervisorJob.setStatus("T");
        supervisorJob.setSupervisor(upperSupervisorJob);
        employee.setId(59999);
        employeeJob.setEmployee(employee);
        employeeJob.setSupervisor(supervisorJob);
        appraisal.setJob(employeeJob);
        appraisalsAction.setLoggedInUser(loggedInUser);
        appraisalsAction.setAppraisal(appraisal);
        when(mockActionHelper.getAdmin()).thenReturn(null);
        appraisalsAction.setActionHelper(mockActionHelper);
        assert appraisalsAction.getRole().equals(ActionHelper.ROLE_UPPER_SUPERVISOR);
    }

    public void shouldBeAdminRoleWhenMasterAdmin() throws Exception {
        appraisalsAction.setUserRole(ActionHelper.ROLE_MASTER_ADMIN);
        assert appraisalsAction.isAdminRole();
    }

    public void shouldBeAdminRoleWhenSuperAdmin() throws Exception {
        appraisalsAction.setUserRole(ActionHelper.ROLE_SUPER_ADMIN);
        assert appraisalsAction.isAdminRole();
    }

    public void shouldNotBeAdminRoleWhenAdministrator() throws Exception {
        appraisalsAction.setUserRole(ActionHelper.ROLE_ADMINISTRATOR);
        assert !appraisalsAction.isAdminRole();
    }

    public void shouldNotBeAdminRoleWhenSupervisor() throws Exception {
        appraisalsAction.setUserRole(ActionHelper.ROLE_SUPERVISOR);
        assert !appraisalsAction.isAdminRole();
    }

    public void shouldNotBeAdminRoleWhenUpperSupervisor() throws Exception {
        appraisalsAction.setUserRole(ActionHelper.ROLE_UPPER_SUPERVISOR);
        assert !appraisalsAction.isAdminRole();
    }

    public void shouldNotBeAdminRoleWhenReviewer() throws Exception {
        appraisalsAction.setUserRole(ActionHelper.ROLE_REVIEWER);
        assert !appraisalsAction.isAdminRole();
    }

    public void shouldNotBeAdminRoleWhenEmployee() throws Exception {
        appraisalsAction.setUserRole(ActionHelper.ROLE_EMPLOYEE);
        assert !appraisalsAction.isAdminRole();
    }

    public void shouldBeFourWhenLastSequenceIsThree() throws Exception {
        Map<String, Assessment> dbAssessmentMap = new HashMap<String, Assessment>();
        Assessment assessment1 = new Assessment();
        Assessment assessment2 = new Assessment();
        Assessment assessment3 = new Assessment();
        assessment1.setSequence(1);
        assessment2.setSequence(3);
        assessment3.setSequence(2);
        dbAssessmentMap.put("test1", assessment1);
        dbAssessmentMap.put("test2", assessment2);
        dbAssessmentMap.put("test3", assessment3);
        assert appraisalsAction.calculateAssessmentSequence(dbAssessmentMap) == 4;
    }

    public void shouldBeSixWhenLastSequenceIsFive() throws Exception {
        Map<String, Assessment> dbAssessmentMap = new HashMap<String, Assessment>();
        Assessment assessment1 = new Assessment();
        Assessment assessment2 = new Assessment();
        Assessment assessment3 = new Assessment();
        Assessment assessment4 = new Assessment();
        Assessment assessment5 = new Assessment();
        assessment1.setSequence(1);
        assessment2.setSequence(3);
        assessment3.setSequence(2);
        assessment4.setSequence(4);
        assessment5.setSequence(5);
        dbAssessmentMap.put("test1", assessment1);
        dbAssessmentMap.put("test2", assessment2);
        dbAssessmentMap.put("test3", assessment3);
        dbAssessmentMap.put("test4", assessment4);
        dbAssessmentMap.put("test5", assessment5);
        assert appraisalsAction.calculateAssessmentSequence(dbAssessmentMap) == 6;
    }

    public void shouldGetCorrectSalaryValues() {
        Salary salary = new Salary();
        salary.setTwoIncrease(2.0);
        salary.setOneMin(1.0);
        salary.setOneMax(1.5);
        Appraisal mockedAppraisal = mock(Appraisal.class);
        when(mockedAppraisal.getSalary()).thenReturn(salary);

        appraisalsAction.setAppraisal(mockedAppraisal);
        Map<String, String> salaryValidationValues = appraisalsAction.getSalaryValidationValues();
        assert salaryValidationValues.get("increaseRate2Value").equals("2.0");
        assert salaryValidationValues.get("increaseRate1MinVal").equals("1.0");
        assert salaryValidationValues.get("increaseRate1MaxVal").equals("1.5");
    }

    public void shouldInitializeCorrectly() throws Exception {
        DBUnit dbunit = new DBUnit();
        dbunit.seedDatabase();
        // Get session and begin a transaction
        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();
        // Mock stuff
        PortletRequest mockedRequest = mock(PortletRequest.class);
        ActionHelper mockedActionHelper = mock(ActionHelper.class);
        PropertiesConfiguration mockedConfig = mock(PropertiesConfiguration.class);
        when(mockedConfig.getString("profFaculty.maximized.Message")).thenReturn("null");
        when(mockedActionHelper.getEvalsConfig()).thenReturn(mockedConfig);
        when(mockedRequest.getParameter("id")).thenReturn("1");
        when(mockedActionHelper.getLoggedOnUser()).thenReturn(EmployeeMgr.findByOnid("cedenoj", null));
        when(mockedPortletContext.getAttribute("permissionRules")).thenReturn(PermissionRuleMgr.list());
        when(mockedActionHelper.getPortletContext()).thenReturn(mockedPortletContext);
        appraisalsAction.setActionHelper(mockedActionHelper);
        // Call initialize and commit
        appraisalsAction.initialize(mockedRequest);
        tx.commit();
        // Assertions
        PermissionRule permRule = appraisalsAction.getPermRule();
        assert appraisalsAction.getUserRole().equals(ActionHelper.ROLE_EMPLOYEE);
        assert permRule.getResults() == null;
        assert permRule.getSupervisorResults() == null;
        assert permRule.getStatus().equals("goalsDue");
    }

    public void shouldAddAssessmentCorrectly() throws Exception {
        DBUnit dbunit = new DBUnit();
        dbunit.seedDatabase();
        // Get session and begin a transaction (error without)
        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();
        // Get assessment ids
        appraisal = AppraisalMgr.getAppraisal(1);
        ArrayList<Integer> assessmentIds = new ArrayList<Integer>();
        for(Assessment assessment : appraisal.getAssessmentMap().values()) {
            assessmentIds.add(assessment.getId());
        }
        // Mock stuff
        PortletRequest mockedRequest = mock(PortletRequest.class);
        ActionHelper mockedActionHelper = mock(ActionHelper.class);
        PropertiesConfiguration mockedConfig = mock(PropertiesConfiguration.class);
        PortletResponse mockedResponse = mock(PortletResponse.class);
        when(mockedConfig.getString("profFaculty.maximized.Message")).thenReturn("null");
        when(mockedActionHelper.getEvalsConfig()).thenReturn(mockedConfig);
        when(mockedRequest.getParameter("id")).thenReturn("1");
        when(mockedActionHelper.getLoggedOnUser()).thenReturn(EmployeeMgr.findByOnid("cedenoj", null));
        when(mockedPortletContext.getAttribute("permissionRules")).thenReturn(PermissionRuleMgr.list());
        when(mockedActionHelper.getPortletContext()).thenReturn(mockedPortletContext);
        when(mockedRequest.getParameterMap()).thenReturn(new HashMap<String, String[]>());
        appraisalsAction.setActionHelper(mockedActionHelper);
        // Call addAssessment and commit
        String assessmentResponse = appraisalsAction.addAssessment(mockedRequest, mockedResponse);
        tx.commit();
        // Get appraisal assessment ids after new assessment added
        ArrayList<Integer> newAssessmentIds = new ArrayList<Integer>();
        for(Assessment assessment : appraisal.getAssessmentMap().values()) {
            newAssessmentIds.add(assessment.getId());
        }
        // Parse json object response
        JsonParser parser = new JsonParser();
        JsonObject responseJson = (JsonObject)parser.parse(assessmentResponse);
        // Assertions
        assert !assessmentIds.contains(responseJson.get("id"));
        assert newAssessmentIds.contains(responseJson.get("id").getAsInt());
    }

    // Add new assessment to appraisal

    //
    public void shouldSetAssessmentFieldsCorrectly() throws Exception {
        // init database, session, and begin transaction
        DBUnit dbunit = new DBUnit();
        dbunit.seedDatabase();
        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();
        // init variables
        appraisal = AppraisalMgr.getAppraisal(1);
        Date curDate = new Date();
        ArrayList<Integer> deletedAssessmentsIds = new ArrayList<Integer>();
        Map<String, Assessment> id2assessment = appraisal.getAssessmentMap();
        Employee employee = EmployeeMgr.findByOnid("cedenoj", null);
        Map<String, String[]> requestMap = new HashMap<String, String[]>();
        requestMap.put("json_data", new String[] {appraisalToJSONString()});
        // init mocks
        PortletRequest mockedRequest = mock(PortletRequest.class);
        ActionHelper mockedActionHelper = mock(ActionHelper.class);
        PropertiesConfiguration mockedConfig = mock(PropertiesConfiguration.class);
        PermissionRule mockedPermRule = mock(PermissionRule.class);
        Appraisal mockedAppraisal = mock(Appraisal.class);
        Employee mockedEmployee = mock(Employee.class);
        // when
        when(mockedPermRule.canEdit("unapprovedGoals")).thenReturn(true);
        when(mockedPermRule.canEdit("results")).thenReturn(false);
        when(mockedPermRule.canEdit("supervisorResults")).thenReturn(false);
        when(mockedConfig.getString("profFaculty.maximized.Message")).thenReturn("null");
        when(mockedActionHelper.getEvalsConfig()).thenReturn(mockedConfig);
        when(mockedRequest.getParameter("id")).thenReturn("1");
        when(mockedActionHelper.getLoggedOnUser()).thenReturn(employee);
        when(mockedPortletContext.getAttribute("permissionRules")).thenReturn(PermissionRuleMgr.list());
        when(mockedActionHelper.getPortletContext()).thenReturn(mockedPortletContext);
        when(mockedAppraisal.getAssessmentMap()).thenReturn(appraisal.getAssessmentMap());
        when(mockedAppraisal.getUnapprovedGoalsVersion()).thenReturn(new GoalVersion());
        when(mockedEmployee.getId()).thenReturn(employee.getId());
        // add mocks
        appraisalsAction.setActionHelper(mockedActionHelper);
        appraisalsAction.setAppraisal(mockedAppraisal);
        appraisalsAction.setPermRule(mockedPermRule);
        appraisalsAction.setLoggedInUser(mockedEmployee);
        // init JSON data
        appraisalsAction.initializeJSONData(requestMap);
        // mark every other assessment for deletion
        int count = 0;
        for(Map.Entry<Integer, AssessmentJSON> assessmentJSONEntry : appraisalsAction.getJsonData().getAssessments().entrySet()) {
            if(count % 2 == 0) {
                deletedAssessmentsIds.add(assessmentJSONEntry.getKey());
                assessmentJSONEntry.getValue().setDeleted("1");
            }
            count++;
        }
        // function being tested
        appraisalsAction.setAssessmentFields();
        session.save(appraisal);
        tx.commit();
        // assertions
        System.out.println(id2assessment.size());
        for(Integer id : deletedAssessmentsIds) {
            Assessment curAssessment = id2assessment.get(id.toString());
            assert curAssessment.getDeleteDate().compareTo(curDate) == 1;
            assert curAssessment.getDeleterPidm() == employee.getId();
        }
    }

    /**
     * Transforms an appraisal to a JSON object string
     * @return
     */
    private String appraisalToJSONString() {
        ArrayList<String> appraisalJSONArray = new ArrayList<String>();
        appraisalJSONArray.add("id:" + appraisal.getId());
        appraisalJSONArray.add("goalsComments:\"\"");
        appraisalJSONArray.add("evaluation:\"\"");
        appraisalJSONArray.add("rating:" + appraisal.getRating());
        appraisalJSONArray.add("salaryRecommendation:\"\"");
        appraisalJSONArray.add("review:\"\"");
        appraisalJSONArray.add("rebuttal:\"\"");
        appraisalJSONArray.add("buttonClicked:\"\"");
        ArrayList<String> appraisalAssessmentsArray = new ArrayList<String>();
        int count = 0;
        for(Assessment assessment : appraisal.getAssessmentMap().values()) {
            String idAsString = assessment.getId().toString();
            dbAssessmentMap.put(idAsString, assessment);
            if(count % 3 == 0) {
                appraisalAssessmentsArray.add(idAsString + ":" +
                        assessmentToJSONString(assessment, EMPLOYEE_RESULTS, SUPERVISOR_RESULTS));
            }
            else {
                appraisalAssessmentsArray.add(idAsString + ":" + assessmentToJSONString(assessment));
            }
            count++;
        }
        appraisalJSONArray.add("assessments:{" + StringUtils.join(appraisalAssessmentsArray, ",") + "}");
        String closeOutReasonId = "null";
        if(appraisal.getCloseOutReason() != null) {
            closeOutReasonId = "" + appraisal.getCloseOutReason().getId();
        }
        appraisalJSONArray.add("closeOutReasonId:" + closeOutReasonId);
        return "{" + StringUtils.join(appraisalJSONArray, ",") + "}";
    }

    /**
     * Calls the method with the same name, using the assessment's
     * employee/supervisor results.
     * @param assessment
     * @return
     */
    private String assessmentToJSONString(Assessment assessment) {
        return assessmentToJSONString(assessment, assessment.getEmployeeResult(), assessment.getSupervisorResult());
    }

    /**
     * Returns the given assessment as a JSON string object. Uses the supplied
     * employee results and supervisor results in place of the original from the assessment.
     * @param assessment
     * @param employeeResults
     * @param supervisorResults
     * @return
     */
    private String assessmentToJSONString(Assessment assessment, String employeeResults, String supervisorResults) {
        ArrayList<String> assessmentsJSONArray = new ArrayList<String>();
        assessmentsJSONArray.add("id:\"" + assessment.getId().toString() + "\"");
        assessmentsJSONArray.add("goal:\"" + assessment.getGoal() + "\"");
        assessmentsJSONArray.add("employeeResult:\"" + employeeResults + "\"");
        assessmentsJSONArray.add("supervisorResult:\"" + supervisorResults + "\"");
        String deleted = assessment.isDeleted() ? "\"1\"" : "\"0\"";
        assessmentsJSONArray.add("deleted:" + deleted);
        ArrayList<String> criteriaArray = new ArrayList<String>();
        for(AssessmentCriteria assessmentCriteria : assessment.getAssessmentCriteria()) {
            criteriaArray.add(assessmentCriteria.getId().toString() + ":" + assessmentCriteria.getChecked().toString());
        }
        assessmentsJSONArray.add("criteria:{" + StringUtils.join(criteriaArray, ",") + "}");
        return "{" + StringUtils.join(assessmentsJSONArray, ",") + "}";
    }
}
