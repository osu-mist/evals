<%@ page import="edu.osu.cws.evals.portlet.Constants" %>
<%@ include file="/jsp/init.jsp"%>
<% Appraisal formAppraisal = (Appraisal) renderRequest.getAttribute("appraisal"); %>

<jsp:useBean id="appraisal" class="edu.osu.cws.evals.models.Appraisal" scope="request" />
<jsp:useBean id="permissionRule" class="edu.osu.cws.evals.models.PermissionRule" scope="request" />
<c:set var="showForm" scope="request"
       value="${not empty permissionRule.saveDraft || not empty permissionRule.secondarySubmit || not empty permissionRule.submit}"/>
<c:set var="goalCount" value="1"/>
<c:set var="rebuttalType" value="${appraisal.job.appointmentType == 'Professional Faculty' ? 'feedback' : 'rebuttal'}"/>
<c:set var="submitMsg" value="${permissionRule.submit}"/>
<c:if test="${submitMsg == 'read-appraisal-rebuttal' && rebuttalType == 'feedback'}">
    <c:set var="submitMsg" value="read-appraisal-feedback"/>
</c:if>
<portlet:resourceURL var="downloadPDFURL" id="downloadPDF" escapeXml="false">
    <portlet:param name="id" value="${appraisal.id}"/>
    <portlet:param name="controller" value="AppraisalsAction"/>
</portlet:resourceURL>
<portlet:resourceURL var="saveDraftAJAXURL" id="update" escapeXml="false" />
<portlet:resourceURL var="addGoalAJAXURL" id="addAssessment" escapeXml="false" />
<portlet:actionURL var="resendAppraisalToNolij" escapeXml="false">
    <portlet:param name="id" value="${appraisal.id}"/>
    <portlet:param name="action" value="resendAppraisalToNolij"/>
    <portlet:param name="controller" value="AppraisalsAction"/>
</portlet:actionURL>
<portlet:actionURL var="closeAppraisal" escapeXml="false">
    <portlet:param name="id" value="${appraisal.id}"/>
    <portlet:param name="action" value="closeOutAppraisal"/>
    <portlet:param name="controller" value="AppraisalsAction"/>
</portlet:actionURL>
<portlet:actionURL var="setAppraisalStatus" escapeXml="false">
    <portlet:param name="id" value="${appraisal.id}"/>
    <portlet:param name="action" value="setStatusToResultsDue"/>
    <portlet:param name="controller" value="AppraisalsAction"/>
</portlet:actionURL>
<portlet:actionURL var="requestGoalsReactivation" escapeXml="false">
    <portlet:param name="id" value="${appraisal.id}"/>
    <portlet:param name="action" value="requestGoalsReactivation"/>
    <portlet:param name="controller" value="AppraisalsAction"/>
</portlet:actionURL>
<portlet:actionURL var="viewPositionDescription" escapeXml="false">
    <portlet:param name="id" value="${appraisal.id}"/>
    <portlet:param name="action" value="display"/>
    <portlet:param name="controller" value="PositionDescriptionAction"/>
</portlet:actionURL>

<div id="pass-appraisal-form" class="osu-cws">

<c:if test="${showForm and !empty appraisalNotice.text}">
    <span class="portlet-msg-alert">
        <c:out value="${appraisalNotice.text}"/>
    </span>
</c:if>
<c:if test="${!empty profFacultyMsg and appraisal.job.isUnclassified}">
   <span class="portlet-msg-alert evals-prof-faculty-start">
       ${profFacultyMsg}
   </span>
