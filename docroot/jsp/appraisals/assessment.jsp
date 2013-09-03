<%
    // This file renders a single assessment in the form with: goals, employee/supervisor results and
// assessment criteria checkboxes. It loops through the assessment.sortedAssessmentCriteria to
// include appraisals/assessmentCriteria which is the jsp that generates the checkboxes

    Assessment formAssessment = (Assessment) pageContext.getAttribute("assessment");
%>

<c:choose>
    <c:when test="${assessment.newGoal}">
        <c:set var="goalStatus" value="${permissionRule.unapprovedGoals}"/>
    </c:when>
    <c:otherwise>
        <c:set var="goalStatus" value="${permissionRule.approvedGoals}"/>
    </c:otherwise>
</c:choose>

<div class="appraisal-assessment-${assessment.id}">
    <h3 class="secret"><liferay-ui:message key="appraisal-assessment-header"/>${loopStatus.index + 1}</h3>
    <c:if test="${goalStatus== 'e'}">
        <a class="delete img-txt assessment-delete delete.id.${assessment.id}"
           title="<liferay-ui:message key="appraisal-assessment-delete"/>"
           href="#"><liferay-ui:message key="appraisal-assessment-delete"/></a>
        <div class="osu-cws-clear-both"></div>
    </c:if>

    <input type="hidden" class="appraisal-assessment-deleted-${assessment.id}" name="<portlet:namespace />appraisal.assessment.deleted.${assessment.id}"
           value="0"/>
    <c:choose>
        <c:when test="${goalStatus == 'e'}">
            <label for="<portlet:namespace />appraisal.goal.${assessment.id}"><liferay-ui:message key="appraisal-goals" />
                    ${loopStatus.index + 1}</label>
            <div><liferay-ui:input-textarea param="appraisal.goal.${assessment.id}"
                                            defaultValue="${assessment.goal}" /></div>
        </c:when>
        <c:when test="${goalStatus == 'v'}">
            <fieldset>
                <legend><span><liferay-ui:message key="appraisal-goals" />${loopStatus.index + 1}</span></legend>
                <p class="pass-form-text"><%= CWSUtil.escapeHtml(formAssessment.getGoal()) %></p>
                <c:if test="${goalStatus == 'v'}">
                    <c:forEach var="assessmentCriteria" items="${assessment.sortedAssessmentCriteria}" varStatus="loopStatus2">
                        <%@ include file="/jsp/appraisals/assessmentCriteria.jsp"%>
                    </c:forEach>
                </c:if>
            </fieldset>
        </c:when>
    </c:choose>

    <c:if test="${goalStatus == 'e'}">
        <div class="assessment-criteria">
            <c:forEach var="assessmentCriteria" items="${assessment.sortedAssessmentCriteria}" varStatus="loopStatus2">
                <%@ include file="/jsp/appraisals/assessmentCriteria.jsp"%>
            </c:forEach>
        </div>
    </c:if>

    <c:if test="${not assessment.newGoal}">
        <c:choose>
            <c:when test="${permissionRule.results == 'e'}">
                <label for="<portlet:namespace />assessment.employeeResult.${assessment.id}"><liferay-ui:message key="appraisal-employee-results" /></label>
                <div><liferay-ui:input-textarea param="assessment.employeeResult.${assessment.id}"
                                                defaultValue="${assessment.employeeResult}" /></div>
            </c:when>
            <c:when test="${permissionRule.results == 'v'}">
                <fieldset>
                    <legend><span><liferay-ui:message key="appraisal-employee-results" /></span></legend>
                    <p class="pass-form-text"><%= CWSUtil.escapeHtml(formAssessment.getEmployeeResult()) %></p>
                </fieldset>
            </c:when>
        </c:choose>

        <c:choose>
            <c:when test="${permissionRule.supervisorResults == 'e'}">
                <label for="<portlet:namespace />assessment.supervisorResult.${assessment.id}"><liferay-ui:message key="appraisal-result-comments" /></label>
                <div><liferay-ui:input-textarea param="assessment.supervisorResult.${assessment.id}"
                                                defaultValue="${assessment.supervisorResult}" /></div>
            </c:when>
            <c:when test="${permissionRule.supervisorResults == 'v'}">
                <fieldset>
                    <legend><span><liferay-ui:message key="appraisal-result-comments" /></span></legend>
                    <p class="pass-form-text"><%= CWSUtil.escapeHtml(formAssessment.getSupervisorResult()) %></p>
                </fieldset>
            </c:when>
        </c:choose>
    </c:if>

</div>