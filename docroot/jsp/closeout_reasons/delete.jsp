<%@ include file="/jsp/init.jsp" %>

<h2><liferay-ui:message key="closeout-reason-delete" /></h2>

<form action="<portlet:actionURL windowState="<%= WindowState.MAXIMIZED.toString() %>">
    <portlet:param name="id" value="${reason.id}"/>
    <portlet:param name="action" value="delete"/>
    <portlet:param name="controller" value="CloseOutAction"/>
    </portlet:actionURL>" method="post">

    <p><liferay-ui:message key="closeout-reason-delete-confirm"/> "<c:out value="${reason.reason}"/>" ?</p>

    <input type="submit" name="delete" value="<liferay-ui:message key="delete" />" />
    <input type="submit" name="cancel" value="<liferay-ui:message key="cancel" />" />
</form>
<%@ include file="/jsp/footer.jsp" %>