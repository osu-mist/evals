<div class="appraisal accordion-menu">
    <div class="osu-accordion-header" onclick="<portlet:namespace/>toggleContent('<portlet:namespace/>AppraisalInfo');">
      <img id="<portlet:namespace/>AppraisalInfoImageToggle" src="/cps/images/accordion/accordion_arrow_up.png"/>
      <liferay-ui:message key="appraisal-info" />
    </div>
    <div class="accordion-content" id="<portlet:namespace/>AppraisalInfo" style="display: block">
        <table class="appraisal-info collapses">
            <tbody>
                <tr>
                    <td><em><liferay-ui:message key="employee" />:</em>
                        <c:out value="${appraisal.job.employee.name}" />
                    </td>
                    <td><em><liferay-ui:message key="supervisor" />:</em>
                        <c:out value="${appraisal.job.currentSupervisor.employee.name}" />
                    </td>
                    <td><em><liferay-ui:message key="job-title" />:</em>
                        <c:out value="${appraisal.job.jobTitle}" />
                    </td>
                    <td><em><liferay-ui:message key="position-no" />:</em>
                        <c:out value="${appraisal.job.positionNumber}" />
                    </td>
                    <td><em><liferay-ui:message key="job-start-date"  />:</em>
                        <fmt:formatDate value="${appraisal.job.beginDate}" pattern="MM/dd/yy"/>
                    </td>
                </tr>
                <tr>
                    <td><em><liferay-ui:message key="appraisal-employee-id"/>:</em>
                        ${appraisal.job.employee.osuid}
                    </td>
                    <td><em><liferay-ui:message key="appraisal-type"/>:</em>
                        <liferay-ui:message key="appraisal-type-${appraisal.type}"/>
                    </td>
                    <td><em><liferay-ui:message key="reviewPeriod" />:</em>
                        ${appraisal.reviewPeriod}
                    </td>
                    <td><em><liferay-ui:message key="status" />:</em>
                        <liferay-ui:message key="${appraisal.status}" />
                    </td>
                    <td><em><liferay-ui:message key="appraisal-rating"/>:</em>
                        <c:if test="${not empty appraisal.rating and (permissionRule.evaluation == 'v' or permissionRule.evaluation == 'e')}">
                            <liferay-ui:message key="appraisal-rating-pdf-${appraisal.rating}"/>
                        </c:if>
                    </td>
                </tr>
            </tbody>
        </table>
    </div>
</div>
