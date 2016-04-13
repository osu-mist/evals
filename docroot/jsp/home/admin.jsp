<jsp:useBean id="admin" class="edu.osu.cws.evals.models.Admin" scope="request" />
<%
PortletURL criteriaListURL = renderResponse.createRenderURL();
criteriaListURL.setWindowState(WindowState.MAXIMIZED);
criteriaListURL.setParameter("action", "list");
criteriaListURL.setParameter("controller", "CriteriaAreasAction");

PortletURL adminListURL = renderResponse.createRenderURL();
adminListURL.setWindowState(WindowState.MAXIMIZED);
adminListURL.setParameter("action", "list");
adminListURL.setParameter("controller", "AdminsAction");

PortletURL reviewerListURL = renderResponse.createRenderURL();
reviewerListURL.setWindowState(WindowState.MAXIMIZED);
reviewerListURL.setParameter("action", "list");
reviewerListURL.setParameter("controller", "ReviewersAction");

PortletURL configurationListURL = renderResponse.createRenderURL();
configurationListURL.setWindowState(WindowState.MAXIMIZED);
configurationListURL.setParameter("action", "list");
configurationListURL.setParameter("controller", "ConfigurationsAction");

PortletURL closeOutReasonListURL = renderResponse.createRenderURL();
closeOutReasonListURL.setWindowState(WindowState.MAXIMIZED);
closeOutReasonListURL.setParameter("action", "list");
closeOutReasonListURL.setParameter("controller", "CloseOutAction");

PortletURL noticeListURL = renderResponse.createRenderURL();
noticeListURL.setWindowState(WindowState.MAXIMIZED);
noticeListURL.setParameter("action", "list");
noticeListURL.setParameter("controller", "NoticeAction");

PortletURL reviewCycleListURL = renderResponse.createRenderURL();
reviewCycleListURL.setWindowState(WindowState.MAXIMIZED);
reviewCycleListURL.setParameter("action", "list");
reviewCycleListURL.setParameter("controller", "ReviewCycleAction");
%>

<c:if test="${isAdmin == 'true'}">
    <div id="<portlet:namespace/>accordionMenuPassAdmin" class="accordion-menu">
        <div class="osu-accordion-header" onclick="<portlet:namespace/>toggleContent('<portlet:namespace/>passAdmin');">
          <img id="<portlet:namespace/>passAdminImageToggle" src="/images/accordion/accordion_arrow_up.png"/>
          <liferay-ui:message key="admin-section" />
        </div>
        <div class="accordion-content" id="<portlet:namespace/>passAdmin" style="display: block;">
            <ul class="pass-menu-list">
                <li>
                    <a href="<%= criteriaListURL.toString() %>">Evaluation Criteria</a>
                </li>
                <li>
                    <a href="<%= adminListURL.toString() %>"><liferay-ui:message key="admins-list-title"/></a>
                </li>
                <li>
                    <a href="<%= reviewerListURL.toString() %>"><liferay-ui:message key="reviewers-list-title"/></a>
                </li>
                <li>
                    <a href="<%= configurationListURL.toString() %>"><liferay-ui:message key="configurations-list-title"/></a>
                </li>
                <li>
                    <a href="<%= closeOutReasonListURL.toString() %>">
                        <liferay-ui:message key="closeout-reason-list-title"/></a>
                </li>
                <li>
                    <a href="<%= noticeListURL.toString() %>">
                        <liferay-ui:message key="notice-list-title"/></a>
                </li>
                <li>
                    <a href="<%= reviewCycleListURL.toString() %>">
                        <liferay-ui:message key="review-cycle-option-list-title"/></a>
                </li>
            </ul>
        </div>
    </div>
</c:if>