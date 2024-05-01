<div id="<portlet:namespace/>accordionMenuPassNotification" class="accordion-menu pass-notification">
    <div class="osu-accordion-header" onclick="<portlet:namespace/>toggleContent('<portlet:namespace/>passNotification');">
      <img id="<portlet:namespace/>passNotificationImageToggle" src="/o/evals/images/accordion/accordion_arrow_up.png"/>
      <liferay-ui:message key="notifications" />
    </div>
    <div class="accordion-content" id="<portlet:namespace/>passNotification" style="display: block;">
        <h3><liferay-ui:message key="my-eval-actions" /></h3>
        <c:if test="${!empty employeeActions}">
            <ul class="pass-menu-list">
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
                <ul class="pass-menu-list">
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

        <c:if test="${initiateProfFaculty == 'true'}">
            <form style="margin:1.5em;" method="post"
                  action="<portlet:actionURL windowState="<%= WindowState.MAXIMIZED.toString() %>">
                        <portlet:param name="action" value="initiateProfessionalFacultyEvals"/>
                        <portlet:param name="controller" value="AppraisalsAction"/>
                        </portlet:actionURL>">
                <input type="submit" value="<liferay-ui:message key="prof-faculty-create-evaluations"/>"/>
            </form>
        </c:if>
    </div>
</div>
