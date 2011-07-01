<%@ include file="/jsp/init.jsp" %>
<jsp:useBean id="reviewer" class="edu.osu.cws.pass.models.Reviewer" scope="request" />

<h2><liferay-ui:message key="reviewer-delete" /></h2>

<form action="<portlet:actionURL>
    <portlet:param name="id" value="${reviewer.id}"/>
    <portlet:param name="action" value="deleteReviewer"/>
    </portlet:actionURL>" method="post">

    <p><liferay-ui:message key="reviewer-delete-confirm"/>: ${reviewer.employee.name} ?</p>

    <input type="submit" name="delete" value="<liferay-ui:message key="delete" />" />
    <input type="submit" name="cancel" value="<liferay-ui:message key="cancel" />" />
</form>
