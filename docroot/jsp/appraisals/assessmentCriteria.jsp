<input type="checkbox" name="<portlet:namespace />appraisal.assessmentCriteria.${assessmentCriteria.id}"
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

<%--@todo: need to provide to js a map of ids with criteria name + description to create things on the fly--%>