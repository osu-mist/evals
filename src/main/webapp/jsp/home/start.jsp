<%@ include file="/jsp/init.jsp" %>

<c:if test="${empty hasNoEvalsAccess}">
    <div id="evaluations">
        <%@ include file="/jsp/home/actionsRequired.jsp" %>
        <%@ include file="/jsp/home/myStatus.jsp" %>
    </div>
</c:if>

<%@ include file="/jsp/home/helpLinks.jsp" %>
<c:if test="${isDemo}">
    <%@ include file="/jsp/home/switchUser.jsp" %>
</c:if>
<%@ include file="/jsp/home/testTools.jsp" %>
<%@ include file="/jsp/footer.jsp" %>
