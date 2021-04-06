<div class="portlet-top-menu action-menu-wrapper menu-max">
    <ul class="portlet-action-menu">
        <li><a href="<portlet:renderURL windowState="<%= WindowState.NORMAL.toString() %>">
                        <portlet:param name="action" value="display"/>
                        <portlet:param name="controller" value="HomeAction"/>
                        </portlet:renderURL>"><liferay-ui:message key="evals-home"/></a>
        </li>
        <c:if test="${isReviewer == 'true' and !empty pendingReviews}">
        <li><a href="#"><liferay-ui:message key="reviews"/></a>
            <ul>
                <c:forEach  var="appraisal" items="${pendingReviews}">
                <li><a href="<portlet:renderURL windowState="<%= WindowState.MAXIMIZED.toString() %>">
                        <portlet:param name="action" value="display"/>
                        <portlet:param name="controller" value="AppraisalsAction"/>
                        <portlet:param name="id" value="${appraisal.id}"/>
                        </portlet:renderURL>">${appraisal.job.employee.name}</a></li>
                </c:forEach>
            </ul>
        </li>
        </c:if>
        <c:if test="${isSupervisor == 'true' and !empty myTeamsAppraisals}">
        <li><a href="#"><liferay-ui:message key="employees"/></a>
            <ul>
                <c:forEach  var="appraisal" items="${myTeamsAppraisals}">
                <li><a href="<portlet:renderURL windowState="<%= WindowState.MAXIMIZED.toString() %>">
                        <portlet:param name="action" value="display"/>
                        <portlet:param name="controller" value="AppraisalsAction"/>
                        <portlet:param name="id" value="${appraisal.id}"/>
                        </portlet:renderURL>">${appraisal.job.employee.name}</a></li>
                </c:forEach>
            </ul>
        </li>
        </c:if>
    </ul>
</div>