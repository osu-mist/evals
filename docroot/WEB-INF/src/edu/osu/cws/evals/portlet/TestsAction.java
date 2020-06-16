package edu.osu.cws.evals.portlet;

import edu.osu.cws.evals.models.Employee;
import edu.osu.cws.evals.models.Job;
import edu.osu.cws.evals.models.Appraisal;
import edu.osu.cws.evals.hibernate.AppraisalMgr;
import edu.osu.cws.evals.hibernate.EmployeeMgr;

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

    public String createEmployee(PortletRequest request, PortletResponse response) throws Exception {
      System.out.println("Create Employee");

      String firstName =request.getParameter("firstName");
      String lastName = request.getParameter("lastName");

      EmployeeMgr.createEmployee(80000554, "932776672", lastName, firstName, "JoestarJ", "joestar@test.com");

      return homeAction.display(request, response);
    }

    public void setErrorHandler(ErrorHandler errorHandler) { this.errorHandler = errorHandler; }
    public void setHomeAction(HomeAction homeAction) { this.homeAction = homeAction; }
    public void setActionHelper(ActionHelper actionHelper) { this.actionHelper = actionHelper; }
}
