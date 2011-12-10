<%@ include file="/jsp/init.jsp" %>
<jsp:useBean id="admin" class="edu.osu.cws.evals.models.Admin" scope="request" />

<h2><liferay-ui:message key="appraisal-closeout" /></h2>

<form action="<portlet:actionURL windowState="<%= WindowState.MAXIMIZED.toString() %>">
    <portlet:param name="id" value="${admin.id}"/>
    <portlet:param name="action" value="updateAppraisal"/>
    </portlet:actionURL>" method="post">

    <p><liferay-ui:message key="appraisal-closeout-confirm"/>
        <c:out value="${appraisal.job.supervisor.employee.name}" /> for the
        review period <c:out value="${appraisal.reviewPeriod}"/> ?</p>

    <fieldset class="pass-review">
        <h3 class="secret"><liferay-ui:message key="appraisal-hr-review"/></h3>
        <legend><liferay-ui:message key="appraisal-hr-review"/></legend>

        <label for="<portlet:namespace />appraisal.closeout">
            <liferay-ui:message key="appraisal-closeout-select-reason"/>
        </label>

        <c:forEach var="closeOutReason" items="${reasonsList}" varStatus="loopStatus">
            <input type="radio" name="<portlet:namespace />appraisal.closeOutReasonId"
                id="<portlet:namespace />appraisal.closeOutReason-${closeOutReason.id}"
                value="${closeOutReason.id}"/>
            <label for="<portlet:namespace />appraisal.closeOutReason-${closeOutReason.id}">
                <c:out value="${closeOutReason.reason}"/></label><br />
        </c:forEach>


    </fieldset>

    <input type="submit" name="delete" value="<liferay-ui:message key="delete" />" />
    <input type="submit" name="cancel" value="<liferay-ui:message key="cancel" />" />
</form>
<%@ include file="/jsp/footer.jsp" %>