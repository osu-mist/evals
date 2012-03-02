<hr />
<h3><liferay-ui:message key="demo-settings"/></h3>
<p><liferay-ui:message key="demo-settings-description"/></p>

<a href="<portlet:actionURL windowState="<%= WindowState.MAXIMIZED.toString() %>">
    <portlet:param name="id" value="${appraisal.id}" />
    <portlet:param name="action" value="demoResetAppraisal" />
    <portlet:param name="controller" value="HomeAction" />
    <portlet:param name="status" value="goalsDue" />
    </portlet:actionURL>">
<liferay-ui:message key="demo-settings-appraisal-reset-goals-due"/>
</a><br />

<a href="<portlet:actionURL windowState="<%= WindowState.MAXIMIZED.toString() %>">
    <portlet:param name="id" value="${appraisal.id}" />
    <portlet:param name="action" value="demoResetAppraisal" />
    <portlet:param name="controller" value="HomeAction" />
    <portlet:param name="status" value="resultsDue" />
    </portlet:actionURL>">
<liferay-ui:message key="demo-settings-appraisal-reset-results-due"/>
</a><br />