<%@ include file="/jsp/init.jsp" %>
<jsp:useBean id="criterion" class="edu.osu.cws.evals.models.CriterionArea" scope="request" />

<h2><liferay-ui:message key="criteria-delete" /></h2>

<form action="<portlet:actionURL windowState="<%= WindowState.MAXIMIZED.toString() %>">
    <portlet:param name="id" value="${criterion.id}"/>
    <portlet:param name="action" value="deleteCriteria"/>
    </portlet:actionURL>" method="post">

    <p><liferay-ui:message key="criteria-delete-confirm"/>: ${criterion.name} ?</p>

    <input type="submit" name="delete" value="<liferay-ui:message key="delete" />" />
    <input type="submit" name="cancel" value="<liferay-ui:message key="cancel" />" />
</form>
<%@ include file="/jsp/footer.jsp" %>