package edu.osu.cws.evals.portlet;

import edu.osu.cws.evals.models.Employee;

import javax.portlet.*;

public class TestsAction implements ActionInterface {

    private ActionHelper actionHelper;

    private HomeAction homeAction;

    private ErrorHandler errorHandler;

    public String updateTest(PortletRequest request, PortletResponse response) throws exception {
      PortletSession session = ActionHelper.getSession(request);
      System.out.println("new update");

      Employee employee = (Employee)session.getAttribute("loggedOnUser");
      System.out.println(employee.getFirstName());
      return "true";
    }

    public void setErrorHandler(ErrorHandler errorHandler) { this.errorHandler = errorHandler; }
    public void setHomeAction(HomeAction homeAction) { this.homeAction = homeAction; }
    public void setActionHelper(ActionHelper actionHelper) { this.actionHelper = actionHelper; }
}
