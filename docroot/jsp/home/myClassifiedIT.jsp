
<div class="osu-accordion-header" onclick="<portlet:namespace/>toggleContent('<portlet:namespace/>MyTeamClassified');">
    <img id="<portlet:namespace/>MyTeamClassifiedImageToggle" src="/cps/images/accordion/accordion_arrow_up.png"/>
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
</div>

