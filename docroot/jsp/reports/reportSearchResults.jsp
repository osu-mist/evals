<c:if test="${!empty searchResults}">
    <table class="taglib-search-iterator">
        <thead>
            <tr class="portlet-section-header results-header">
                <th><liferay-ui:message key="name"/></th>
                <th><liferay-ui:message key="jobTitle"/></th>
                <th><liferay-ui:message key="supervisor"/></th>
                <th><liferay-ui:message key="position-no"/></th>
                <th><liferay-ui:message key="osuid"/></th>
            </tr>
        </thead>
        <tbody>
    <c:forEach var="job" items="${searchResults}" varStatus="loopStatus">
        <tr class="${loopStatus.index % 2 == 0 ? 'portlet-section-body results-row' : 'portlet-section-alternate results-row alt'}">
            <td><a href="<portlet:actionURL windowState="<%= WindowState.MAXIMIZED.toString()%>">
                    <portlet:param name="action" value="report"/>
                    <portlet:param name="controller" value="ReportsAction"/>
                    <portlet:param name="requestBreadcrumbs" value="${requestBreadcrumbs}" />
                    <portlet:param name="<%= ReportsAction.SCOPE %>" value="<%= ReportsAction.SCOPE_SUPERVISOR %>"/>
                    <portlet:param name="<%= ReportsAction.SCOPE_VALUE %>" value="${job.idKey}"/>
                    <portlet:param name="requestBreadcrumbs" value="${breadcrumbsWithRootOnly}" />
                    </portlet:actionURL>">${job.employee.name}</a></td>
            <td>${job.jobTitle}</td>
            <td>${job.supervisor.employee.name}</td>
            <td>${job.positionNumber}</td>
            <td>${job.employee.osuid}</td>
        </tr>
    </c:forEach>
        </tbody>
    </table>
</c:if>