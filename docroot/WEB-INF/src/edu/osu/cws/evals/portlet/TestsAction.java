package edu.osu.cws.evals.portlet;

import javax.portlet.*;

public class TestsAction implements ActionInterface {

    public String updateTest(PortletRequest request, PortletResponse response) {
      System.out.println("new update");
      return "true";
    }

    public void setErrorHandler(ErrorHandler errorHandler) { this.errorHandler = errorHandler; }
    public void setHomeAction(HomeAction homeAction) { this.homeAction = homeAction; }
    public void setActionHelper(ActionHelper actionHelper) { this.actionHelper = actionHelper; }
}
