<div class="pass-evaluation">
    <h4><liferay-ui:message key="appraisal-summary"/></h4>
    <c:choose>
        <c:when test="${permissionRule.evaluation == 'e'}">
            <liferay-ui:input-textarea param="appraisal.evaluation"
                defaultValue="${appraisal.evaluation}" />
        </c:when>
        <c:when test="${permissionRule.evaluation == 'v'}">
<p class="pass-form-text">${fn:replace(appraisal.evaluation, "
", "<br />")}</p>
        </c:when>
    </c:choose>

    <strong><strong>Check one category</strong></strong><br />
    <fieldset>
        1. <input type="radio" name="<portlet:namespace />appraisal.rating" value="1"
        <c:if test="${appraisal.rating == 1}">
            checked="checked"
        </c:if>
        <c:if test="${permissionRule.evaluation == 'v'}">
            disabled="disabled"
        </c:if> />
        <liferay-ui:message key="appraisal-rating-1" /><br />

        2. <input type="radio" name="<portlet:namespace />appraisal.rating" value="2"
        <c:if test="${appraisal.rating == 2}">
            checked="checked"
        </c:if>
        <c:if test="${permissionRule.evaluation == 'v'}">
            disabled="disabled"
        </c:if> />
        <liferay-ui:message key="appraisal-rating-2" /><br />

        3. <input type="radio" name="<portlet:namespace />appraisal.rating" value="3"
        <c:if test="${appraisal.rating == 3}">
            checked="checked"
        </c:if>
        <c:if test="${permissionRule.evaluation == 'v'}">
            disabled="disabled"
        </c:if> />
        <liferay-ui:message key="appraisal-rating-3" /><br />

        4. <input type="radio" name="<portlet:namespace />appraisal.rating" value="4"
        <c:if test="${appraisal.rating == 4}">
            checked="checked"
        </c:if>
        <c:if test="${permissionRule.evaluation == 'v'}">
            disabled="disabled"
        </c:if> />
        <liferay-ui:message key="appraisal-rating-4" /><br />
    </fieldset>
</div><!-- end evaluation -->