</c:if>

    <h2><c:out value = "${appraisal.job.appointmentType} "/><liferay-ui:message key="appraisal-title" />: <liferay-ui:message key="${appraisal.viewStatus}" /></h2>

    <ul class="actions">
        <c:if test="${not empty displayDownloadPdf}">
            <li> <span class="" >
              <a href="<%=renderResponse.encodeURL(downloadPDFURL.toString())%>" class=" taglib-icon" >
              <img id="rjus_null_null" src="/evals/images/pdf.png" alt=""> <span class="taglib-text " >Download as PDF</span> </a> </span>
            </li>
        </c:if>
        <c:if test="${not empty displayResendNolij}">
            <li><liferay-ui:icon
                image="copy"
                url="<%=renderResponse.encodeURL(resendAppraisalToNolij.toString())%>"
                label="true"
                message="appraisal-resend-to-nolij"
                cssClass="evals-show-confirm"
            /></li>
        </c:if>
        <c:if test="${not empty displayCloseOutAppraisal}">
            <li><liferay-ui:icon
                image="copy"
                url="<%=renderResponse.encodeURL(closeAppraisal.toString())%>"
                label="true"
                message="appraisal-closeout"
            /></li>
        </c:if>
        <c:if test="${not empty displaySetAppraisalStatus}">
            <li><liferay-ui:icon
                image="action_right"
                url="<%=renderResponse.encodeURL(setAppraisalStatus.toString())%>"
                label="true"
                message="appraisal-move-to-results-due"
                cssClass="evals-show-confirm"
                toolTip="appraisal-move-to-results-due"
            /></li>
        </c:if>
        <c:if test="${not empty displayReactivateGoals}">
            <li><liferay-ui:icon
                image="copy"
                url="<%=renderResponse.encodeURL(requestGoalsReactivation.toString())%>"
                label="true"
                message="appraisal-request-goals-reactivation"
                cssClass="evals-show-confirm"
            /></li>
        </c:if>
        <c:if test="${appraisal.isOpen}">
            <li>
                <span><a href="<%=renderResponse.encodeURL(viewPositionDescription.toString())%>" target="_blank">
                <img class="icon" src="/evals/images/common/copy.png" alt="<liferay-ui:message key="view-position-description"/>"></a>
                <a href="<%=renderResponse.encodeURL(viewPositionDescription.toString())%>" target="_blank"><liferay-ui:message key="view-position-description"/></a>
                </span>
            </li>
        </c:if>
    </ul>

    <liferay-ui:success key="draft-saved" message="draft-saved" />
    <liferay-ui:success key="appraisal-goals-reactivation-requested" message="appraisal-goals-reactivation-requested" />
    <liferay-ui:success key="appraisal-sent-to-nolij-success" message="appraisal-sent-to-nolij-success" />
    <liferay-ui:success key="appraisal-set-status-success" message="appraisal-set-status-success" />
    <c:if test="${permissionRule.status == 'goalsReactivationRequested' && permissionRule.role == 'supervisor'}">
        <span class="portlet-msg-alert">
            <liferay-ui:message key="appraisal-goals-reactivation-warning"/>
        </span>
    </c:if>


    <%@ include file="/jsp/appraisals/info.jsp"%>

    <c:if test="${showForm}">
    <form class="appraisal ${appraisal.status}" id="<portlet:namespace />fm"
        action="<portlet:actionURL windowState="<%= WindowState.NORMAL.toString() %>">
        <portlet:param name="action" value="update" />
        <portlet:param name="controller" value="AppraisalsAction" />
        </portlet:actionURL>" method="post" name="<portlet:namespace />request_form">

        <input type="hidden" id="id" name="id" value="${appraisal.id}"/>
        <input type="hidden" id="<portlet:namespace />autosave_timestamp" name="<portlet:namespace />autosave_timestamp"
               value="0"/>
        <input type="hidden" id="assessmentCount" name="assessmentCount"
               value="-1"/> <!-- @todo: this needs to be updated -->
        <input type="hidden" id="assessmentSequence" name="assessmentSequence"
               value="-1"/> <!-- @todo: this needs to be updated -->
    </c:if>

    <div class="appraisal-criteria">
        <fieldset>
            <legend><liferay-ui:message key="appraisal-details"/></legend>
            <c:if test="${not empty appraisal.approvedGoalsVersions}">
                <c:forEach var="goalsVersion" items="${appraisal.approvedGoalsVersions}">
                    <div class="goals-header">
                        <liferay-ui:message key="appraisal-goals-approved-on"/>
                        <fmt:formatDate value="${goalsVersion.goalsApprovedDate}" pattern="MM/dd/yy"/>:
                    </div>
                    <c:forEach var="assessment" items="${goalsVersion.sortedAssessments}">
                        <%@ include file="/jsp/appraisals/assessment.jsp"%>
                    </c:forEach>
                </c:forEach>
            </c:if>

            <c:if test="${not empty appraisal.unapprovedGoalsVersion}">
                <c:if test="${permissionRule.unapprovedGoals == 'e' || permissionRule.unapprovedGoals == 'v'}">
                    <div class="goals-header">
                        <liferay-ui:message key="appraisal-goals-need-approved"/>
                    </div>
                    <c:forEach var="assessment" items="${appraisal.unapprovedGoalsVersion.sortedAssessments}">
                        <%@ include file="/jsp/appraisals/assessment.jsp"%>
                    </c:forEach>
                </c:if>
            </c:if>
        </fieldset>

        <c:if test="${permissionRule.unapprovedGoals == 'e'}">
            <ul class="ul-h-nav">
                <li><a href="#" class="img-txt add" id="addAssessment">
                    <liferay-ui:message key="appraisal-assessment-add"/></a>
                </li>
            </ul>
        </c:if>
    </div>


    <c:choose>
        <c:when test="${permissionRule.goalComments == 'e'}">
            <fieldset>
                <h3 class="secret"><liferay-ui:message key="appraisal-goals-legend" /></h3>
                <legend><liferay-ui:message key="appraisal-goals-legend" /></legend>
                <label for="<portlet:namespace />appraisal.goalsComments"><liferay-ui:message key="appraisal-goals-comments" /></label>
                <liferay-ui:input-textarea param="appraisal.goalsComments"
                    defaultValue="${appraisal.unapprovedGoalsVersion.goalsComments}" />
            </fieldset>
        </c:when>
        <c:when test="${permissionRule.goalComments == 'v'}">
            <fieldset>
                <h3 class="secret"><liferay-ui:message key="appraisal-goals-legend" /></h3>
                <legend><liferay-ui:message key="appraisal-goals-legend" /></legend>
                <p><strong><liferay-ui:message key="appraisal-goals-comments" /></strong></p>
                <p class="pass-form-text"><%= CWSUtil.escapeHtml(formAppraisal.getUnapprovedGoalsVersion().getGoalsComments()) %></p>
            </fieldset>
        </c:when>
    </c:choose>

    <c:if test="${not empty permissionRule.evaluation}">
        <%@ include file="/jsp/appraisals/evaluation.jsp"%>
    </c:if>

    <c:if test="${not empty permissionRule.review}">
        <%@ include file="/jsp/appraisals/review.jsp"%>
    </c:if>

    <div class="pass-employee-response">
        <c:if test="${not empty permissionRule.employeeResponse}">
            <fieldset>
                <h3 class="secret"><liferay-ui:message key="appraisal-employee-legend" /></h3>
                <legend><liferay-ui:message key="appraisal-employee-legend" /></legend>
                <fieldset>
                    <legend><liferay-ui:message key="appraisal-employee-signature" /></legend>
                    <input type="checkbox"  name="<portlet:namespace />acknowledge-read-appraisal"
                        id="<portlet:namespace />acknowledge-read-appraisal"
                        <c:if test="${not empty appraisal.employeeSignedDate}">
                            checked="checked" disabled="disabled"
                        </c:if>
                    />
                    <label for="<portlet:namespace />acknowledge-read-appraisal">
                        <liferay-ui:message key="appraisal-acknowledge-checkbox"/>
                    </label>
                    <br />
                    <p><c:if test="${not empty appraisal.employeeSignedDate}">
                            <liferay-ui:message key="appraisal-signed" />
                            ${appraisal.job.employee.name}
                            <fmt:formatDate value="${appraisal.employeeSignedDate}" pattern="MM/dd/yy h:m a"/>
                        </c:if>
                    </p>
                </fieldset>
          </c:if>


            <c:choose>
                <c:when test="${permissionRule.employeeResponse == 'e'}">
                    <c:if test="${empty appraisal.rebuttal}">
                            <input type="submit" id="<portlet:namespace />show-rebuttal"
                                value="<liferay-ui:message key="appraisal-want-${rebuttalType}"/>"/>
                            <div class="pass-appraisal-rebuttal">
                    </c:if>
                    <label for="<portlet:namespace />appraisal.rebuttal">
                        <liferay-ui:message key="appraisal-employee-response-${rebuttalType}"/>
                    </label>
                    <liferay-ui:input-textarea param="appraisal.rebuttal" defaultValue="${appraisal.rebuttal}"/>
                    <c:if test="${empty appraisal.rebuttal}">
                        </div><!-- end pass-appraisal-rebuttal-->
                    </c:if>
                </c:when>
                <c:when test="${permissionRule.employeeResponse == 'v' && not empty appraisal.rebuttal}">
                    <fieldset>
                        <h4 class="secret"><liferay-ui:message key="appraisal-employee-response-${rebuttalType}" /></h4>
                        <legend><liferay-ui:message key="appraisal-employee-response-${rebuttalType}" /></legend>
                        <p class="pass-form-text"><%= CWSUtil.escapeHtml(formAppraisal.getRebuttal()) %></p>
                    </fieldset>
                </c:when>
            </c:choose>

            <c:choose>
                <c:when test="${permissionRule.rebuttalRead == 'e'}">
                    <input type="checkbox" id="<portlet:namespace />appraisal-readRebuttal">
                        <label for="<portlet:namespace />appraisal-readRebuttal">
                            <liferay-ui:message key="appraisal-supervisor-ack-read-${rebuttalType}" />
                        </label>
                </c:when>
                <c:when test="${permissionRule.rebuttalRead == 'v' and not empty appraisal.supervisorRebuttalRead}">
                    <p><strong><liferay-ui:message key="appraisal-supervisor-${rebuttalType}-read" />
                        <c:if test="${not empty appraisal.evaluator}">
                            ${appraisal.evaluator.name}
                        </c:if>
                        on
                    <fmt:formatDate value="${appraisal.supervisorRebuttalRead}" pattern="MM/dd/yy"/> at
                    <fmt:formatDate value="${appraisal.supervisorRebuttalRead}" pattern="h:m a"/>
                    </strong></p>
                </c:when>
            </c:choose>
        <c:if test="${not empty permissionRule.employeeResponse}">
            </fieldset>
        </c:if>
    </div>

    <div class="pass-actions">
        <c:if test="${not empty permissionRule.saveDraft}">
        <input name="${permissionRule.saveDraft}" type="submit" value="<liferay-ui:message key="${permissionRule.saveDraft}" />">
        </c:if>

        <c:if test="${not empty permissionRule.secondarySubmit}">
        <input name="${permissionRule.secondarySubmit}" class="evals-show-confirm"
               type="submit" value="<liferay-ui:message key="${permissionRule.secondarySubmit}" />">
        </c:if>

        <c:if test="${not empty permissionRule.submit}">
        <input name="${permissionRule.submit}" class="evals-show-confirm"
               type="submit" id="<portlet:namespace />${permissionRule.submit}"
        value="<liferay-ui:message key="${submitMsg}"/>">
        </c:if>

        <c:if test="${not empty permissionRule.saveDraft || not empty permissionRule.secondarySubmit || not empty permissionRule.submit}">
        </form>
    </div><!-- end pass-actions-->

    <script type="text/javascript">
        <%@ include file="/jsp/appraisals/appraisal.js"%>
    </script>
    </c:if>

<c:if test="${isDemo}">
    <%@ include file="/jsp/appraisals/demoSettings.jsp"%>

</c:if>

</div><!-- end appraisal -->
<%@ include file="/jsp/footer.jsp" %>
