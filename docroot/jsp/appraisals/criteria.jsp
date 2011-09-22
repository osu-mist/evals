<%
Assessment formAssessment = (Assessment) pageContext.getAttribute("assessment");
%>
<fieldset>
<h3 class="secret"><c:out value="${assessment.criterionDetail.areaID.name}"/></h3>
<legend><c:out value="${assessment.criterionDetail.areaID.name}"/></legend>
<p class="instructions"><c:out value="${assessment.criterionDetail.description}" /></p>

<c:choose>
    <c:when test="${permissionRule.goals == 'e'}">
        <label for="<portlet:namespace />appraisal.goal.${assessment.id}"><liferay-ui:message key="appraisal-goals" /></label>
        <liferay-ui:input-textarea param="appraisal.goal.${assessment.id}"
            defaultValue="${assessment.goal}" />
    </c:when>
    <c:when test="${permissionRule.goals == 'v'}">
        <fieldset>
            <legend><liferay-ui:message key="appraisal-goals" /></legend>
            <p class="pass-form-text"><%= CWSUtil.escapeHtml(formAssessment.getGoal()) %></p>
        </fieldset>
    </c:when>
</c:choose>

<c:choose>
    <c:when test="${permissionRule.newGoals == 'e'}">
        <label for="<portlet:namespace />appraisal.newGoals.${assessment.id}"><liferay-ui:message key="appraisal-newGoals" /></label>
        <liferay-ui:input-textarea param="appraisal.newGoals.${assessment.id}"
            defaultValue="${assessment.newGoals}" />
    </c:when>
    <c:when test="${permissionRule.newGoals == 'v'}">
        <fieldset>
            <legend><liferay-ui:message key="appraisal-newGoals" /></legend>
            <p class="pass-form-text"><%= CWSUtil.escapeHtml(formAssessment.getNewGoals()) %></p>
        </fieldset>
    </c:when>
</c:choose>

<c:choose>
    <c:when test="${permissionRule.results == 'e'}">
        <label for="<portlet:namespace />assessment.employeeResult.${assessment.id}"><liferay-ui:message key="appraisal-employee-results" /></label>
        <liferay-ui:input-textarea param="assessment.employeeResult.${assessment.id}"
            defaultValue="${assessment.employeeResult}" />
    </c:when>
    <c:when test="${permissionRule.results == 'v'}">
        <fieldset>
            <legend><liferay-ui:message key="appraisal-employee-results" /></legend>
            <p class="pass-form-text"><%= CWSUtil.escapeHtml(formAssessment.getEmployeeResult()) %></p>
        </fieldset>
    </c:when>
</c:choose>

<c:choose>
    <c:when test="${permissionRule.supervisorResults == 'e'}">
        <label for="<portlet:namespace />assessment.supervisorResult.${assessment.id}"><liferay-ui:message key="appraisal-result-comments" /></label>
        <liferay-ui:input-textarea param="assessment.supervisorResult.${assessment.id}"
            defaultValue="${assessment.supervisorResult}" />
    </c:when>
    <c:when test="${permissionRule.supervisorResults == 'v'}">
        <fieldset>
            <legend><liferay-ui:message key="appraisal-result-comments" /></legend>
            <p class="pass-form-text"><%= CWSUtil.escapeHtml(formAssessment.getSupervisorResult()) %></p>
        </fieldset>
    </c:when>
</c:choose>
</fieldset>
