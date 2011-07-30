<c:if test="${isSupervisor == 'true'}">
<jsp:useBean id="myTeamsActiveAppraisals" class="java.util.ArrayList" scope="request" />
    <div id="<portlet:namespace/>accordionMenuMyTeam" class="accordion-menu">
        <div class="osu-accordion-header" onclick="<portlet:namespace/>toggleContent('<portlet:namespace/>MyTeam');">
          <img id="<portlet:namespace/>MyTeamImageToggle" src="/cps/images/accordion/accordion_arrow_down.png"/>
          <liferay-ui:message key="myTeam" />
        </div>
        <div class="accordion-content" id="<portlet:namespace/>MyTeam" style="display: block;">
            <c:if test="${!empty myTeamsActiveAppraisals}">
                <table class="taglib-search-iterator">
                    <tr class="portlet-section-header results-header">
                        <th><liferay-ui:message key="name" /></th>
                        <th><liferay-ui:message key="appointment-type" /></th>
                        <th><liferay-ui:message key="reviewPeriod" /></th>
                        <th><liferay-ui:message key="status" /></th>
                    </tr>
                    <c:forEach var="shortAppraisal" items="${myTeamsActiveAppraisals}" varStatus="loopStatus">
                        <tr class="${loopStatus.index % 2 == 0 ? 'portlet-section-body results-row' : 'portlet-section-alternate results-row alt'}"
                        >
                            <td>${shortAppraisal.job.employee.name}</td>
                            <td><liferay-ui:message key="${shortAppraisal.job.appointmentType}" /></td>
                            <td>${shortAppraisal.reviewPeriod}
                            </td>
                            <td><a href="<portlet:actionURL windowState="<%= WindowState.MAXIMIZED.toString()%>">
                                <portlet:param name="id" value="${shortAppraisal.id}"/>
                                <portlet:param  name="action" value="displayAppraisal"/>
                               </portlet:actionURL>"><liferay-ui:message key="${shortAppraisal.status}" /></a>
                            </td>
                        </tr>
                    </c:forEach>
                </table>
            </c:if>
            <c:if test="${empty myTeamsActiveAppraisals}">
                <p><em><liferay-ui:message key="noTeamActiveAppraisals" /></em></p>
            </c:if>
        </div>
    </div>
</c:if>