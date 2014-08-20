package edu.osu.cws.evals.tests;

import edu.osu.cws.evals.models.*;
import edu.osu.cws.evals.portlet.ActionHelper;
import edu.osu.cws.evals.portlet.AppraisalsAction;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.mockito.Mockito.*;

import javax.portlet.PortletContext;
import java.util.HashMap;

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
        Employee emp = new Employee();
        Job job = new Job();
        int id = 59999;
        emp.setId(id);
        job.setEmployee(emp);
        appraisal.setJob(job);
        appraisalsAction.setLoggedInUser(emp);
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


}
