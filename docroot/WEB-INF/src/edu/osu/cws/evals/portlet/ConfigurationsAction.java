package edu.osu.cws.evals.portlet;

import com.liferay.portal.kernel.util.ParamUtil;
import edu.osu.cws.evals.hibernate.ConfigurationMgr;
import edu.osu.cws.evals.models.Configuration;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class ConfigurationsAction implements ActionInterface {
    private ActionHelper actionHelper;

    private HomeAction homeAction;
    
    /**
     * Handles listing the configuration parameters.
     *
     * @param request
     * @param response
     * @return
     */
    public String list(PortletRequest request, PortletResponse response) throws Exception {
        // Check that the logged in user is admin
        ResourceBundle resource = (ResourceBundle) actionHelper.getPortletContextAttribute("resourceBundle");
        if (!actionHelper.isLoggedInUserAdmin()) {
            actionHelper.addErrorsToRequest(resource.getString("access-denied"));
            return homeAction.display(request, response);
        }

        actionHelper.refreshContextCache();
        ArrayList<Configuration> configurations = (ArrayList<Configuration>)
                actionHelper.getPortletContextAttribute("configurationsList");
        actionHelper.addToRequestMap("configurations", configurations);
        actionHelper.useMaximizedMenu();

        return Constants.JSP_CONFIGURATION_LIST;
    }

    /**
     * Handles updating a configuration parameter. This method is only called using ajax.
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public String edit(PortletRequest request, PortletResponse response) throws Exception {
        // Check that the logged in user is admin
        ResourceBundle resource = (ResourceBundle) actionHelper.getPortletContextAttribute("resourceBundle");
        if (!actionHelper.isLoggedInUserAdmin()) {
            actionHelper.addErrorsToRequest(resource.getString("access-denied"));
            return homeAction.display(request, response);
        }

        int id = ParamUtil.getInteger(request, "id");
        String value = ParamUtil.getString(request, "value");

        if (id != 0) {
            try {
                ConfigurationMgr configurationMgr = new ConfigurationMgr();
                configurationMgr.edit(id, value);
                actionHelper.setEvalsConfiguration(true);
            } catch (Exception e) {
                return e.getMessage();
            }
        }

        return "success";
    }

    public void setActionHelper(ActionHelper actionHelper) {
        this.actionHelper = actionHelper;
    }

    public void setHomeAction(HomeAction homeAction) {
        this.homeAction = homeAction;
    }
}
