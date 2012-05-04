<c:if test="${!empty listAppraisals}">
  <table class="taglib-search-iterator report-data">
    <caption><liferay-ui:message key="report-list-title" /></caption>
      <thead>
          <tr class="portlet-section-header results-header">
              <th><liferay-ui:message key="employee"/></th>
              <th><liferay-ui:message key="reviewPeriod"/></th>
              <th><liferay-ui:message key="supervisor"/></th>
              <th><liferay-ui:message key="overdue"/></th>
              <th><liferay-ui:message key="status"/></th>
          </tr>
      </thead>
      <tbody>
      <c:forEach var="appraisal" items="${listAppraisals}" varStatus="loopStatus">
          <tr class="${loopStatus.index % 2 == 0 ? 'portlet-section-body results-row' :
                  'portlet-section-alternate results-row alt'}">
              <td><c:out value="${appraisal.job.employee.name}"/></td>
              <td><c:out value="${appraisal.reviewPeriod}"/></td>
              <td>
                  <c:if test="${appraisal.job.supervisor != null}">
                      <a href="<portlet:actionURL windowState="<%= WindowState.MAXIMIZED.toString() %>">
                        <portlet:param name="action" value="report"/>
                        <portlet:param name="controller" value="ReportsAction"/>
                        <portlet:param name="<%= ReportsAction.SCOPE %>" value="<%= ReportsAction.SCOPE_SUPERVISOR %>"/>
                        <portlet:param name="<%= ReportsAction.SCOPE_VALUE %>" value="${appraisal.job.supervisor.idKey}"/>
                        <portlet:param name="requestBreadcrumbs" value="${breadcrumbsWithRootOnly}" />
                        <portlet:param name="isReportListSearch" value="true"/>
                        </portlet:actionURL>">
                        <c:out value="${appraisal.job.supervisor.employee.name}"/></a>
                  </c:if>
              </td>
              <td><c:out value="${appraisal.viewOverdue}"/></td>
              <td><a href="<portlet:actionURL windowState="<%= WindowState.MAXIMIZED.toString()%>">
                              <portlet:param name="id" value="${appraisal.id}"/>
                              <portlet:param  name="action" value="display"/>
                              <portlet:param  name="controller" value="AppraisalsAction"/>
                             </portlet:actionURL>">
                  <liferay-ui:message key="${appraisal.viewStatus}"/></a></td>
          </tr>
      </c:forEach>
      </tbody>
  </table>
</c:if>