<%@ include file="/jsp/init.jsp"%>

<jsp:useBean id="appraisal" class="edu.osu.cws.pass.models.Appraisal" scope="request" />
<jsp:useBean id="permissionRule" class="edu.osu.cws.pass.models.PermissionRule" scope="request" />

<h2><liferay-ui:message key="appraisal-classified-title" /></h2>
<%@ include file="/jsp/appraisals/info.jsp"%>

<c:if test="${not empty permissionRule.saveDraft || not empty permissionRule.requireModification || not empty permissionRule.submit}">
<form class="appraisal" action="<portlet:actionURL windowState="<%= WindowState.NORMAL.toString() %>">
    <portlet:param name="action" value="updateAppraisal" />
    </portlet:actionURL>" method="post" name="<portlet:namespace />request_form">
    <input type="hidden" name="id" value="${appraisal.id}"/>
</c:if>

<div id="appraisal-criteria">
<c:forEach var="assessment" items="${appraisal.assessments}" varStatus="loopStatus">
    <%@ include file="/jsp/appraisals/criteria.jsp"%>
</c:forEach>
</div>

<c:choose>
    <c:when test="${permissionRule.goalComments == 'e'}">
        <p><strong><liferay-ui:message key="appraisal-goals-comments" /></strong></p>
        <liferay-ui:input-textarea param="appraisal.goalsComments"
            defaultValue="${appraisal.goalsComments}" /><br />
    </c:when>
    <c:when test="${permissionRule.goalComments == 'v'}">
        <p><strong><liferay-ui:message key="appraisal-goals-comments" /></strong></p>
        <c:out value="${appraisal.goalsComments}" />
    </c:when>
</c:choose>

<c:if test="${not empty permissionRule.evaluation}">
    <%@ include file="/jsp/appraisals/evaluation.jsp"%>
</c:if>

<c:if test="${not empty permissionRule.review}">
    <%@ include file="/jsp/appraisals/review.jsp"%>
</c:if>

<c:choose>
    <c:when test="${permissionRule.employeeResponse == 'e'}">
        <p><strong><liferay-ui:message key="appraisal-employee-response" /></strong></p>
        <liferay-ui:input-textarea param="appraisal.employeeResponse"
            defaultValue="${appraisal.employeeResponse}" /><br />
    </c:when>
    <c:when test="${permissionRule.employeeResponse == 'v'}">
        <p><strong><liferay-ui:message key="appraisal-employee-response" /></strong></p>
        <c:out value="${appraisal.employeeResponse}" />
    </c:when>
</c:choose>

<br />
<c:if test="${not empty permissionRule.saveDraft}">
<input name="${permissionRule.saveDraft}" type="submit" value="<liferay-ui:message key="${permissionRule.saveDraft}" />">
</c:if>

<c:if test="${not empty permissionRule.requireModification}">
<input name="${permissionRule.requireModification}" type="submit" value="<liferay-ui:message key="${permissionRule.requireModification}" />">
</c:if>

<c:if test="${not empty permissionRule.submit}">
<input name="${permissionRule.submit}" type="submit" value="<liferay-ui:message key="${permissionRule.submit}" />">
</c:if>

<c:if test="${not empty permissionRule.saveDraft || not empty permissionRule.requireModification || not empty permissionRule.submit}">
</form>
</c:if>