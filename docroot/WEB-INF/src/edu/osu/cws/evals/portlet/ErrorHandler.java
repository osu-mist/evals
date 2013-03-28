package edu.osu.cws.evals.portlet;

import edu.osu.cws.evals.models.Evals;
import edu.osu.cws.evals.portlet.ActionHelper;
import edu.osu.cws.evals.portlet.HomeAction;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import java.util.ResourceBundle;

public class ErrorHandler extends Evals {

    private static ActionHelper actionHelper;

    private static HomeAction homeAction = new HomeAction();

    public static String handleAccessDenied(PortletRequest request, PortletResponse response)
            throws Exception {
        ResourceBundle resources =
                (ResourceBundle) actionHelper.getPortletContextAttribute("resourceBundle");
        actionHelper.addErrorsToRequest(resources.getString("access-denied"));
        return homeAction.display(request, response);
    }

}
