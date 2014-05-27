<div class="pass-evaluation">
    <fieldset>
        <h3 class="secret"><liferay-ui:message key="appraisal-summary"/></h3>
        <legend><liferay-ui:message key="appraisal-summary"/></legend>
            <c:choose>
                <c:when test="${permissionRule.evaluation == 'e'}">
                    <label for="<portlet:namespace />appraisal.evaluation">
                        <liferay-ui:message key="appraisal-evaluation" />
                    </label>
                    <liferay-ui:input-textarea param="appraisal.evaluation"
                        defaultValue="${appraisal.evaluation}" /><br />
                </c:when>
                <c:when test="${permissionRule.evaluation == 'v'}">
                    <fieldset>
                        <legend>Evaluation</legend>
                        <p class="pass-form-text"><%= CWSUtil.escapeHtml(formAppraisal.getEvaluation()) %></p>
                    </fieldset>
                </c:when>
            </c:choose>

        <fieldset>
            <h3 class="secret"><liferay-ui:message key="appraisal-select-rating"/></h3>
            <legend><liferay-ui:message key="appraisal-select-rating"/></legend>
            <!-- ><label for="<portlet:namespace />appraisal.rating">
                <liferay-ui:message key="appraisal-rating-label" />
            </label> -->

            <c:forEach var="rating" items="${ratings}">
                <input type="radio" name="<portlet:namespace />appraisal.rating" value="${rating.rate}"
                       id="<portlet:namespace />appraisal.rating-${rating.rate}"
                <c:if test="${appraisal.rating == rating.rate}">
                    checked="checked"
                </c:if>
                <c:if test="${permissionRule.evaluation == 'v'}">
                    disabled="disabled"
                </c:if> />
                <label for="<portlet:namespace />appraisal.rating-${rating.rate}">${rating.descripting}</label><br />
            </c:forEach>
        </fieldset>

        <c:if test="${appraisal.isSalaryUsed}">
            <%@ include file="/jsp/appraisals/salary.jsp"%>
        </c:if>

    </fieldset>
</div><!-- end evaluation -->
