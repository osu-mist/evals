<fieldset class="pass-review">
    <h3 class="secret"><liferay-ui:message key="appraisal-hr-review"/></h3>
    <legend id="<portlet:namespace />review-legend">
        <img id="<portlet:namespace />review-arrow" src="/cps/images/accordion/accordion_arrow_down.png"/>
        <liferay-ui:message key="appraisal-hr-review"/>
    </legend>
    <div id="<portlet:namespace />review-wrapper">
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
    </div>
</fieldset><!-- end review -->

<script type="text/javascript">
    jQuery(document).ready(function(){
        jQuery("#<portlet:namespace />review-legend").click(function() {
            jQuery("#<portlet:namespace />review-wrapper").slideToggle(function() {
                var img = jQuery("#<portlet:namespace />review-arrow");
                if(jQuery(img).attr("src") == "/cps/images/accordion/accordion_arrow_down.png") {
                    jQuery(img).attr("src", "/cps/images/accordion/accordion_arrow_up.png");
                }
                else {
                    jQuery(img).attr("src", "/cps/images/accordion/accordion_arrow_down.png");
                }
            });
        });
    });
</script>