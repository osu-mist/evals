package edu.osu.cws.evals.tests;

import edu.osu.cws.evals.models.*;
import edu.osu.cws.evals.portlet.ActionHelper;
import edu.osu.cws.evals.portlet.AppraisalsAction;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.mockito.Mockito.*;

import javax.portlet.PortletContext;
import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

@Test
public class AppraisalsActionTest {

    private Appraisal appraisal;
    private PortletContext mockedPortletContext;
    private AppraisalsAction appraisalsAction;

    @BeforeMethod
    public void setup() {
        appraisal = new Appraisal();
        appraisal.setJob(new Job());
        appraisal.getJob().setAppointmentType(AppointmentType.CLASSIFIED);

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

    public void shouldGetCorrectRoleWhenUpperAdmin() throws Exception {
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
}
