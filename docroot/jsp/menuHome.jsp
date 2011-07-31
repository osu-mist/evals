<div id="pass-top-menu" class="action-menu-wrapper">
    <ul class="portlet-action-menu">
        <li
        <c:if test="${not empty isHome}">
        class="active"
        </c:if>
        ><a href="<portlet:renderURL windowState="<%= WindowState.NORMAL.toString() %>">
                        <portlet:param name="action" value="displayHomeView"/>
                        </portlet:renderURL>"><liferay-ui:message key="home"/></a></li>
        <li><a href="#"><liferay-ui:message key="settings"/></a>
            <ul>
                <li><a href="<portlet:renderURL windowState="<%= WindowState.NORMAL.toString() %>">
                        <portlet:param name="action" value="displayMyInformation"/>
                        </portlet:renderURL>"><liferay-ui:message key="my-information"/></a></li>
            </ul>
        </li>

        <c:if test="${isAdmin == 'true' || isReviewer == 'true' || isSupervisor == 'true'}">
        <li class="pass-roles"><a href="#"><liferay-ui:message key="roles"/></a>
            <ul>
                <c:if test="${isAdmin == 'true'}">
                <li><a href="<portlet:renderURL windowState="<%= WindowState.NORMAL.toString() %>">
                        <portlet:param name="action" value="displayAdminHomeView"/>
                        </portlet:renderURL>"><liferay-ui:message key="role-admin"/></a></li>
                </c:if>
                <c:if test="${isReviewer == 'true'}">
                <li><a href="<portlet:renderURL windowState="<%= WindowState.MAXIMIZED.toString() %>">
                        <portlet:param name="action" value="displayReviewList"/>
                        </portlet:renderURL>"><liferay-ui:message key="role-reviewer"/></a></li>
                </c:if>
                <c:if test="${isSupervisor == 'true'}">
                <li><a href="<portlet:renderURL windowState="<%= WindowState.NORMAL.toString() %>">
                        <portlet:param name="action" value="displaySupervisorHomeView"/>
                        </portlet:renderURL>"><liferay-ui:message key="role-supervisor"/></a></li>
                </c:if>
                <li><a href="<portlet:renderURL windowState="<%= WindowState.NORMAL.toString() %>">
                        <portlet:param name="action" value="displayHomeView"/>
                        </portlet:renderURL>"><liferay-ui:message key="role-self"/></a></li>
            </ul>
        </li>
        </c:if>
    </ul>
</div>