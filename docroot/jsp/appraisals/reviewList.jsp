<%@ include file="/jsp/init.jsp" %>
<jsp:useBean id="reviews" class="java.util.ArrayList" scope="request" />

<h2><liferay-ui:message key="pending-reviews" /></h2>
<c:if test="${!empty reviews}">
    <div class="separator"></div>
    <table class="taglib-search-iterator">
        <tr class="portlet-section-header results-header">
            <th><liferay-ui:message key="employee"/></th>
            <th><liferay-ui:message key="supervisor"/></th>
            <th><liferay-ui:message key="job-title"/></th>
            <th><liferay-ui:message key="orgn-code-desc"/></th>
            <th><liferay-ui:message key="submit-date"/></th>
            <th><liferay-ui:message key="status"/></th>
        </tr>

    <c:forEach var="review" items="${reviews}" varStatus="loopStatus">
        <tr class="${loopStatus.index % 2 == 0 ? 'portlet-section-body results-row' : 'portlet-section-alternate results-row alt'}"
            onmouseover="this.className = 'portlet-section-body-hover results-row hover';"
            onmouseout="this.className = '${loopStatus.index % 2 == 0 ? 'portlet-section-body results-row' : 'portlet-section-alternate results-row alt'}';"
        >
            <td>${review.job.employee.name}</td>
            <td>${review.job.supervisor.employee.name}</td>
            <td>${review.job.jobTitle}</td>
            <td>${review.job.orgCodeDescription}</td>
            <td><fmt:formatDate value="${review.evaluationSubmitDate}" pattern="dd/MM/yy"/></td>
            <td><a href="<portlet:actionURL windowState="<%= WindowState.MAXIMIZED.toString()%>">
                                <portlet:param name="id" value="${review.id}"/>
                                <portlet:param  name="action" value="displayAppraisal"/>
                               </portlet:actionURL>">
               <liferay-ui:message key="${review.status}"/></a></td>
        </tr>
    </c:forEach>
    </table>
</c:if>



