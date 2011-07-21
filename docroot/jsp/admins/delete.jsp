<%@ include file="/jsp/init.jsp" %>
<jsp:useBean id="admin" class="edu.osu.cws.pass.models.Admin" scope="request" />

<h2><liferay-ui:message key="admin-delete" /></h2>

<form action="<portlet:actionURL windowState="<%= WindowState.MAXIMIZED.toString() %>">
    <portlet:param name="id" value="${admin.id}"/>
    <portlet:param name="action" value="deleteAdmin"/>
    </portlet:actionURL>" method="post">

    <p><liferay-ui:message key="admin-delete-confirm"/>: ${admin.employee.name} ?</p>

    <input type="submit" name="delete" value="<liferay-ui:message key="delete" />" />
    <input type="submit" name="cancel" value="<liferay-ui:message key="cancel" />" />
</form>
<%@ include file="/jsp/footer.jsp" %>