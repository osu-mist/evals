<jsp:useBean id="myTeamsActiveAppraisals" class="java.util.ArrayList" scope="request" />

<div id="<portlet:namespace/>accordionMenuMyTeam" class="accordion-menu">
    <div class="osu-accordion-header" onclick="<portlet:namespace/>toggleContent('<portlet:namespace/>MyTeam');">
      <img id="<portlet:namespace/>MyTeamImageToggle" src="/o/evals/images/accordion/accordion_arrow_up.png"/>
         <c:if test="${empty report}">
            <liferay-ui:message key="myTeam" />
         </c:if>
        <c:if test="${!empty report}">
            <c:if test="${isMyReport}">
                <liferay-ui:message key="report-supervisor-my-team-evals" />
            </c:if>
            <c:if test="${!isMyReport}">
                <liferay-ui:message key="report-supervisor-team-evals" /> ${currentSupervisorName}
            </c:if>
        </c:if>
    </div>
    <div class="accordion-content" id="<portlet:namespace/>MyTeam" style="display: block;">
        <p><liferay-ui:message key="myTeamWarning" /></p>
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
                        <td>
                            <c:if test="${shortAppraisal.id != 0}">
                                ${shortAppraisal.reviewPeriod}
                            </c:if>
                        </td>
                        <c:if test="${!empty report}">
                            <td>${shortAppraisal.viewOverdue}</td>
                        </c:if>
                        <td>
                            <c:if test="${shortAppraisal.id != 0}">
                                <a href="<portlet:actionURL windowState="<%= WindowState.MAXIMIZED.toString()%>">
                                        <portlet:param name="id" value="${shortAppraisal.id}"/>
                                        <portlet:param  name="action" value="display"/>
                                        <portlet:param  name="controller" value="AppraisalsAction"/>
                                    </portlet:actionURL>"><liferay-ui:message key="${shortAppraisal.viewStatus}" />
                                </a>
                            </c:if>
                            <%-- The only shortAppraisals with an id of 0 are from employees that need to be
                            initialized manually --%>
                            <c:if test="${shortAppraisal.id == 0}">
                                <a href="<portlet:actionURL windowState="<%= WindowState.MAXIMIZED.toString() %>">
                                        <portlet:param name="action" value="initiateProfessionalFacultyEvals"/>
                                        <portlet:param name="controller" value="AppraisalsAction"/>
                                    </portlet:actionURL>"><liferay-ui:message key="prof-faculty-create-evaluation-short" />
                                </a>
                            </c:if>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </c:if>
        <c:if test="${empty myTeamsActiveAppraisals}">
            <c:choose>
                <c:when test="${empty report}">
                    <p><liferay-ui:message key="noTeamActiveAppraisals" /></p>

                </c:when>
                <c:otherwise>
                    <p><liferay-ui:message key="report-supervisors-team-no-evals" /> ${currentSupervisorName}</p>
                </c:otherwise>
            </c:choose>
        </c:if>
    </div>
</div>
