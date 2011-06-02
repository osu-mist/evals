<table class="appraisal-info">
    <tr>
        <th><liferay-ui:message key="employee" />:</th>
        <td><c:out value="${appraisal.job.employee.name}" /></td>
        <th><liferay-ui:message key="job-title" />:</th>
        <td><c:out value="${appraisal.job.jobTitle}" /></td>
        <th><liferay-ui:message key="reviewPeriod" />:</th>
        <td>
            <fmt:formatDate value="${appraisal.startDate}" pattern="MM/yyyy"/> -
            <fmt:formatDate value="${appraisal.endDate}" pattern="MM/yyyy"/>
        </td>
        <th><liferay-ui:message key="status" />:</th>
        <td>
            <c:choose>
                <c:when test="${userRole == 'employee' && (appraisal.status == 'appraisal-due'
                    || appraisal.status == 'appraisal-past-due'  || appraisal.status == 'review-due'
                    || appraisal.status == 'review-past-due' || appraisal.status == 'release-due'
                    || appraisal.status == 'release-past-due')}">
               <liferay-ui:message key="in-review"/>
                </c:when>
                <c:otherwise>
                    <liferay-ui:message key="${appraisal.status}" />
                </c:otherwise>
            </c:choose>
        </td>
    </tr>
    <tr>
        <th><liferay-ui:message key="appointment-type" />:</th>
        <td><liferay-ui:message key="${appraisal.job.appointmentType}"  /></td>
        <th><liferay-ui:message key="position-no" />:</th>
        <td><c:out value="${appraisal.job.positionNumber}" /></td>
        <th><liferay-ui:message key="job-start-date"  />:</th>
        <td><c:out value="${appraisal.job.beginDate}" /></td>
        <th><liferay-ui:message key="supervisor" />:</th>
        <td><c:out value="${appraisal.job.currentSupervisor.employee.name}" /></td>
    </tr>
</table>