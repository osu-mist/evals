<jsp:useBean id="requiredActions" class="java.util.ArrayList" scope="request" />

<c:if test="${!empty requiredActions}">
    <h2><liferay-ui:message key="actions-required" /></h2>
    <ul>
        <c:forEach var="reqAction" items="${requiredActions}">
            <li><a href="<portlet:actionURL>
            <c:forEach var="params" items="${reqAction.parameters}">
                <portlet:param name="${params.key}" value="${params.value}"/>
            </c:forEach>
           </portlet:actionURL>">${reqAction.anchorText}</a></li>
        </c:forEach>
    </ul>
</c:if>


