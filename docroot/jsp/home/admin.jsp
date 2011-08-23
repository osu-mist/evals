<jsp:useBean id="admin" class="edu.osu.cws.pass.models.Admin" scope="request" />
<%
PortletURL criteriaListURL = renderResponse.createRenderURL();
criteriaListURL.setWindowState(WindowState.MAXIMIZED);
criteriaListURL.setParameter("action", "listCriteria");

PortletURL adminListURL = renderResponse.createRenderURL();
adminListURL.setWindowState(WindowState.MAXIMIZED);
adminListURL.setParameter("action", "listAdmin");

PortletURL reviewerListURL = renderResponse.createRenderURL();
reviewerListURL.setWindowState(WindowState.MAXIMIZED);
reviewerListURL.setParameter("action", "listReviewer");

PortletURL configurationListURL = renderResponse.createRenderURL();
configurationListURL.setWindowState(WindowState.MAXIMIZED);
configurationListURL.setParameter("action", "listConfiguration");
%>

<c:if test="${isAdmin == 'true'}">
    <div id="<portlet:namespace/>accordionMenuPassAdmin" class="accordion-menu">
        <div class="osu-accordion-header" onclick="<portlet:namespace/>toggleContent('<portlet:namespace/>passAdmin');">
          <img id="<portlet:namespace/>passAdminImageToggle" src="/cps/images/accordion/accordion_arrow_up.png"/>
          PASS Administration
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
            </ul>
        </div>
    </div>
</c:if>