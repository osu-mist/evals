<jsp:useBean id="myTeamsActiveAppraisals" class="java.util.ArrayList" scope="request" />
<jsp:useBean id="myTeamsActiveClassifiedITAppraisals" class="java.util.ArrayList" scope="request" />

<div id="<portlet:namespace/>accordionMenuMyTeam" class="accordion-menu">
    <div class="osu-accordion-header" onclick="<portlet:namespace/>toggleContent('<portlet:namespace/>MyTeam');">
      <img id="<portlet:namespace/>MyTeamImageToggle" src="/cps/images/accordion/accordion_arrow_up.png"/>
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
    <div class="osu-accordion-header" onclick="<portlet:namespace/>toggleContent('<portlet:namespace/>MyTeamClassified');">
        <img id="<portlet:namespace/>MyTeamClassifiedImageToggle" src="/cps/images/accordion/accordion_arrow_up.png"/>
        <c:if test="${empty report}">
            <liferay-ui:message key="myTeam" />
        </c:if>
        <c:if test="${!empty report}">
            <c:if test="${isMyReport}">
                <liferay-ui:message key="report-supervisor-my-team-classifiedIT-evals" />
            </c:if>
            <c:if test="${!isMyReport}">
                <liferay-ui:message key="report-supervisor-team-classifiedIT-evals" /> ${currentSupervisorName}
            </c:if>
        </c:if>
    </div>
    <div class="accordion-content" id="<portlet:namespace/>MyTeamClassified" style="display: block;">
        <c:if test="${!empty myTeamsActiveClassifiedITAppraisals}">
            <table class="taglib-search-iterator narrow">
                <thead>
                <tr class="portlet-section-header results-header">
                    <th><liferay-ui:message key="name" /></th>
                    <th><liferay-ui:message key="reviewPeriod" /></th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="classifiedITAppraisal" items="${myTeamsActiveClassifiedITAppraisals}" varStatus="loopStatus">
                    <tr class="${loopStatus.index % 2 == 0 ? 'portlet-section-body results-row' : 'portlet-section-alternate results-row alt'}"
                            >
                        <td>${classifiedITAppraisal.employeeName}</td>
                        <td>${classifiedITAppraisal.reviewPeriod}</td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </c:if>
        <c:if test="${empty myTeamsActiveClassifiedITAppraisals}">
            <c:choose>
                <c:when test="${empty report}">
                    <p><liferay-ui:message key="noTeamClassifiedIT" /></p>

                </c:when>
                <c:otherwise>
                    <p><liferay-ui:message key="report-supervisors-team-no-evals" /> ${currentSupervisorName}</p>
                </c:otherwise>
            </c:choose>
        </c:if>
    </div>
</div>
