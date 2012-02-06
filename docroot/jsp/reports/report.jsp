<%@ page import="edu.osu.cws.evals.portlet.ReportsAction" %>
<%@ include file="/jsp/init.jsp"%>

<h1>Scope: ${scope} , ScopeValue: ${scopeValue}</h1>
<%@ include file="breadcrumbs.jsp"%>

<h2>Data</h2>
<c:forEach var="row" items="${reportAppraisals}">
    <c:forEach var="column" items="${row}">
        <c:out value="${column}"/><br/>
    </c:forEach>
    ---------------------------------------------------------------<br />
</c:forEach>

<h2>Drill Down</h2>
<ul>
<c:forEach var="unit" items="${units}">
    <li><a href="<portlet:actionURL windowState="<%= WindowState.MAXIMIZED.toString() %>">
        <portlet:param name="action" value="report"/>
        <portlet:param name="controller" value="ReportsAction"/>
        <portlet:param name="<%= ReportsAction.SCOPE %>" value="${nextScope}"/>
        <portlet:param name="<%= ReportsAction.SCOPE_VALUE %>" value="${unit.key}"/>
    </portlet:actionURL>">${unit.key} - ${unit.value}</a></li>
</c:forEach>
</ul>