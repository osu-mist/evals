package edu.osu.cws.evals.portlet;

import com.liferay.portal.kernel.util.ParamUtil;
import edu.osu.cws.evals.hibernate.NoticeMgr;
import edu.osu.cws.evals.models.Employee;
import edu.osu.cws.evals.models.ModelException;
import edu.osu.cws.evals.models.Notice;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.RenderRequest;
import java.util.ArrayList;
import java.util.Date;

public class NoticeAction implements ActionInterface {

    private ActionHelper actionHelper;

    private HomeAction homeAction;

    private ErrorHandler errorHandler;

    /**
     * Handles listing the notice.
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public String list(PortletRequest request, PortletResponse response) throws Exception {
        // Check that the logged in user is admin
        boolean isAdmin = actionHelper.getAdmin() != null;
        if (!isAdmin) {
            return errorHandler.handleAccessDenied(request, response);
        }

        ArrayList<Notice> noticeList = NoticeMgr.list();
        actionHelper.addToRequestMap("noticeList", noticeList);
        actionHelper.useMaximizedMenu();

        return Constants.JSP_NOTICE_LIST;
    }


    /**
     * edit the notice of provided ancestorID, insert a new notice instead of edit the existed one
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public String edit(PortletRequest request, PortletResponse response) throws Exception {
        // Check that the logged in user is admin
        boolean isAdmin = actionHelper.getAdmin() != null;
        if (!isAdmin) {
            return errorHandler.handleAccessDenied(request, response);
        }
        Notice notice = new Notice();
        try {
            int ancestorId = ParamUtil.getInteger(request, "ancestorID") ;
            if (request instanceof RenderRequest) {
                notice = NoticeMgr.get(ancestorId);
            } else {
                String text = ParamUtil.getString(request, "text");
                Employee loggedOnUser = actionHelper.getLoggedOnUser();
                notice.setAncestorID(ancestorId);
                notice.setCreator(loggedOnUser);
                notice.setCreateDate(new Date());
                notice.setName(ParamUtil.getString(request, "name"));
                notice.setText(text);
                boolean noticeChange = NoticeMgr.compareNotices(notice);
                //if the current notice is a yellowBoxMessage and it is changed, update it into
                //portletContext so the other loggedInUser can see it.
                if(noticeChange){
                    actionHelper.updateContextTimestamp();
                    actionHelper.setAdminPortletData();
                }
                return list(request, response);
            }
        } catch (ModelException e) {
            actionHelper.addErrorsToRequest(e.getMessage());
        }

        actionHelper.addToRequestMap("notice", notice);
        actionHelper.useMaximizedMenu();

        return Constants.JSP_NOTICE_EDIT;
    }

    public void setActionHelper(ActionHelper actionHelper) {
        this.actionHelper = actionHelper;
    }

    public void setHomeAction(HomeAction homeAction) {
        this.homeAction = homeAction;
    }

    public void setErrorHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }
}

