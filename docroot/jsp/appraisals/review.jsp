<div class="review">
    <h4><liferay-ui:message key="appraisal-review"/></h4>
    <c:choose>
        <c:when test="${permissionRule.review == 'e'}">
            <liferay-ui:input-textarea param="appraisal.review"
                defaultValue="${appraisal.review}" />
        </c:when>
        <c:when test="${permissionRule.review == 'v'}">
${fn:replace(appraisal.review, "
", "<br />")}
        </c:when>
    </c:choose>
</div><!-- end review -->