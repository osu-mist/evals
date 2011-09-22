<fieldset class="pass-review">
     <h3 class="secret"><liferay-ui:message key="appraisal-hr-review"/></h3>
    <legend><liferay-ui:message key="appraisal-hr-review"/></legend>
    <c:choose>
        <c:when test="${permissionRule.review == 'e'}">
            <label for="<portlet:namespace />appraisal.review">
                <liferay-ui:message key="appraisal-hr-comments"/>
            </label>
            <liferay-ui:input-textarea param="appraisal.review"
                defaultValue="${appraisal.review}" />
        </c:when>
        <c:when test="${permissionRule.review == 'v'}">
            <p class="pass-form-text"><%= CWSUtil.escapeHtml(formAppraisal.getReview()) %></p>
        </c:when>
    </c:choose>
</fieldset><!-- end review -->