package edu.osu.cws.evals.portlet;

import edu.osu.cws.evals.models.Employee;
import edu.osu.cws.evals.models.Job;

import javax.portlet.*;

import java.util.Set;

public class TestsAction implements ActionInterface {

    private ActionHelper actionHelper;

    private HomeAction homeAction;

    private ErrorHandler errorHandler;

    public String updateTest(PortletRequest request, PortletResponse response) throws Exception {
      PortletSession session = ActionHelper.getSession(request);
      System.out.println("new update");

      Employee employee = (Employee)session.getAttribute("loggedOnUser");
      Set<Job> jobs = new Set<Job>(employee.getNonTerminatedJobs());
      for(Job job : jobs) {
        System.out.println(job.getJobTitle());
      }
      return "true";
    }

    public void setErrorHandler(ErrorHandler errorHandler) { this.errorHandler = errorHandler; }
    public void setHomeAction(HomeAction homeAction) { this.homeAction = homeAction; }
    public void setActionHelper(ActionHelper actionHelper) { this.actionHelper = actionHelper; }
}
