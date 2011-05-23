<div class="review">
    <h4><liferay-ui:message key="appraisal-review"/></h4>
    <c:choose>
        <c:when test="${permissionRule.review == 'e'}">
            <liferay-ui:input-textarea param="appraisal.hrComments"
                defaultValue="${appraisal.hrComments}" />
        </c:when>
        <c:when test="${permissionRule.review == 'v'}">
            <c:out value="${appraisal.hrComments}" />
        </c:when>
    </c:choose>
</div><!-- end review -->