<%@ include file="/jsp/init.jsp" %>

<h2><liferay-ui:message key="review-cycle-option-delete" /></h2>

<form action="<portlet:actionURL windowState="<%= WindowState.MAXIMIZED.toString() %>">
    <portlet:param name="id" value="${reviewCycleOption.id}"/>
    <portlet:param name="action" value="delete"/>
    <portlet:param name="controller" value="ReviewCycleAction"/>
    </portlet:actionURL>" method="post">

    <p><liferay-ui:message key="review-cycle-option-delete-confirm"/>: "<c:out value="${reviewCycleOption.name}"/>"?</p>

    <input type="submit" name="delete" value="<liferay-ui:message key="delete" />" />
    <input type="submit" name="cancel" value="<liferay-ui:message key="cancel" />" />
</form>
<%@ include file="/jsp/footer.jsp" %>