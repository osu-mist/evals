<div class="pass-review">
    <h4><liferay-ui:message key="appraisal-review"/></h4>
    <c:choose>
        <c:when test="${permissionRule.review == 'e'}">
            <liferay-ui:input-textarea param="appraisal.review"
                defaultValue="${appraisal.review}" />
        </c:when>
        <c:when test="${permissionRule.review == 'v'}">
<p class="pass-form-text"><%= formAppraisal.getReview().replaceAll("\n", "<br />") %></p>
        </c:when>
    </c:choose>
</div><!-- end review -->