package edu.osu.cws.evals.models;

import edu.osu.cws.evals.portlet.ActionHelper;
import edu.osu.cws.evals.portlet.HomeAction;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import java.util.ResourceBundle;

public class ErrorHandler extends Evals {

    private static ActionHelper actionHelper = new ActionHelper();
    private static HomeAction homeAction = new HomeAction();

    public ErrorHandler() {
    }

    public static String handleAccessDenied(PortletRequest request, PortletResponse response) throws Exception {
        ResourceBundle resource = (ResourceBundle) actionHelper.getPortletContextAttribute("resourceBundle");
        actionHelper.addErrorsToRequest(request, resource.getString("access-denied"));
        return homeAction.display(request, response);
    }

}
