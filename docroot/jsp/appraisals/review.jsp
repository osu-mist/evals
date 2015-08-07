<c:set var="reviewType" value="${appraisal.job.isUnclassified ? 'employee' : 'hr'}"/>

<h3 class="secret"><liferay-ui:message key="appraisal-hr-review"/></h3>
<div class="appraisal accordion-menu">
    <div class="osu-accordion-header" onclick="<portlet:namespace/>toggleContent('<portlet:namespace/>AppraisalHrReview');">
        <img id="<portlet:namespace/>AppraisalHrReviewImageToggle" src="/cps/images/accordion/accordion_arrow_up.png"/>
        <liferay-ui:message key="appraisal-${reviewType}-review" />
    </div>
    <div class="accordion-content" id="<portlet:namespace/>AppraisalHrReview" style="display: block">
        <div class="appraisal-info collapses">
            <c:choose>
                <c:when test="${permissionRule.review == 'e'}">
                    <label for="<portlet:namespace />appraisal.review">
                        <liferay-ui:message key="appraisal-${reviewType}-comments"/>
                    </label>
                    <liferay-ui:input-textarea param="appraisal.review"
                                               defaultValue="${appraisal.review}" />
                </c:when>
                <c:when test="${permissionRule.review == 'v'}">
                    <p class="pass-form-text"><%= CWSUtil.escapeHtml(formAppraisal.getReview()) %></p>
                </c:when>
            </c:choose>
        </div>
    </div>
</div>