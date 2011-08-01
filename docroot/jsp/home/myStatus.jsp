<jsp:useBean id="myActiveAppraisals" class="java.util.ArrayList" scope="request" />

<div id="<portlet:namespace/>accordionMenuMyStatus" class="accordion-menu">
    <div class="osu-accordion-header" onclick="<portlet:namespace/>toggleContent('<portlet:namespace/>MyStatus');">
      <img id="<portlet:namespace/>MyStatusImageToggle" src="/cps/images/accordion/accordion_arrow_down.png"/>
      <liferay-ui:message key="myStatus" />
    </div>
    <div class="accordion-content" id="<portlet:namespace/>MyStatus" style="display: block;">
        <c:if test="${!empty myActiveAppraisals}">
            <table class="taglib-search-iterator">
                <thead>
                    <tr class="portlet-section-header results-header">
                        <th><liferay-ui:message key="job-title" /></th>
                        <th><liferay-ui:message key="reviewPeriod" /></th>
                        <th><liferay-ui:message key="status" /></th>
                    </tr>
                </thead>
                <tbody>
                <c:forEach var="shortAppraisal" items="${myActiveAppraisals}" varStatus="loopStatus">
                    <tr class="${loopStatus.index % 2 == 0 ? 'portlet-section-body results-row' : 'portlet-section-alternate results-row alt'}"
                    >
                        <td>${shortAppraisal.job.jobTitle}</td>
                        <td>${shortAppraisal.reviewPeriod}</td>
                        <td><a href="<portlet:actionURL windowState="<%= WindowState.MAXIMIZED.toString()%>">
                            <portlet:param name="id" value="${shortAppraisal.id}"/>
                            <portlet:param  name="action" value="displayAppraisal"/>
                           </portlet:actionURL>">
                                <liferay-ui:message key="${shortAppraisal.status}" />
                           </a>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </c:if>
        <c:if test="${empty myActiveAppraisals}">
            <p><em><liferay-ui:message key="noActiveAppraisals" /></em></p>
        </c:if>
    </div>
</div>