<input type="checkbox" name="<portlet:namespace />appraisal.assessmentCriteria.${assessmentCriteria.id}"
    id="<portlet:namespace />appraisal.assessmentCriteria.${assessmentCriteria.id}"
    <c:if test="${assessmentCriteria.checked}">
        checked="checked"
    </c:if>
    <c:if test="${permissionRule.approvedGoals == 'v'}">
        disabled="disabled"
    </c:if>
/>
<label for="<portlet:namespace />appraisal.assessmentCriteria.${assessmentCriteria.id}">
    <span title="${assessmentCriteria.criteriaArea.description}">
        <c:out value="${assessmentCriteria.criteriaArea.name}"/></span><sup>[?]</sup></label>