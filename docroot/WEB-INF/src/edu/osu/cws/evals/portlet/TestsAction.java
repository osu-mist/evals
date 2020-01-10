package edu.osu.cws.evals.portlet;

import edu.osu.cws.evals.models.Employee;
import edu.osu.cws.evals.models.Job;
import edu.osu.cws.evals.models.Appraisal;
import edu.osu.cws.evals.hibernate.AppraisalMgr;

import javax.portlet.*;

import java.util.Set;
import org.joda.time.DateTime;

public class TestsAction implements ActionInterface {

    private ActionHelper actionHelper;

    private HomeAction homeAction;

    private ErrorHandler errorHandler;

    public String updateTest(PortletRequest request, PortletResponse response) throws Exception {
      PortletSession session = ActionHelper.getSession(request);
      System.out.println("new update");

      Employee employee = (Employee)session.getAttribute("loggedOnUser");
      Set<Job> jobs = employee.getNonTerminatedJobs();
      for(Job job : jobs) {
        System.out.println("Creating appraisal for: " + job.getJobTitle());
        AppraisalMgr.createAppraisal(job, new DateTime(2019, 11, 15, 0, 0), Appraisal.TYPE_ANNUAL);
        System.out.println("appraisal created");
      }

      return homeAction.display(request, response);
    }

    public String deleteAppraisal(PortletRequest request, PortletResponse response) throws Exception {
      System.out.println("delete appraisal");
      System.out.println(request.getParameter("id"));
      System.out.println(actionHelper.getMyAppraisals().size());
      int appraisalId = Integer.parseInt(request.getParameter("id"));
      Appraisal appraisal = AppraisalMgr.getAppraisal(appraisalId);
      AppraisalMgr.deleteAppraisal(appraisal);
      actionHelper.resetMyAppraisals();

      System.out.println(actionHelper.getMyAppraisals().size());
      return homeAction.display(request, response);
    }

    public void setErrorHandler(ErrorHandler errorHandler) { this.errorHandler = errorHandler; }
    public void setHomeAction(HomeAction homeAction) { this.homeAction = homeAction; }
    public void setActionHelper(ActionHelper actionHelper) { this.actionHelper = actionHelper; }
}
