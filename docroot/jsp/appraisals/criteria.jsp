<h3><c:out value="${assessment.criterionDetail.areaID.name}"/></h3>
<p class="instructions"><c:out value="${assessment.criterionDetail.description}" /></p>

<c:choose>
    <c:when test="${permissionRule.goals == 'e'}">
        <p class="instructions"><liferay-ui:message key="appraisal-goals" /></p>
        <liferay-ui:input-textarea param="appraisal.goal.${assessment.id}"
            defaultValue="${assessment.goal}" />
    </c:when>
    <c:when test="${permissionRule.goals == 'v'}">
        <p class="instructions"><liferay-ui:message key="appraisal-goals" /></p>
<p class="pass-form-text">${assessment.goal}</p>
    </c:when>
</c:choose>

<c:choose>
    <c:when test="${permissionRule.newGoals == 'e'}">
        <p class="instructions"><liferay-ui:message key="appraisal-newGoals" /></p>
        <liferay-ui:input-textarea param="appraisal.newGoal.${assessment.id}"
            defaultValue="${assessment.newGoal}" />
    </c:when>
    <c:when test="${permissionRule.newGoals == 'v'}">
        <p class="instructions"><liferay-ui:message key="appraisal-newGoals" /></p>
<p class="pass-form-text">${assessment.newGoal}</p>
    </c:when>
</c:choose>

<c:choose>
    <c:when test="${permissionRule.results == 'e'}">
        <p class="instructions"><liferay-ui:message key="appraisal-employee-results" /></p>
        <liferay-ui:input-textarea param="assessment.employeeResult.${assessment.id}"
            defaultValue="${assessment.employeeResult}" />
    </c:when>
    <c:when test="${permissionRule.results == 'v'}">
        <p class="instructions"><liferay-ui:message key="appraisal-employee-results" /></p>
<p class="pass-form-text">${assessment.employeeResult}</p>
    </c:when>
</c:choose>

<c:choose>
    <c:when test="${permissionRule.supervisorResults == 'e'}">
        <p class="instructions"><liferay-ui:message key="appraisal-result-comments" /></p>
        <liferay-ui:input-textarea param="assessment.supervisorResult.${assessment.id}"
            defaultValue="${assessment.supervisorResult}" />
    </c:when>
    <c:when test="${permissionRule.supervisorResults == 'v'}">
        <p class="instructions"><liferay-ui:message key="appraisal-result-comments" /></p>
<p class="pass-form-text">${assessment.supervisorResult}</p>
    </c:when>
</c:choose>