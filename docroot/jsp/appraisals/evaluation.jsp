<div class="pass-evaluation">
    <fieldset>
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
            <legend><liferay-ui:message key="appraisal-select-rating"/></legend>
            <ol>
              <li>
                <input type="radio" name="<portlet:namespace />appraisal.rating" value="1"
                <c:if test="${appraisal.rating == 1}">
                    checked="checked"
                </c:if>
                <c:if test="${permissionRule.evaluation == 'v'}">
                    disabled="disabled"
                </c:if> />
                <liferay-ui:message key="appraisal-rating-1" />
              </li>

              <li><input type="radio" name="<portlet:namespace />appraisal.rating" value="2"
                <c:if test="${appraisal.rating == 2}">
                    checked="checked"
                </c:if>
                <c:if test="${permissionRule.evaluation == 'v'}">
                    disabled="disabled"
                </c:if> />
                <liferay-ui:message key="appraisal-rating-2" />
              </li>

              <li><input type="radio" name="<portlet:namespace />appraisal.rating" value="3"
                <c:if test="${appraisal.rating == 3}">
                    checked="checked"
                </c:if>
                <c:if test="${permissionRule.evaluation == 'v'}">
                    disabled="disabled"
                </c:if> />
                <liferay-ui:message key="appraisal-rating-3" />
              </li>

              <li>
                <input type="radio" name="<portlet:namespace />appraisal.rating" value="4"
                <c:if test="${appraisal.rating == 4}">
                    checked="checked"
                </c:if>
                <c:if test="${permissionRule.evaluation == 'v'}">
                    disabled="disabled"
                </c:if> />
                <liferay-ui:message key="appraisal-rating-4" />
              </li>
            </ol>
        </fieldset>
    </fieldset>
</div><!-- end evaluation -->
