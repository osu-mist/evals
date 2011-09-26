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
            
                <input type="radio" name="<portlet:namespace />appraisal.rating" value="1" id="<portlet:namespace />appraisal.rating-1"
                <c:if test="${appraisal.rating == 1}">
                    checked="checked"
                </c:if>
                <c:if test="${permissionRule.evaluation == 'v'}">
                    disabled="disabled"
                </c:if> />
                <label for="<portlet:namespace />appraisal.rating-1"><liferay-ui:message key="appraisal-rating-1" /></label><br />
              

              <input type="radio" name="<portlet:namespace />appraisal.rating" value="2" id="<portlet:namespace />appraisal.rating-2"
                <c:if test="${appraisal.rating == 2}">
                    checked="checked"
                </c:if>
                <c:if test="${permissionRule.evaluation == 'v'}">
                    disabled="disabled"
                </c:if> />
                <label for="<portlet:namespace />appraisal.rating-2"><liferay-ui:message key="appraisal-rating-2" /></label><br />
              

             <input type="radio" name="<portlet:namespace />appraisal.rating" value="3" id="<portlet:namespace />appraisal.rating-3"
                <c:if test="${appraisal.rating == 3}">
                    checked="checked"
                </c:if>
                <c:if test="${permissionRule.evaluation == 'v'}">
                    disabled="disabled"
                </c:if> />
                <label for="<portlet:namespace />appraisal.rating-3"><liferay-ui:message key="appraisal-rating-3" /></label><br />
              

              
                <input type="radio" name="<portlet:namespace />appraisal.rating" value="4" id="<portlet:namespace />appraisal.rating-4"
                <c:if test="${appraisal.rating == 4}">
                    checked="checked"
                </c:if>
                <c:if test="${permissionRule.evaluation == 'v'}">
                    disabled="disabled"
                </c:if> />
                <label for="<portlet:namespace />appraisal.rating-4"><liferay-ui:message key="appraisal-rating-4" /></label>
              
        </fieldset>
    </fieldset>
</div><!-- end evaluation -->
