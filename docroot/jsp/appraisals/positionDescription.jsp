<%@ taglib prefix="lifera-ui" uri="http://java.sun.com/jstl/fmt_rt" %>
<%@ include file="/jsp/init.jsp" %>
<% PositionDescription formPositionDescription = (PositionDescription) renderRequest.getAttribute("positionDescription"); %>

<h2><liferay-ui:message key="appraisal-position-description"/></h2>

<c:if test="${positionDescription != null}">
    <span class="portlet-msg-alert">
        <liferay-ui:message key="position-description-disclaimer"/>
    </span>
</c:if>

<c:if test="${positionDescription != null}">
    <div class="appraisal accordion-menu">
        <div class="osu-accordion-header" onclick="<portlet:namespace/>toggleContent('<portlet:namespace/>PositionDescriptionInfo');">
            <img id="<portlet:namespace/>PositionDescriptionInfoImageToggle" src="/images/accordion/accordion_arrow_up.png"/>
            <liferay-ui:message key="position-information" />
        </div>
        <div class="accordion-content" id="<portlet:namespace/>PositionDescriptionInfo" style="display: block">
            <table class="appraisal-info collapses">
                <tbody>
                <tr>
                    <td><em><liferay-ui:message key="position-title" />:</em>
                        <c:out value="${positionDescription.positionTitle}" />
                    </td>
                    <td><em><liferay-ui:message key="position-class-code" />:</em>
                        <c:out value="${positionDescription.positionTitleCode}"/>
                    </td>
                    <td colspan="2"><em><liferay-ui:message key="department" />:</em>
                        <c:out value="${positionDescription.department}" />
                    </td>
                    <td><em><liferay-ui:message key="job-location"/>:</em>
                        <c:out value="${positionDescription.jobLocation}"/>
                    </td>
                </tr>
                <tr>
                    <td><em><liferay-ui:message key="employee-name" />:</em>
                        <c:out value="${positionDescription.firstName}" /> <c:out value="${positionDescription.lastName}" />
                    </td>
                    <td><em><liferay-ui:message key="position-number"/>:</em>
                        <c:out value="${positionDescription.positionNumber}"/>
                    </td>
                    <td><em><liferay-ui:message key="appt-percent"/>:</em>
                        <c:out value="${positionDescription.positionApptPercent}"/>
                    </td>
                    <td><em><liferay-ui:message key="appt-basis" />:</em>
                        <c:out value="${positionDescription.appointmentBasis}"/>
                    </td>
                    <td><em><liferay-ui:message key="flsa-status" />:</em>
                        <c:out value="${positionDescription.flsaStatus}"/>
                    </td>
                </tr>
                <tr>
                    <td><em><liferay-ui:message key="employment-category"/>:</em>
                        <c:out value="${positionDescription.employmentCategory}"/>
                    </td>
                    <td><em><liferay-ui:message key="work-schedule"/>:</em>
                        <c:out value="${positionDescription.workSchedule}"/>
                    </td>
                    <td><em><liferay-ui:message key="effective-date"  />:</em>
                        <c:out value="${positionDescription.effectiveDate}"/>
                    </td>
                    <td colspan="2"><em><liferay-ui:message key="last-update-date"  />:</em>
                        <fmt:formatDate value="${positionDescription.lastUpdate}" pattern="MM/dd/yy"/>
                    </td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>

    <fieldset>
        <legend><liferay-ui:message key="position-responsibilities"/></legend>

        <fieldset>
            <legend><liferay-ui:message key="brief-position-desc"/></legend>
            <p class="pass-form-text"><%= CWSUtil.escapeHtml(formPositionDescription.getPositionDescription())%></p>
        </fieldset>

        <fieldset>
            <legend><liferay-ui:message key="position-summary"/></legend>
            <p class="pass-form-text"><%= CWSUtil.escapeHtml(formPositionDescription.getPositionSummary())%></p>

        </fieldset>

        <fieldset>
            <legend><liferay-ui:message key="decision-making"/></legend>
            <p class="pass-form-text"><%= CWSUtil.escapeHtml(formPositionDescription.getDecisionMakingGuidelines())%></p>

        </fieldset>

        <fieldset>
            <legend><liferay-ui:message key="compliance-ncaa-question"/></legend>
            <p class="pass-form-text"><%= CWSUtil.escapeHtml(formPositionDescription.getCommitmentNcaaFsb())%></p>
        </fieldset>

        <fieldset>
            <legend><liferay-ui:message key="lead-work-responsibilities"/></legend>
            <c:forEach var="leadWorkResponsibility" items="${positionDescription.leadWorkResponsibilities}">
                <c:out value="${leadWorkResponsibility.response}"/><br />
            </c:forEach>
        </fieldset>

        <fieldset>
            <legend><liferay-ui:message key="percent-time-lead-work"/></legend>
            <%= CWSUtil.escapeHtml(formPositionDescription.getPercentOfTimeLeadWork())%>
        </fieldset>

        <fieldset>
        <legend><liferay-ui:message key="employee-directly-supervised"/></legend>
            <%= CWSUtil.escapeHtml(formPositionDescription.getNumberOfEmployeesSupervised())%>
        </fieldset>
    </fieldset>

    <fieldset>
        <legend><liferay-ui:message key="position-duties"/></legend>
        <p class="pass-form-text"><%= CWSUtil.escapeHtml(formPositionDescription.getPositionDuties())%></p>

        <p class="pass-form-text"><strong><liferay-ui:message key="position-duties-cont"/></strong><br />
            <c:out value="${positionDescription.positionDutiesCont}"/>
        </p>
    </fieldset>

    <fieldset>
        <legend><liferay-ui:message key="qualifications"/></legend>
        <fieldset>
            <legend><liferay-ui:message key="min-qualifications"/></legend>
            <p class="pass-form-text"><%= CWSUtil.escapeHtml(formPositionDescription.getMinimumQualifications())%></p>
        </fieldset>

        <fieldset>
            <legend><liferay-ui:message key="additional-qualifications"/></legend>
            <p class="pass-form-text"><%= CWSUtil.escapeHtml(formPositionDescription.getAdditionalQualifications())%></p>
        </fieldset>

        <fieldset>
            <legend><liferay-ui:message key="security-sensitive-position-question"/></legend>
            <c:out value="${positionDescription.securitySensitivePosition}"/>
        </fieldset>

        <fieldset>
            <legend><liferay-ui:message key="valid-drivers-license-required"/></legend>
            <c:out value="${positionDescription.validDriverLicenseRequired}"/>
        </fieldset>

        <fieldset>
            <legend><liferay-ui:message key="diversity-initiative"/></legend>
            <p class="pass-form-text"><%= CWSUtil.escapeHtml(formPositionDescription.getDiversityInitiative())%></p>
        </fieldset>

        <fieldset>
            <legend><liferay-ui:message key="working-conditions"/></legend>
            <p class="pass-form-text"><%= CWSUtil.escapeHtml(formPositionDescription.getWorkingConditions())%></p>
        </fieldset>

        <fieldset>
            <legend><liferay-ui:message key="preferred-qualifications"/></legend>
            <p class="pass-form-text"><%= CWSUtil.escapeHtml(formPositionDescription.getPreferredQualifications())%></p>
        </fieldset>
    </fieldset>

</c:if>

<%@ include file="/jsp/footer.jsp" %>