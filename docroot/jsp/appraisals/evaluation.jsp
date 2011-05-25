<div class="evaluation">
    <h4><liferay-ui:message key="appraisal-summary"/></h4>
    <c:choose>
        <c:when test="${permissionRule.evaluation == 'e'}">
            <liferay-ui:input-textarea param="appraisal.evaluation"
                defaultValue="${appraisal.evaluation}" />
        </c:when>
        <c:when test="${permissionRule.evaluation == 'v'}">
            <c:out value="${appraisal.evaluation}" />
        </c:when>
    </c:choose>

    <fieldset>
        <caption><strong>Check one category</strong></caption>
        <ol>
            <li><input type="radio" name="<portlet:namespace />rating" value="1"
            <c:if test="${appraisal.rating == 1}">
                "checked"
            </c:if>
            <c:if test="${permissionRule.evaluation == 'v'}">
                "disabled"
            </c:if> />
            <liferay-ui:message key="appraisal-rating-1" /></li>

            <li><input type="radio" name="<portlet:namespace />rating" value="2"
            <c:if test="${appraisal.rating == 2}">
                "checked"
            </c:if>
            <c:if test="${permissionRule.evaluation == 'v'}">
                "disabled"
            </c:if> />
            <liferay-ui:message key="appraisal-rating-2" /></li>

            <li><input type="radio" name="<portlet:namespace />rating" value="3"
            <c:if test="${appraisal.rating == 3}">
                "checked"
            </c:if>
            <c:if test="${permissionRule.evaluation == 'v'}">
                "disabled"
            </c:if> />
            <liferay-ui:message key="appraisal-rating-3" /></li>

            <li><input type="radio" name="<portlet:namespace />rating" value="4"
            <c:if test="${appraisal.rating == 4}">
                "checked"
            </c:if>
            <c:if test="${permissionRule.evaluation == 'v'}">
                "disabled"
            </c:if> />
            <liferay-ui:message key="appraisal-rating-4" /></li>
        </ol>
    </fieldset>
</div><!-- end evaluation -->