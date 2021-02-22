<jsp:useBean id="myAppraisals" class="java.util.ArrayList" scope="request" />

<div id="<portlet:namespace/>accordionMenuMyStatus" class="accordion-menu">
    <div class="osu-accordion-header" onclick="<portlet:namespace/>toggleContent('<portlet:namespace/>MyStatus');">
      <img id="<portlet:namespace/>MyStatusImageToggle" src="/o/evals-portlet/images/accordion/accordion_arrow_up.png"/>
        <c:if test="${empty report}">
            <liferay-ui:message key="myStatus" />
        </c:if>
        <c:if test="${!empty report}">
            <c:if test="${!isMyReport}">
                <liferay-ui:message key="report-supervisor-evals" /> ${currentSupervisorName}
            </c:if>
        </c:if>
    </div>
    <div class="accordion-content" id="<portlet:namespace/>MyStatus" style="display: block;">
        <c:if test="${!empty myAppraisals}">
            <table class="main taglib-search-iterator">
                <thead>
                    <tr class="portlet-section-header results-header">
                        <th><liferay-ui:message key="job-title" /></th>
                        <th><liferay-ui:message key="reviewPeriod" /></th>
                        <c:if test="${!empty report}">
                            <th><liferay-ui:message key="overdue"/></th>
                        </c:if>
                        <th><liferay-ui:message key="status" /></th>
                        <c:if test="${isAdmin == 'true'}">
                            <th><liferay-ui:message key="delete"/></th>
                        </c:if>
                    </tr>
                </thead>
                <tbody>
                <c:forEach var="shortAppraisal" items="${myAppraisals}" varStatus="loopStatus">
                    <tr class="${loopStatus.index % 2 == 0 ? 'portlet-section-body results-row' : 'portlet-section-alternate results-row alt'}"
                    >
                        <td>${shortAppraisal.job.jobTitle}</td>
                        <td>${shortAppraisal.reviewPeriod}</td>
                        <c:if test="${!empty report}">
                            <td>${shortAppraisal.viewOverdue}</td>
                        </c:if>
                        <td><a href="<portlet:actionURL windowState="<%= WindowState.MAXIMIZED.toString()%>">
                            <portlet:param name="id" value="${shortAppraisal.id}"/>
                            <portlet:param  name="action" value="display"/>
                            <portlet:param  name="controller" value="AppraisalsAction"/>
                           </portlet:actionURL>">
                                <liferay-ui:message key="${shortAppraisal.viewStatus}" />
                           </a>
                        </td>
                        <c:if test="${isAdmin == 'true'}">
                            <td><a href="<portlet:actionURL windowState="<%= WindowState.MAXIMIZED.toString()%>">
                                <portlet:param name="id" value="${shortAppraisal.id}"/>
                                <portlet:param  name="action" value="deleteAppraisal"/>
                                <portlet:param  name="controller" value="TestsAction"/>
                              </portlet:actionURL>">
                                    <liferay-ui:message key="Delete" />
                              </a>
                            </td>
                        </c:if>
                        </tr>
                </c:forEach>
                </tbody>
            </table>
        </c:if>
        <c:if test="${empty myAppraisals}">
            <c:choose>
                <c:when test="${empty report}">
                    <p><liferay-ui:message key="noActiveAppraisals" /></p>
                </c:when>
                <c:otherwise>
                    <p>${currentSupervisorName} <liferay-ui:message key="report-supervisors-no-evals" /></p>
                </c:otherwise>
            </c:choose>

        </c:if>
    </div>
</div>
