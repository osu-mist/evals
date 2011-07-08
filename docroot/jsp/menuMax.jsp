<div id="pass-top-menu" class="action-menu-wrapper">
    <ul class="portlet-action-menu">
        <li><a href="<portlet:renderURL windowState="<%= WindowState.NORMAL.toString() %>">
                        <portlet:param name="action" value="displayHomeView"/>
                        </portlet:renderURL>"><liferay-ui:message key="home"/></a>
        </li>
        <c:if test="${isReviewer == 'true'}">
        <li><a href="#"><liferay-ui:message key="reviews"/></a>
            <ul>
                <c:forEach  var="appraisal" items="${pendingReviews}">
                <li><a href="<portlet:renderURL windowState="<%= WindowState.MAXIMIZED.toString() %>">
                        <portlet:param name="action" value="displayAppraisal"/>
                        <portlet:param name="id" value="${appraisal.id}"/>
                        </portlet:renderURL>">${appraisal.employeeName}</a></li>
                </c:forEach>
            </ul>
        </li>
        </c:if>
        <c:if test="${isSupervisor == 'true'}">
        <li><a href="#"><liferay-ui:message key="employees"/></a>
            <ul>
                <c:forEach  var="appraisal" items="${myTeamsAppraisals}">
                <li><a href="<portlet:renderURL windowState="<%= WindowState.MAXIMIZED.toString() %>">
                        <portlet:param name="action" value="displayAppraisal"/>
                        <portlet:param name="id" value="${appraisal.id}"/>
                        </portlet:renderURL>">${appraisal.employeeName}</a></li>
                </c:forEach>
            </ul>
        </li>
        </c:if>
    </ul>
</div>