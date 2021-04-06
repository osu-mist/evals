<%@ include file="/jsp/init.jsp" %>

<%@ include file="/jsp/home/actionsRequired.jsp" %>
<%@ include file="/jsp/home/admin.jsp" %>
<%@ include file="/jsp/home/search.jsp" %>
<%@ include file="/jsp/home/helpLinks.jsp" %>
<c:if test="${isDemo}">
    <%@ include file="/jsp/home/switchUser.jsp" %>
</c:if>
<%@ include file="/jsp/footer.jsp" %>
