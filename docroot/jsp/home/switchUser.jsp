<hr />
<h2><liferay-ui:message key="demo-settings"/></h2>
<p><liferay-ui:message key="demo-settings-description"/></p>

<form action="<portlet:actionURL windowState="<%= WindowState.NORMAL.toString() %>">
    <portlet:param name="action" value="demoSwitchUser" />
    <portlet:param name="controller" value="HomeAction" />
    </portlet:actionURL>"
    method="post"/>

    <strong><liferay-ui:message key="logged-in-as"/>: </strong> ${employee.name}

    <strong><liferay-ui:message key="switch-user"/>: </strong>
    <select name="<portlet:namespace />employee.id">
    <c:forEach var="demoEmployee" items="${employees}">
    <option value="${demoEmployee.id}">${demoEmployee.name}</option>
    </c:forEach>
    </select>
    <input type="submit" value="<liferay-ui:message key="submit"/>" />
</form>