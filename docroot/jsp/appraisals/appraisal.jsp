<%@ include file="/jsp/init.jsp"%>
<% Appraisal formAppraisal = (Appraisal) request.getAttribute("appraisal"); %>

<jsp:useBean id="appraisal" class="edu.osu.cws.pass.models.Appraisal" scope="request" />
<jsp:useBean id="permissionRule" class="edu.osu.cws.pass.models.PermissionRule" scope="request" />

<div id="pass-appraisal-form">
    <h2><liferay-ui:message key="appraisal-classified-title" /></h2>
    <%@ include file="/jsp/appraisals/info.jsp"%>

    <c:if test="${not empty permissionRule.saveDraft || not empty permissionRule.requireModification || not empty permissionRule.submit}">
    <form class="appraisal" id="<portlet:namespace />fm"
        action="<portlet:actionURL windowState="<%= WindowState.NORMAL.toString() %>">
        <portlet:param name="action" value="updateAppraisal" />
        </portlet:actionURL>" method="post" name="<portlet:namespace />request_form">

        <input type="hidden" name="id" value="${appraisal.id}"/>
    </c:if>

    <div class="appraisal-criteria">
    <c:forEach var="assessment" items="${appraisal.sortedAssessments}" varStatus="loopStatus">
        <%@ include file="/jsp/appraisals/criteria.jsp"%>
    </c:forEach>
    </div>

    <c:choose>
        <c:when test="${permissionRule.goalComments == 'e'}">
            <p><strong><liferay-ui:message key="appraisal-goals-comments" /></strong></p>
            <liferay-ui:input-textarea param="appraisal.goalsComments"
                defaultValue="${appraisal.goalsComments}" /><br />
        </c:when>
        <c:when test="${permissionRule.goalComments == 'v'}">
            <p><strong><liferay-ui:message key="appraisal-goals-comments" /></strong></p>
    <p class="pass-form-text"><%= formAppraisal.getGoalsComments().replaceAll("\n", "<br />") %></p>
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
            <input type="checkbox"  name="<portlet:namespace />sign-appraisal"
                id="<portlet:namespace />sign-appraisal"
                <c:if test="${not empty appraisal.employeeSignedDate}">
                    checked="checked" disabled="disabled"
                </c:if>
            >
            <liferay-ui:message key="appraisal-acknowledge-read"/></input>
        </c:if>
        <c:choose>
            <c:when test="${permissionRule.employeeResponse == 'e'}">
                <p><strong><liferay-ui:message key="appraisal-employee-response" /></strong></p>
                <liferay-ui:input-textarea param="appraisal.employeeResponse"
                    defaultValue="${appraisal.employeeResponse}" /><br />
            </c:when>
            <c:when test="${permissionRule.employeeResponse == 'v'}">
                <p><strong><liferay-ui:message key="appraisal-employee-response" /></strong></p>
        <p class="pass-form-text"><%= formAppraisal.getEmployeeResponse().replaceAll("\n", "<br />") %></p>
            </c:when>
        </c:choose>
    </div>

    <br />
    <div class="pass-actions">
        <c:if test="${not empty permissionRule.saveDraft}">
        <input name="${permissionRule.saveDraft}" type="submit" value="<liferay-ui:message key="${permissionRule.saveDraft}" />">
        </c:if>

        <c:if test="${not empty permissionRule.requireModification}">
        <input name="${permissionRule.requireModification}" type="submit" value="<liferay-ui:message key="${permissionRule.requireModification}" />">
        </c:if>

        <c:if test="${not empty permissionRule.submit}">
        <input name="${permissionRule.submit}" type="submit" value="<liferay-ui:message key="${permissionRule.submit}" />">
        </c:if>

        <c:if test="${not empty permissionRule.saveDraft || not empty permissionRule.requireModification || not empty permissionRule.submit}">
        </form>
    </div><!-- end pass-actions-->

    <script type="text/javascript">
    jQuery(document).ready(function() {
      jQuery("#<portlet:namespace />fm").submit(function() {
        var errors = "";
        if (jQuery("#<portlet:namespace />sign-appraisal").length > 0 &&
                !jQuery("#<portlet:namespace />sign-appraisal").is(':checked')) {
          errors = "<li><%= Appraisal.signatureRequired %></li>";
        }
        if (errors != "") {
          jQuery("#<portlet:namespace />flash").html(
            '<span class="portlet-msg-error"><ul>'+errors+'</ul></span>'
          );
          return false;
        }

        return true;
      });
    });
    </script>
    </c:if>
</div><!-- end appraisal -->
