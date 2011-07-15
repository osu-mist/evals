<div id="<portlet:namespace/>accordionMenuPassNotification" class="accordion-menu pass-notification">
    <div class="accordion-header" onclick="<portlet:namespace/>toggleContent('<portlet:namespace/>passNotification');">
        <table>
            <tr>
                <td align='left'><h2 class="accordion-header-left"></h2></td>
                <td align='left' class="accordion-header-middle">
                    <span class="accordion-header-content" id="<portlet:namespace/>_header_1">
                        &nbsp;&nbsp;<img id="<portlet:namespace/>passNotificationImageToggle" src="/cps/images/accordion/accordion_arrow_down.png"/>
                    </span>
                    <span class="accordion-header-content"><liferay-ui:message key="notifications"/></span>
                </td>
                <td align='right'><h2 class="accordion-header-right"></h2></td>
            </tr>
        </table>
    </div>
    <div class="accordion-content" id="<portlet:namespace/>passNotification" style="display: block;">
        <h3><liferay-ui:message key="my-eval-actions" /></h3>
        <c:if test="${!empty employeeActions}">
            <ul>
                <c:forEach var="reqAction" items="${employeeActions}">
                    <li><a href="<portlet:actionURL windowState="<%= WindowState.MAXIMIZED.toString()%>">
                    <c:forEach var="params" items="${reqAction.parameters}">
                        <portlet:param name="${params.key}" value="${params.value}"/>
                    </c:forEach>
                   </portlet:actionURL>">${reqAction.anchorText}</a></li>
                </c:forEach>
            </ul>
        </c:if>
        <c:if test="${empty employeeActions}">
            <p><liferay-ui:message key="no-employee-actions"/></p>
        </c:if>

        <c:if test="${isSupervisor == 'true' || isReviewer == 'true'}">
            <h3><liferay-ui:message key="my-admin-actions" /></h3>
            <c:if test="${!empty administrativeActions}">
                <ul>
                    <c:forEach var="reqAction" items="${administrativeActions}">
                        <li><a href="<portlet:actionURL windowState="<%= WindowState.MAXIMIZED.toString()%>">
                        <c:forEach var="params" items="${reqAction.parameters}">
                            <portlet:param name="${params.key}" value="${params.value}"/>
                        </c:forEach>
                       </portlet:actionURL>">${reqAction.anchorText}</a></li>
                    </c:forEach>
                </ul>
            </c:if>
            <c:if test="${empty administrativeActions}">
                <p><liferay-ui:message key="no-admin-actions"/></p>
            </c:if>
        </c:if>
    </div>
</div>
