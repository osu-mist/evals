package edu.osu.cws.evals.portlet;

import javax.portlet.*;

public class TestsAction implements ActionInterface {

    private ActionHelper actionHelper;

    private HomeAction homeAction;

    private ErrorHandler errorHandler;

    public String updateTest(PortletRequest request, PortletResponse response) {
      PortletSession session = ActionHelper.getSession(request);
      System.out.println("new update");

      Employee employee = session.getAttribute("loggedOnUser");
      System.out.println(employee.getFirstName());
      return "true";
    }

    public void setErrorHandler(ErrorHandler errorHandler) { this.errorHandler = errorHandler; }
    public void setHomeAction(HomeAction homeAction) { this.homeAction = homeAction; }
    public void setActionHelper(ActionHelper actionHelper) { this.actionHelper = actionHelper; }
}
