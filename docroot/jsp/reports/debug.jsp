<h2>Debug Information</h2>
<h3>Scope: ${scope} , ScopeValue: ${scopeValue}</h3>

<%--<h2>Data</h2>--%>
<%--<c:forEach var="row" items="${reportAppraisals}">--%>
    <%--<c:forEach var="column" items="${row}">--%>
        <%--<c:out value="${column}"/><br/>--%>
    <%--</c:forEach>--%>
    <%-----------------------------------------------------------------<br />--%>
<%--</c:forEach>--%>


<h3>Chart Data</h3>
<c:forEach var="row" items="${chartData}">
    <c:forEach var="column" items="${row}">
        <c:out value="${column}"/><br/>
    </c:forEach>
    ---------------------------------------------------------------<br />
</c:forEach>