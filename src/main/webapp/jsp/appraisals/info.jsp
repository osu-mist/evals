<h3 class="secret"><liferay-ui:message key="appraisal-info" /></h3>
<div class="appraisal accordion-menu">
    <div class="osu-accordion-header" onclick="<portlet:namespace/>toggleContent('<portlet:namespace/>AppraisalInfo');">
      <img id="<portlet:namespace/>AppraisalInfoImageToggle" src="/o/evals/images/accordion/accordion_arrow_up.png"/>
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
                        <c:out value="${appraisal.job.supervisor.employee.name}" />
                    </td>
                    <td colspan="2"><em><liferay-ui:message key="job-title" />:</em>
                        <c:out value="${appraisal.job.jobTitle}" />
                    </td>
                </tr>
                <tr>
                    <td><em><liferay-ui:message key="appraisal-employee-id"/>:</em>
                        ${appraisal.job.employee.osuid}
                    </td>
                    <td><em><liferay-ui:message key="position-no" />:</em>
                        <c:out value="${appraisal.job.positionNumber}" />
                    </td>
                    <td><em><liferay-ui:message key="job-start-date"  />:</em>
                        <fmt:formatDate value="${appraisal.job.beginDate}" pattern="MM/dd/yy"/>
                    </td>
                    <td><em><liferay-ui:message key="appraisal-type"/>:</em>
                        <liferay-ui:message key="appraisal-type-${appraisal.type}"/>
                    </td>
                </tr>
                <tr>
                    <td><em><liferay-ui:message key="reviewPeriod" />:</em>
                        ${appraisal.reviewPeriod}
                    </td>
                    <td><em><liferay-ui:message key="status" />:</em>
                        <liferay-ui:message key="${appraisal.viewStatus}" />
                        <c:if test="${appraisal.viewStatus == 'closed'}">
                            (Reason:<c:out value="${appraisal.closeOutReason.reason}" />)
                        </c:if>
                    </td>
                    <td colspan="2"><em><liferay-ui:message key="appraisal-rating"/>:</em>
                        <c:if test="${not empty appraisal.rating and (permissionRule.evaluation == 'v' or permissionRule.evaluation == 'e')}">
                            <c:forEach var="rating" items="${ratings}">
                                <c:if test="${appraisal.rating == rating.rate}">
                                    <c:out value="${rating.name}" />
                                </c:if>
                            </c:forEach>
                        </c:if>
                    </td>
                </tr>
            </tbody>
        </table>
    </div>
</div>
