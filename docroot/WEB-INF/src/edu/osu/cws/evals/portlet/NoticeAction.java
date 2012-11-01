package edu.osu.cws.evals.portlet;

import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.util.ParamUtil;
import edu.osu.cws.evals.hibernate.NoticeMgr;
import edu.osu.cws.evals.models.Notice;
import edu.osu.cws.evals.models.ModelException;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import java.util.ArrayList;

public class NoticeAction implements ActionInterface {
    private ActionHelper actionHelper;

    private HomeAction homeAction;

    /**
     * Handles listing the close out reasons.
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public String list(PortletRequest request, PortletResponse response) throws Exception {
        // Check that the logged in user is admin
        if (!actionHelper.isLoggedInUserAdmin(request)) {
            actionHelper.addErrorsToRequest(request, ActionHelper.ACCESS_DENIED);
            return homeAction.display(request, response);
        }

        ArrayList<Notice> noticeList = NoticeMgr.list();
        actionHelper.addToRequestMap("noticeList", noticeList, request);
        actionHelper.useMaximizedMenu(request);

        return Constants.JSP_CLOSEOUT_REASON_LIST;
    }

    /**
     * Handles adding a close out reason. If successful, it displays the list of close out reasons.
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public String add(PortletRequest request, PortletResponse response) throws Exception {
        // Check that the logged in user is admin
        if (!actionHelper.isLoggedInUserAdmin(request)) {
            actionHelper.addErrorsToRequest(request, ActionHelper.ACCESS_DENIED);
            return homeAction.display(request, response);
        }

        String name = ParamUtil.getString(request, "name");
        String text = ParamUtil.getString(request, "text");
        try {
            NoticeMgr.add(actionHelper.getLoggedOnUser(request), name, text);
            SessionMessages.add(request, "notice-added");
        } catch (ModelException e) {
            actionHelper.addErrorsToRequest(request, e.getMessage());
        } catch (Exception e) {
            throw e;
        }

        return list(request, response);
    }

    /**
     * Handles performing a soft delete of a single close out reason.
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public String edit(PortletRequest request, PortletResponse response) throws Exception {
        // Check that the logged in user is admin
        if (!actionHelper.isLoggedInUserAdmin(request)) {
            actionHelper.addErrorsToRequest(request, ActionHelper.ACCESS_DENIED);
            return homeAction.display(request, response);
        }

        String editName = ParamUtil.getString(request, "name");
        String editText = ParamUtil.getString(request, "text");
        int ancestorId = ParamUtil.getInteger(request, "ancestorID")  ;
        try {
            NoticeMgr.edit(actionHelper.getLoggedOnUser(request), ancestorId, editName, editText);
            SessionMessages.add(request, "notice-edited");
        } catch (ModelException e) {
            actionHelper.addErrorsToRequest(request, e.getMessage());
        } catch (Exception e) {
            throw e;
        }

        return list(request, response);
    }

    public void setActionHelper(ActionHelper actionHelper) {
        this.actionHelper = actionHelper;
    }

    public void setHomeAction(HomeAction homeAction) {
        this.homeAction = homeAction;
    }

}

