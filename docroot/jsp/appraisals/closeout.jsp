<%@ include file="/jsp/init.jsp" %>
<jsp:useBean id="admin" class="edu.osu.cws.evals.models.Admin" scope="request" />

<h2><liferay-ui:message key="appraisal-closeout" /></h2>

<form action="<portlet:actionURL windowState="<%= WindowState.NORMAL.toString() %>">
    <portlet:param name="id" value="${appraisal.id}"/>
    <portlet:param name="action" value="update"/>
    <portlet:param name="controller" value="AppraisalsAction"/>
    </portlet:actionURL>" id="<portlet:namespace />fm" method="post">

    <input type="hidden" name="id" value="${appraisal.id}"/>


    <fieldset class="pass-closeout">
        <h3 class="secret"><liferay-ui:message key="appraisal-closeout"/></h3>
        <legend><liferay-ui:message key="appraisal-closeout"/></legend>

        <p><strong><liferay-ui:message key="appraisal-closeout-confirm"/>
            <c:out value="${appraisal.job.employee.name}" /> for the
            review period <c:out value="${appraisal.reviewPeriod}"/> ?</strong></p>

        <fieldset class="pass-closeout-reasons">
            <legend>
                <liferay-ui:message key="appraisal-closeout-select-reason"/>
            </legend>

            <c:forEach var="closeOutReason" items="${reasonsList}" varStatus="loopStatus">
                <input type="radio" name="<portlet:namespace />appraisal.closeOutReasonId"
                    id="<portlet:namespace />appraisal.closeOutReason-${closeOutReason.id}"
                    value="${closeOutReason.id}"/>
                <label for="<portlet:namespace />appraisal.closeOutReason-${closeOutReason.id}">
                    <c:out value="${closeOutReason.reason}"/></label><br />
            </c:forEach>
        </fieldset>


    </fieldset>

    <input type="submit" name="close-appraisal" value="<liferay-ui:message key="close" />"
           class="<portlet:namespace />show-confirm" />
    <input type="submit" name="cancel" value="<liferay-ui:message key="cancel" />" />
</form>

<script type="text/javascript">
jQuery("#<portlet:namespace />fm").submit(function() {
    var portlet_namespace = '<portlet:namespace />';
    var json_data = { id: ${appraisal.id}};
    json_data.closeOutReasonId = jQuery("input[name=\"" + portlet_namespace + "appraisal.closeOutReasonId\"]:checked").val();
    // button clicked
    json_data.buttonClicked = jQuery("input[type=submit][clicked=true]").attr('name');

    var json_text = JSON.stringify(json_data);
    var extra_data = jQuery('<input type="hidden" name="json_data"/>').val(json_text);
    jQuery("#<portlet:namespace />fm").append(extra_data);
});
</script>

<%@ include file="/jsp/footer.jsp" %>