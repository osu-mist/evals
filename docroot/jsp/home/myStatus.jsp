<jsp:useBean id="myActiveAppraisals" class="java.util.ArrayList" scope="request" />

<div id="<portlet:namespace/>accordionMenuMyStatus" class="accordion-menu">
    <div class="accordion-header" onclick="<portlet:namespace/>toggleContent('<portlet:namespace/>MyStatus');">
        <table>
            <tr>
                <td align='left'><div class="accordion-header-left"></div></td>
                <td align='left' class="accordion-header-middle">
                    <span class="accordion-header-content" id="<portlet:namespace/>_header_1">
                        &nbsp;&nbsp;<img id="<portlet:namespace/>MyStatusImageToggle" src="/cps/images/accordion/accordion_arrow_down.png"/>
                    </span>
                    <span class="accordion-header-content"><liferay-ui:message key="myStatus" /></span>
                </td>
                <td align='right'><div class="accordion-header-right"></div></td>
            </tr>
        </table>
    </div>
    <div class="accordion-content" id="<portlet:namespace/>MyStatus" style="display: block;">
        <c:if test="${!empty myActiveAppraisals}">
            <table class="taglib-search-iterator">
                <tr class="portlet-section-header results-header">
                    <th><liferay-ui:message key="job-title" /></th>
                    <th><liferay-ui:message key="reviewPeriod" /></th>
                    <th><liferay-ui:message key="status" /></th>
                </tr>
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
            </table>
        </c:if>
        <c:if test="${empty myActiveAppraisals}">
            <p><em><liferay-ui:message key="noActiveAppraisals" /></em></p>
        </c:if>
    </div>
</div>