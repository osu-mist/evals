<table class="appraisal-info">
    <tr>
        <th><liferay-ui:message key="employee" />:</th>
        <td><c:out value="${appraisal.job.employee.name}" /></td>
        <th><liferay-ui:message key="job-title" />:</th>
        <td><c:out value="${appraisal.job.jobTitle}" /></td>
        <th><liferay-ui:message key="reviewPeriod" />:</th>
        <td>${appraisal.reviewPeriod}</td>
        <th><liferay-ui:message key="status" />:</th>
        <td>
            <liferay-ui:message key="${appraisal.status}" />
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