<hr />
<h2><liferay-ui:message key="demo-settings"/></h2>
<p><liferay-ui:message key="demo-settings-description"/></p>

<form action="<portlet:actionURL windowState="<%= WindowState.NORMAL.toString() %>">
    <portlet:param name="action" value="demoSwitchUser" />
    <portlet:param name="controller" value="HomeAction" />
    </portlet:actionURL>"
    method="post"/>

    <div>
        <strong><liferay-ui:message key="logged-in-as"/>: </strong> ${employee.name}
    </div>

    <div>
        <strong><liferay-ui:message key="switch-user"/>: </strong>
    </div>

    <input name="<portlet:namespace />employee.onid" type="text" value="" />
    <input type="submit" value="<liferay-ui:message key="submit"/>" />
</form>