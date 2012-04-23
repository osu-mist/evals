<jsp:useBean id="myTeamsActiveAppraisals" class="java.util.ArrayList" scope="request" />

<div id="<portlet:namespace/>accordionMenuMyTeam" class="accordion-menu">
    <div class="osu-accordion-header" onclick="<portlet:namespace/>toggleContent('<portlet:namespace/>MyTeam');">
      <img id="<portlet:namespace/>MyTeamImageToggle" src="/cps/images/accordion/accordion_arrow_up.png"/>
         <c:if test="${empty report}">
            <liferay-ui:message key="myTeam" />
         </c:if>
        <c:if test="${!empty report}">
            <liferay-ui:message key="report-supervisor-evals" />
            <c:if test="${isMyReport}">
                My
            </c:if>
            <c:if test="${!isMyReport}">
                ${currentSupervisorName}
            </c:if>
            <liferay-ui:message key="report-supervisor-tem-evals" />
        </c:if>
    </div>
    <div class="accordion-content" id="<portlet:namespace/>MyTeam" style="display: block;">
        <c:if test="${!empty myTeamsActiveAppraisals}">
            <table class="taglib-search-iterator narrow">
                <thead>
                    <tr class="portlet-section-header results-header">
                        <th><liferay-ui:message key="name" /></th>
                        <c:if test="${empty report}">
                            <th><liferay-ui:message key="appointment-type" /></th>
                        </c:if>
                        <th><liferay-ui:message key="reviewPeriod" /></th>
                        <c:if test="${!empty report}">
                            <th><liferay-ui:message key="overdue" /></th>
                        </c:if>
                        <th><liferay-ui:message key="status" /></th>
                    </tr>
                </thead>
                <tbody>
                <c:forEach var="shortAppraisal" items="${myTeamsActiveAppraisals}" varStatus="loopStatus">
                    <tr class="${loopStatus.index % 2 == 0 ? 'portlet-section-body results-row' : 'portlet-section-alternate results-row alt'}"
                    >
                        <td>${shortAppraisal.job.employee.name}</td>
                        <c:if test="${empty report}">
                            <td><liferay-ui:message key="${shortAppraisal.job.appointmentType}" /></td>
                        </c:if>
                        <td>${shortAppraisal.reviewPeriod}</td>
                        <c:if test="${!empty report}">
                            <td>${shortAppraisal.viewOverdue}</td>
                        </c:if>
                        <td><a href="<portlet:actionURL windowState="<%= WindowState.MAXIMIZED.toString()%>">
                            <portlet:param name="id" value="${shortAppraisal.id}"/>
                            <portlet:param  name="action" value="display"/>
                            <portlet:param  name="controller" value="AppraisalsAction"/>
                           </portlet:actionURL>"><liferay-ui:message key="${shortAppraisal.viewStatus}" /></a>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </c:if>
        <c:if test="${empty myTeamsActiveAppraisals}">
            <p><em><liferay-ui:message key="noTeamActiveAppraisals" /></em></p>
        </c:if>
    </div>
</div>
