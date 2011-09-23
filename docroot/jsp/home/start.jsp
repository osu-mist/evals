<%@ include file="/jsp/init.jsp" %>

<c:if test="${empty hasNoEvalsAccess}">
    <%@ include file="/jsp/home/actionsRequired.jsp" %>
    <%@ include file="/jsp/home/myStatus.jsp" %>
</c:if>

<%@ include file="/jsp/home/helpLinks.jsp" %>
<%@ include file="/jsp/footer.jsp" %>
