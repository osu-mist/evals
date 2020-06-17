package edu.osu.cws.evals.portlet;

import edu.osu.cws.evals.models.Employee;
import edu.osu.cws.evals.models.Job;
import edu.osu.cws.evals.models.Appraisal;
import edu.osu.cws.evals.hibernate.AppraisalMgr;
import edu.osu.cws.evals.hibernate.EmployeeMgr;
import edu.osu.cws.evals.hibernate.JobMgr;
import edu.osu.cws.evals.hibernate.AdminMgr;

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

    public Employee createEmployee(PortletRequest request) throws Exception {
      System.out.println("Create Employee");

      String firstName = request.getParameter("firstName");
      String lastName = request.getParameter("lastName");
      String onid = request.getParameter("onid");
      String email = request.getParameter("email");

      return EmployeeMgr.createEmployee(lastName, firstName, onid, email);
    }

    public Job createJob(PortletRequest request, Employee employee) throws Exception {
      System.out.println("Create Job");

      String appointmentType = request.getParameter("appointmentType");

      return JobMgr.createJob(employee, appointmentType);
    }

    public String createPerson(PortletRequest request, PortletResponse response) throws Exception {
      Employee employee = createEmployee(request);
      Job job = createJob(request, employee);
      System.out.println(request.getParameter("admin"));
      if ("true".equals(request.getParameter("admin"))) {
        System.out.println("create admin");
        AdminMgr.add(employee.getOnid(), "1", actionHelper.getLoggedOnUser());
        actionHelper.updateContextTimestamp();
        actionHelper.setAdminPortletData();
      }

      return homeAction.display(request, response);
    }

    public void setErrorHandler(ErrorHandler errorHandler) { this.errorHandler = errorHandler; }
    public void setHomeAction(HomeAction homeAction) { this.homeAction = homeAction; }
    public void setActionHelper(ActionHelper actionHelper) { this.actionHelper = actionHelper; }
}
