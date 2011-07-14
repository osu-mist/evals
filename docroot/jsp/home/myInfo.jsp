<%@ include file="/jsp/init.jsp" %>

<h2><liferay-ui:message key="my-information"/></h2>

<div id="pass-my-info">
    <table>
        <tr>
            <td><strong><liferay-ui:message key="first-name"/></strong></td>
            <td><c:out value="${employee.firstName}"/></td>
        </tr>
        <tr>
            <td><strong><liferay-ui:message key="middle-name"/></strong></td>
            <td><c:out value="${employee.middleName}"/></td>
        </tr>
        <tr>
            <td><strong><liferay-ui:message key="last-name"/></strong></td>
            <td><c:out value="${employee.lastName}"/></td>
        </tr>
        <tr>
            <td><strong><liferay-ui:message key="email"/></strong></td>
            <td><c:out value="${employee.email}"/></td>
        </tr>
    </table>
    <hr />

    <h3><liferay-ui:message key="job-information"/></h3>
    <c:forEach var="job" items="${employee.jobs}" varStatus="loopStatus">
        <table>
            <tr>
                <td><strong><liferay-ui:message key="position-no"/></strong></td>
                <td><c:out value="${job.positionNumber}"/></td>
            </tr>
            <tr>
                <td><strong><liferay-ui:message key="jobTitle"/></strong></td>
            <td><c:out value="${job.jobTitle}"/></td>
            </tr>
            <tr>
                <td><strong><liferay-ui:message key="supervisor"/></strong></td>
            <td><c:out value="${job.supervisor.name}"/></td>
            </tr>
            <tr>
                <td><strong><liferay-ui:message key="job-start-date"/></strong></td>
                <td><fmt:formatDate value="${job.beginDate}" pattern="dd/MM/yy"/></td>
            </tr>
            <tr>
                <td><strong><liferay-ui:message key="appointment-type"/></strong></td>
                <td><c:out value="${job.appointmentType}"/></td>
            </tr>
            <tr>
                <td><strong><liferay-ui:message key="status"/></strong></td>
                <td><liferay-ui:message key="job-status-${job.status}"/></td>
            </tr>
        </table>
        <hr />
    </c:forEach>

    <p><em><liferay-ui:message key="how-to-correct-job-info"/></em></p>
</div>