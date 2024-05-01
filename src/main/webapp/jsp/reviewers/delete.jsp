<%@ include file="/jsp/init.jsp" %>
<jsp:useBean id="reviewer" class="edu.osu.cws.evals.models.Reviewer" scope="request" />

<h2><liferay-ui:message key="reviewer-delete" /></h2>

<form action="<portlet:actionURL windowState="<%= WindowState.MAXIMIZED.toString() %>">
    <portlet:param name="id" value="${reviewer.id}"/>
    <portlet:param name="action" value="delete"/>
    <portlet:param name="controller" value="ReviewersAction"/>
    </portlet:actionURL>" method="post">

    <p><liferay-ui:message key="reviewer-delete-confirm"/>: ${reviewer.employee.name}?</p>

    <input type="submit" name="delete" value="<liferay-ui:message key="delete" />" />
    <input type="submit"class="cancel" name="cancel" value="<liferay-ui:message key="cancel" />" />
</form>
<%@ include file="/jsp/footer.jsp" %>