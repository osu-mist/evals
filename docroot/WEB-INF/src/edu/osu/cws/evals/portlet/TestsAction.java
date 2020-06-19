package edu.osu.cws.evals.portlet;

import com.liferay.portal.kernel.util.ParamUtil;
import edu.osu.cws.evals.models.Employee;
import edu.osu.cws.evals.models.Job;
import edu.osu.cws.evals.models.Appraisal;
import edu.osu.cws.evals.models.GoalVersion;
import edu.osu.cws.evals.models.Assessment;
import edu.osu.cws.evals.hibernate.AppraisalMgr;
import edu.osu.cws.evals.hibernate.EmployeeMgr;
import edu.osu.cws.evals.hibernate.JobMgr;
import edu.osu.cws.evals.hibernate.AdminMgr;
import edu.osu.cws.evals.hibernate.ReviewerMgr;

import javax.portlet.*;

import java.util.*;
import org.joda.time.DateTime;

public class TestsAction implements ActionInterface {

    private ActionHelper actionHelper;

    private HomeAction homeAction;

    private ErrorHandler errorHandler;

    public String createAppraisal(PortletRequest request, PortletResponse response) throws Exception {
      PortletSession session = actionHelper.getSession(request);
      System.out.println("new update");

      Employee employee = (Employee)session.getAttribute("loggedOnUser");
      Set<Job> jobs = employee.getNonTerminatedJobs();
      for(Job job : jobs) {
        System.out.println("Creating appraisal for: " + job.getJobTitle());
        AppraisalMgr.createAppraisal(job, new DateTime(2019, 11, 15, 0, 0), Appraisal.TYPE_ANNUAL);
        System.out.println("appraisal created");
      }
      actionHelper.reloadMyAppraisals();

      return homeAction.display(request, response);
    }

    public String deleteAppraisal(PortletRequest request, PortletResponse response) throws Exception {
      System.out.println("delete appraisal");
      int appraisalId = Integer.parseInt(request.getParameter("id"));
      Appraisal appraisal = AppraisalMgr.getAppraisal(appraisalId);
      if (appraisal != null) {
        AppraisalMgr.deleteAppraisal(appraisal);
        actionHelper.reloadMyAppraisals();
      }

      return homeAction.display(request, response);
    }

    public Employee createEmployee(Map<String, String> parameters) throws Exception {
      System.out.println("Create Employee");

      String firstName = parameters.get("firstName");
      String lastName = parameters.get("lastName");
      String onid = parameters.get("onid");
      String email = parameters.get("email");

      return EmployeeMgr.createEmployee(lastName, firstName, onid, email);
    }

    public void createSupervisorEmployees(Employee supervisor, String appointmentType, Job supJob) throws Exception {
      List<Employee> employees = new ArrayList<Employee>();
      for(int i = 1; i <= 2; i++) {
        String name = "employee" + String.valueOf(i);
        String empOnid = "emp" + String.valueOf(i) + supervisor.getLastName().toLowerCase();
        empOnid = empOnid.substring(0, Math.min(empOnid.length(), 8));
        employees.add(EmployeeMgr.createEmployee(name, supervisor.getLastName(), empOnid, "employee@test.com"));
      }

      for(Employee employee : employees) {
        JobMgr.createJob(employee, appointmentType, supJob);
      }
    }

    public Job createJob(Map<String, String> parameters, Employee employee) throws Exception {
      System.out.println("Create Job");

      String appointmentType = parameters.get("appointmentType");

      Job job = JobMgr.createJob(employee, appointmentType);
      job.setId(employee.getId());

      if("true".equals(parameters.get("reviewer"))) {
        System.out.println("create reviewer");
        ReviewerMgr.add(employee.getOnid(), parameters.get("businessCenter").substring(0, 4));
        actionHelper.updateContextTimestamp();
        actionHelper.setAdminPortletData();
        parameters.put("reviewer", "false");
        parameters.put("supervisor", "true");
      }

      if ("true".equals(parameters.get("supervisor"))) {
        System.out.println("create supervisor");
        createSupervisorEmployees(employee, appointmentType, job);
      }

      return job;
    }

    private void createPerson(Map<String, String> parameters) throws Exception {
      Employee employee = createEmployee(parameters);
      Job job = createJob(parameters, employee);

      if ("true".equals(parameters.get("admin"))) {
        System.out.println("create admin");
        AdminMgr.add(employee.getOnid(), "1", actionHelper.getLoggedOnUser());
        actionHelper.updateContextTimestamp();
        actionHelper.setAdminPortletData();
      }
    }

    public String createPerson(PortletRequest request, PortletResponse response) throws Exception {
      // System.out.println(request.getParameterMap().get("firstName"));
      Map<String, String> parameters = new HashMap<String, String>();
      Enumeration<String> names = request.getParameterNames();
      while(names.hasMoreElements()) {
        String key = names.nextElement();
        parameters.put(key, request.getParameter(key));
      }
      createPerson(parameters);

      return homeAction.display(request, response);
    }

    public String advanceAppraisal(PortletRequest request, PortletResponse response) throws Exception {
      int appraisalId = ParamUtil.getInteger(request, "id");
      Appraisal appraisal = AppraisalMgr.getAppraisal(appraisalId);
      for (GoalVersion goalVersion : modifiedAppraisal.getGoalVersions()) {
        for (Assessment assessment : goalVersion.getAssessments()) {
          System.out.println(assessment.getGoal());
        }
      }

      return homeAction.display(request, response);
    }

    public void setErrorHandler(ErrorHandler errorHandler) { this.errorHandler = errorHandler; }
    public void setHomeAction(HomeAction homeAction) { this.homeAction = homeAction; }
    public void setActionHelper(ActionHelper actionHelper) { this.actionHelper = actionHelper; }
}
