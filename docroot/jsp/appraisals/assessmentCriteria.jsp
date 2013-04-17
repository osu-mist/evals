<input type="checkbox" name="<portlet:namespace />"
    id="<portlet:namespace />appraisal.assessmentCriteria.${assessmentCriteria.id}"
    <c:if test="${assessmentCriteria.checked}">
        checked="checked"
    </c:if>
    <c:if test="${permissionRule.goals == 'v'}">
        disabled="disabled"
    </c:if>
/>
<label for="<portlet:namespace />appraisal.assessmentCriteria.${assessmentCriteria.id}">
    <c:out value="${assessmentCriteria.criteriaArea.name}"/></label>
<liferay-ui:icon-help message="${assessmentCriteria.criteriaArea.description}"/>