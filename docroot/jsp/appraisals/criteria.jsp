<%
Assessment formAssessment = (Assessment) pageContext.getAttribute("assessment");
%>
<fieldset>
<legend><c:out value="${assessment.criterionDetail.areaID.name}"/></legend>
<p class="instructions"><c:out value="${assessment.criterionDetail.description}" /></p>

<c:choose>
    <c:when test="${permissionRule.goals == 'e'}">
        <label for="<portlet:namespace />appraisal.goal.${assessment.id}"><liferay-ui:message key="appraisal-goals" /></label>
        <liferay-ui:input-textarea param="appraisal.goal.${assessment.id}"
            defaultValue="${assessment.goal}" />
    </c:when>
    <c:when test="${permissionRule.goals == 'v'}">
        <p class="instructions"><liferay-ui:message key="appraisal-goals" /></p>
<p class="pass-form-text"><%= formAssessment.getGoal().replaceAll("\n", "<br />") %></p>
    </c:when>
</c:choose>

<c:choose>
    <c:when test="${permissionRule.newGoals == 'e'}">
        <label for="<portlet:namespace />appraisal.newGoals.${assessment.id}"><liferay-ui:message key="appraisal-newGoals" /></label>
        <liferay-ui:input-textarea param="appraisal.newGoals.${assessment.id}"
            defaultValue="${assessment.newGoals}" />
    </c:when>
    <c:when test="${permissionRule.newGoals == 'v'}">
        <p class="instructions"><liferay-ui:message key="appraisal-newGoals" /></p>
<p class="pass-form-text"><%= formAssessment.getNewGoals().replaceAll("\n", "<br />") %></p>
    </c:when>
</c:choose>

<c:choose>
    <c:when test="${permissionRule.results == 'e'}">
        <label for="<portlet:namespace />assessment.employeeResult.${assessment.id}"><liferay-ui:message key="appraisal-employee-results" /></label>
        <liferay-ui:input-textarea param="assessment.employeeResult.${assessment.id}"
            defaultValue="${assessment.employeeResult}" />
    </c:when>
    <c:when test="${permissionRule.results == 'v'}">
        <p class="instructions"><liferay-ui:message key="appraisal-employee-results" /></p>
<p class="pass-form-text"><%= formAssessment.getEmployeeResult().replaceAll("\n", "<br />") %></p>
    </c:when>
</c:choose>

<c:choose>
    <c:when test="${permissionRule.supervisorResults == 'e'}">
        <label for="<portlet:namespace />assessment.supervisorResult.${assessment.id}"><liferay-ui:message key="appraisal-result-comments" /></label>
        <liferay-ui:input-textarea param="assessment.supervisorResult.${assessment.id}"
            defaultValue="${assessment.supervisorResult}" />
    </c:when>
    <c:when test="${permissionRule.supervisorResults == 'v'}">
        <p class="instructions"><liferay-ui:message key="appraisal-result-comments" /></p>
<p class="pass-form-text"><%= formAssessment.getSupervisorResult().replaceAll("\n", "<br />") %></p>
    </c:when>
</c:choose>
</fieldset>