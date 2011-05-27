<h3><c:out value="${assessment.criterionDetail.areaID.name}"/></h3>
<p><strong><c:out value="${assessment.criterionDetail.description}" /></strong></p>

<c:choose>
    <c:when test="${permissionRule.goals == 'e'}">
        <p><strong><liferay-ui:message key="appraisal-goals" /></strong></p>
        <liferay-ui:input-textarea param="appraisal.goal.${assessment.id}"
            defaultValue="${assessment.goal}" />
    </c:when>
    <c:when test="${permissionRule.goals == 'v'}">
        <p><strong><liferay-ui:message key="appraisal-goals" /></strong></p>
        <c:out value="${assessment.goal}" />
    </c:when>
</c:choose>

<c:choose>
    <c:when test="${permissionRule.newGoals == 'e'}">
        <p><strong><liferay-ui:message key="appraisal-newGoals" /></strong></p>
        <liferay-ui:input-textarea param="appraisal.newGoal.${assessment.id}"
            defaultValue="${assessment.newGoal}" />
    </c:when>
    <c:when test="${permissionRule.newGoals == 'v'}">
        <p><strong><liferay-ui:message key="appraisal-newGoals" /></strong></p>
        <c:out value="${assessment.newGoal}" />
    </c:when>
</c:choose>

<c:choose>
    <c:when test="${permissionRule.results == 'e'}">
        <p><strong><liferay-ui:message key="appraisal-employee-result" /></strong></p>
        <liferay-ui:input-textarea param="assessment.employeeResult.${assessment.id}"
            defaultValue="${assessment.employeeResult}" />
    </c:when>
    <c:when test="${permissionRule.results == 'v'}">
        <p><strong><liferay-ui:message key="appraisal-employee-results" /></strong></p>
        <c:out value="${assessment.employeeResult}" />
    </c:when>
</c:choose>

<c:choose>
    <c:when test="${permissionRule.supervisorResults == 'e'}">
        <p><strong><liferay-ui:message key="appraisal-result-comments" /></strong></p>
        <liferay-ui:input-textarea param="assessment.supervisorResult.${assessment.id}"
            defaultValue="${assessment.supervisorResult}" />
    </c:when>
    <c:when test="${permissionRule.supervisorResults == 'v'}">
        <p><strong><liferay-ui:message key="appraisal-result-comments" /></strong></p>
        <c:out value="${assessment.supervisorResult}" />
    </c:when>
</c:choose>