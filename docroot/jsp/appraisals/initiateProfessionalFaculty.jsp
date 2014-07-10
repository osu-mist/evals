<%@ include file="/jsp/init.jsp" %>
<h2><liferay-ui:message key="prof-faculty-create-evals-title"/></h2>

<fieldset>
    <legend><liferay-ui:message key="prof-faculty-create-evals-details"/></legend>
    <p class="pass-form-text"><liferay-ui:message key="prof-faculty-create-evals-instructions"/></p>
</fieldset>

<c:if test="${not empty shortJobsWithEvals}">
    <fieldset>
        <legend><liferay-ui:message key="prof-faculty-create-evals-my-prof-team-with-evals"/></legend>

        <table class="main taglib-search-iterator">
            <thead>
            <tr class="portlet-section-header results-header">
                <th><liferay-ui:message key="name"/></th>
                <th><liferay-ui:message key="position-number"/></th>
                <th><liferay-ui:message key="jobTitle"/></th>
                <th><liferay-ui:message key="reviewPeriod" /></th>
                <th><liferay-ui:message key="supervising-position-number"/></th>
            </tr>
            </thead>
            <tbody>
                <c:forEach var="shortJob" items="${shortJobsWithEvals}" varStatus="loopStatus">
                    <tr class="${loopStatus.index % 2 == 0 ? 'portlet-section-body results-row' : 'portlet-section-alternate results-row alt'}">
                        <td><c:out value="${shortJob.employee.name}"/></td>
                        <td><c:out value="${shortJob.positionNumber}"/></td>
                        <td><c:out value="${shortJob.jobTitle}"/></td>
                        <td><c:out value="${shortJob.reviewPeriod}"/></td>
                        <td><c:out value="${shortJob.supervisor.positionNumber}"/></td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
    </fieldset>
</c:if>

<fieldset>
    <legend><liferay-ui:message key="prof-faculty-create-evals-my-prof-team-without-evals"/></legend>

    <table class="main taglib-search-iterator">
        <thead>
        <tr class="portlet-section-header results-header">
            <th><liferay-ui:message key="name"/></th>
            <th><liferay-ui:message key="position-number"/></th>
            <th><liferay-ui:message key="jobTitle"/></th>
            <th><liferay-ui:message key="supervising-position-number"/></th>
        </tr>
        </thead>
        <tbody>
            <c:forEach var="shortJob" items="${jobsWithoutEvals}" varStatus="loopStatus">
                <tr class="${loopStatus.index % 2 == 0 ? 'portlet-section-body results-row' : 'portlet-section-alternate results-row alt'}">
                    <td><c:out value="${shortJob.employee.name}"/></td>
                    <td><c:out value="${shortJob.positionNumber}"/></td>
                    <td><c:out value="${shortJob.jobTitle}"/></td>
                    <td><c:out value="${shortJob.supervisor.positionNumber}"/></td>
                </tr>
            </c:forEach>
        </tbody>
    </table>
</fieldset>

<form method="post" action="<portlet:actionURL windowState="<%= WindowState.NORMAL.toString() %>">
    <portlet:param name="action" value="createProfFacultyEvaluations"/>
    <portlet:param name="controller" value="AppraisalsAction"/>
    </portlet:actionURL>">

    <fieldset>
        <legend><liferay-ui:message key="prof-faculty-choose-cycle"/></legend>
        <liferay-ui:message key="prof-faculty-create-evals-month"/>
        <select id="month" name="month">
            <option value="">Select a month</option>
            <c:forEach var="month" items="${months}" varStatus="loopStatus">
                <option value="${loopStatus.count}"><c:out value="${month}"/></option>
            </c:forEach>
        </select>

        <liferay-ui:message key="prof-faculty-create-evals-year"/>
        <select id="year" name="year">
            <option value="">Select a year</option>
            <c:forEach var="year" items="${years}" varStatus="loopStatus">
                <option value="${year}"><c:out value="${year}"/></option>
            </c:forEach>
        </select>

        <input type="hidden" id="currentRole" name="currentRole" value="<%= ActionHelper.ROLE_SUPERVISOR %>"/>
        <input type="submit" value="<liferay-ui:message key="prof-faculty-create-evals-submit"/>"/>
    </fieldset>
</form>

<script type="text/javascript">
    jQuery(document).ready(function() {
        jQuery("form").submit(function() {
            if (jQuery('#year').val() == '' || jQuery('#month').val() == '') {
                alert('<liferay-ui:message key="prof-faculty-select-cycle"/>');
                return false;
            }
            return true;
        });
    });
</script>


<%@ include file="/jsp/footer.jsp" %>