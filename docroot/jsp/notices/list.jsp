<%@ include file="/jsp/init.jsp"%>

<h2><liferay-ui:message key="notice-list-title"/></h2>

<table class="taglib-search-iterator">
    <thead>
    <tr class="portlet-section-header results-header">
        <th><liferay-ui:message key="name"/></th>
        <th><liferay-ui:message key="text"/></th>
        <th><liferay-ui:message key="actions"/></th>
    </tr>
    </thead>
    <tbody>
    <c:forEach var="notice" items="${noticeList}" varStatus="loopStatus">
        <tr class="${loopStatus.index % 2 == 0 ? 'portlet-section-body results-row' : 'portlet-section-alternate results-row alt'}"
                >
            <td><c:out value="${notice.name}"/></td>
            <td>
                <c:out value="${notice.text}"/></td>
            <td>
                <a class="<portlet:namespace/>notice-edit"
                    href="<portlet:renderURL
            windowState="<%= WindowState.MAXIMIZED.toString() %>">
            <portlet:param name="ancestorID" value="${notice.ancestorID}"/>
            <portlet:param name="action" value="edit"/>
            <portlet:param name="controller" value="NoticeAction"/>
        </portlet:renderURL>"><liferay-ui:message key="edit"/></a>
            </td>
        </tr>
    </c:forEach>
    </tbody>
</table>

<%@ include file="/jsp/footer.jsp" %>