package edu.osu.cws.evals.portlet;

import com.liferay.portal.kernel.util.ParamUtil;
import edu.osu.cws.evals.hibernate.NoticeMgr;
import edu.osu.cws.evals.models.Employee;
import edu.osu.cws.evals.models.Notice;
import edu.osu.cws.evals.models.ModelException;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.RenderRequest;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class NoticeAction implements ActionInterface {

    private ActionHelper actionHelper;

    private HomeAction homeAction;
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
        if (!actionHelper.isLoggedInUserAdmin(request)) {
            actionHelper.addErrorsToRequest(request, ActionHelper.ACCESS_DENIED);
            return homeAction.display(request, response);
        }

        ArrayList<Notice> noticeList = NoticeMgr.list();
        actionHelper.addToRequestMap("noticeList", noticeList, request);
        actionHelper.useMaximizedMenu(request);

        return Constants.JSP_NOTICE_LIST;
    }


    /**
     * put a new version of file in a single ancestorID.
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
        Notice notice = new Notice();
        try {
            int ancestorId = ParamUtil.getInteger(request, "ancestorID") ;
            if (request instanceof RenderRequest) {

                notice = NoticeMgr.get(ancestorId);
            } else {
                Employee loggedOnUser = actionHelper.getLoggedOnUser(request);
                notice = new Notice();
                notice.setAncestorID(ancestorId);
                notice.setCreator(loggedOnUser);
                Calendar calendar = Calendar.getInstance();
                Date date = calendar.getTime();
                notice.setCreateDate(date);
                notice.setName(ParamUtil.getString(request, "name"));
                notice.setText(ParamUtil.getString(request, "text"));
                NoticeMgr.edit(notice);
                return list(request, response);
           }
       } catch (ModelException e) {
           actionHelper.addErrorsToRequest(request, e.getMessage());
       }

        actionHelper.addToRequestMap("notice", notice, request);
        actionHelper.useMaximizedMenu(request);

       return Constants.JSP_NOTICE_EDIT;
   }




    public void setActionHelper(ActionHelper actionHelper) {
        this.actionHelper = actionHelper;
    }

    public void setHomeAction(HomeAction homeAction) {
        this.homeAction = homeAction;
    }

}

