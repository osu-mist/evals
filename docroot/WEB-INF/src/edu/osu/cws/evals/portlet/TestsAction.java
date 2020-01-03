package edu.osu.cws.evals.portlet;

import javax.portlet.*;

public class TestsAction {

    public String updateTest(PortletRequest request, PortletResponse response) {
      System.out.println("new update");
      return "true";
    }
}
