package edu.osu.cws.evals.portlet;

import edu.osu.cws.evals.util.HibernateUtil;
import org.hibernate.Session;
import com.liferay.portal.kernel.util.ParamUtil;
import edu.osu.cws.evals.models.Employee;
import edu.osu.cws.evals.models.Job;
import edu.osu.cws.evals.models.Appraisal;
import edu.osu.cws.evals.models.GoalVersion;
import edu.osu.cws.evals.models.Assessment;
import edu.osu.cws.evals.models.AssessmentCriteria;
import edu.osu.cws.evals.models.Salary;
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

    public void createAppraisal(Employee employee) throws Exception {
      Session hibSession = HibernateUtil.getCurrentSession();

      Set<Job> jobs = employee.getNonTerminatedJobs();
      for(Job job : jobs) {
        System.out.println("Creating appraisal for: " + job.getJobTitle());
        Appraisal appraisal = AppraisalMgr.createAppraisal(job, new DateTime(2019, 11, 15, 0, 0), Appraisal.TYPE_ANNUAL);
        Salary salary = new Salary(appraisal.getId());
        hibSession.save(salary);
        System.out.println("appraisal created");
      }
    }

    public String createAppraisal(PortletRequest request, PortletResponse response) throws Exception {
      PortletSession session = actionHelper.getSession(request);

      Employee employee = (Employee)session.getAttribute("loggedOnUser");
      createAppraisal(employee);
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
        Job job = JobMgr.createJob(employee, "Classified IT", supJob);
        Set<Job> jobSet = new HashSet<Job>();
        jobSet.add(job);
        employee.setJobs(jobSet);
        createAppraisal(employee);
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
      Session session = HibernateUtil.getCurrentSession();
      // for (GoalVersion goalVersion : appraisal.getGoalVersions()) {
        // for (Assessment assessment : goalVersion.getAssessments()) {
          // System.out.println(assessment.getGoal());
        // }
      // }

      String status = appraisal.getStatus();
      if("goalsDue".equals(status) || "goalsOverdue".equals(status)) {
        for (GoalVersion goalVersion : appraisal.getGoalVersions()) {
          for (Assessment assessment : goalVersion.getAssessments()) {
            if(assessment.getGoal() == null || assessment.getGoal().isEmpty()) {
              assessment.setGoal("autocompleted goal");
            }
            for(AssessmentCriteria crit : assessment.getAssessmentCriteria()) {
              crit.setChecked(true);
            }
          }
        }
        appraisal.getUnapprovedGoalsVersion().setGoalsSubmitDate(new Date());
        appraisal.setStatus("goalsApprovalDue");
        session.save(appraisal);
      }

      if("goalsApprovalDue".equals(status) || "goalsApprovalOverdue".equals(status)) {
        for (GoalVersion goalVersion : appraisal.getGoalVersions()) {
          goalVersion.setGoalsApprovedDate(new Date());
          if (goalVersion.getGoalsComments() == null || goalVersion.getGoalsComments().isEmpty()) {
            goalVersion.setGoalsComments("autocompleted goals comment");
          }
          goalVersion.setGoalsApproverPidm(appraisal.getJob().getSupervisor().getEmployee().getId());
          appraisal.setStatus("resultsDue");
        }
        session.save(appraisal);
      }

      if(Appraisal.STATUS_GOALS_APPROVED.equals(status)) {
        appraisal.setStatus(Appraisal.STATUS_RESULTS_DUE);
        session.save(appraisal);
      }

      if(Appraisal.STATUS_RESULTS_DUE.equals(status) || Appraisal.STATUS_RESULTS_OVERDUE.equals(status)) {
        for (GoalVersion goalVersion : appraisal.getGoalVersions()) {
          for (Assessment assessment : goalVersion.getAssessments()) {
            if(assessment.getEmployeeResult() == null || assessment.getEmployeeResult().isEmpty()) {
              assessment.setEmployeeResult("autocompleted employee result");
            }
          }
        }
        appraisal.setResultSubmitDate(new Date());
        appraisal.setStatus(Appraisal.STATUS_APPRAISAL_DUE);
        session.save(appraisal);
      }

      if(Appraisal.STATUS_APPRAISAL_DUE.equals(status) || Appraisal.STATUS_APPRAISAL_OVERDUE.equals(status)) {
        for (GoalVersion goalVersion : appraisal.getGoalVersions()) {
          for (Assessment assessment : goalVersion.getAssessments()) {
            if(assessment.getSupervisorResult() == null || assessment.getSupervisorResult().isEmpty()) {
              assessment.setSupervisorResult("autocompleted supervisor result");
            }
          }
        }
        appraisal.setRating(1);
        appraisal.setEvaluation("autocompleted evaluation");
        appraisal.setEvaluator(appraisal.getJob().getSupervisor().getEmployee());
        appraisal.setEvaluationSubmitDate(new Date());
        appraisal.setStatus(Appraisal.STATUS_REVIEW_DUE);
        session.save(appraisal);
      }

      if(Appraisal.STATUS_REVIEW_DUE.equals(status) || Appraisal.STATUS_REVIEW_OVERDUE.equals(status)) {
        appraisal.setReviewSubmitDate(new Date());
        appraisal.setReview("autocompleted review");
        appraisal.setReviewer(appraisal.getJob().getSupervisor().getEmployee());
        appraisal.setStatus(Appraisal.STATUS_RELEASE_DUE);
        session.save(appraisal);
      }

      if(Appraisal.STATUS_EMPLOYEE_REVIEW_DUE.equals(status)) {
        appraisal.setReviewSubmitDate(new Date());
        appraisal.setReview("autocompleted employee review");
        appraisal.setReviewer(appraisal.getJob().getEmployee());
        appraisal.setStatus(Appraisal.STATUS_RELEASE_DUE);
        session.save(appraisal);
      }

      if(Appraisal.STATUS_RELEASE_DUE.equals(status) || Appraisal.STATUS_RELEASE_OVERDUE.equals(status)) {
        appraisal.setReleaseDate(new Date());
        appraisal.setStatus(appraisal.STATUS_SIGNATURE_DUE);
        session.save(appraisal);
      }

      if(Appraisal.STATUS_SIGNATURE_DUE.equals(status) || Appraisal.STATUS_SIGNATURE_OVERDUE.equals(status)) {
        appraisal.setStatus(Appraisal.STATUS_COMPLETED);
        appraisal.setEvaluationSubmitDate(new Date());
        appraisal.setEmployeeSignedDate(new Date());
        session.save(appraisal);
      }

      System.out.println(appraisal.getStatus());

      actionHelper.reloadMyAppraisals();
      actionHelper.setupMyTeamActiveAppraisals();
      actionHelper.setAdminPortletData();
      return homeAction.display(request, response);
    }

    public void setErrorHandler(ErrorHandler errorHandler) { this.errorHandler = errorHandler; }
    public void setHomeAction(HomeAction homeAction) { this.homeAction = homeAction; }
    public void setActionHelper(ActionHelper actionHelper) { this.actionHelper = actionHelper; }
}
