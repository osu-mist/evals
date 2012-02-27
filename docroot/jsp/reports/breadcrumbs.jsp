<div class="osu-cws-breadcrumbs">
    <c:forEach var="breadcrumb" items="${breadcrumbList}" varStatus="loopStatus">
        <c:if test="${loopStatus.last}">
            ${breadcrumb.anchorText}
        </c:if>

        <c:if test="${!loopStatus.last}">
            <a href="<portlet:actionURL windowState="<%= WindowState.MAXIMIZED.toString() %>">
        <portlet:param name="action" value="report"/>
        <portlet:param name="controller" value="ReportsAction"/>
        <portlet:param name="<%= ReportsAction.BREADCRUMB_INDEX %>" value="${loopStatus.index}"/>
        <portlet:param name="<%= ReportsAction.SCOPE %>" value="${breadcrumb.scope}"/>
        <portlet:param name="<%= ReportsAction.SCOPE_VALUE %>" value="${breadcrumb.scopeValue}"/>
        <portlet:param name="requestBreadcrumbs" value="${requestBreadcrumbs}"/>
        </portlet:actionURL>">${breadcrumb.anchorText}</a>
            &gt;
        </c:if>
    </c:forEach>
</div>