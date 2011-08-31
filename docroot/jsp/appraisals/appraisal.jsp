<%@ include file="/jsp/init.jsp"%>
<% Appraisal formAppraisal = (Appraisal) request.getAttribute("appraisal"); %>

<jsp:useBean id="appraisal" class="edu.osu.cws.pass.models.Appraisal" scope="request" />
<jsp:useBean id="permissionRule" class="edu.osu.cws.pass.models.PermissionRule" scope="request" />
<portlet:resourceURL var="downloadPDFURL" id="downloadPDF" escapeXml="false">
    <portlet:param name="id" value="${appraisal.id}"/>
</portlet:resourceURL>
<portlet:actionURL var="resendAppraisalToNolij" escapeXml="false">
    <portlet:param name="id" value="${appraisal.id}"/>
    <portlet:param name="action" value="resendAppraisalToNolij"/>
</portlet:actionURL>

<div id="pass-appraisal-form" class="osu-cws">

    <h2><liferay-ui:message key="appraisal-classified-title" /></h2>
    <liferay-ui:success key="draft-saved" message="draft-saved" />
    <liferay-ui:success key="appraisal-sent-to-nolij-success" message="appraisal-sent-to-nolij-success" />

    <ul class="actions">
        <li><liferay-ui:icon
            image="../document_library/pdf"
            url="<%=renderResponse.encodeURL(downloadPDFURL.toString())%>"
            label="true"
            message="appraisal-download-pdf"
        /></li>
        <c:if test="${not empty displayResendNolij}">
            <li><liferay-ui:icon
                image="copy"
                url="<%=renderResponse.encodeURL(resendAppraisalToNolij.toString())%>"
                label="true"
                message="appraisal-resend-to-nolij"
            /></li>
        </c:if>
    </ul>

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
            <fieldset>
                <legend><liferay-ui:message key="appraisal-goals-legend" /></legend>
                <label for="<portlet:namespace />.appraisal.goalsComments"><liferay-ui:message key="appraisal-goals-comments" /></label>
                <liferay-ui:input-textarea param="appraisal.goalsComments"
                    defaultValue="${appraisal.goalsComments}" />
            </fieldset>
        </c:when>
        <c:when test="${permissionRule.goalComments == 'v'}">
            <fieldset>
                <legend><liferay-ui:message key="appraisal-goals-legend" /></legend>
                <p><strong><liferay-ui:message key="appraisal-goals-comments" /></strong></p>
                <p class="pass-form-text"><%= CWSUtil.escapeHtml(formAppraisal.getGoalsComments()) %></p>
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
                <legend><liferay-ui:message key="appraisal-employee-legend" /></legend>
                <fieldset>
                    <legend><liferay-ui:message key="appraisal-employee-signature" /></legend>
                    <input type="checkbox"  name="<portlet:namespace />acknowledge-read-appraisal"
                        id="<portlet:namespace />acknowledge-read-appraisal"
                        <c:if test="${appraisal.status != 'signatureDue' && appraisal.status != 'signatureOverdue'}">
                            checked="checked" disabled="disabled"
                        </c:if>
                    >
                        <liferay-ui:message key="appraisal-acknowledge-checkbox"/>
                    </input>
                    <br />
                    <p><c:if test="${not empty appraisal.employeeSignedDate}">
                            <liferay-ui:message key="appraisal-employee-signed" />
                            ${appraisal.job.employee.name}
                            <fmt:formatDate value="${appraisal.employeeSignedDate}" pattern="MM/dd/yy h:m a"/>
                        </c:if>
                    </p>
                </fieldset>
          </c:if>
            
            <c:choose>
                <c:when test="${permissionRule.employeeResponse == 'e'}">
                    <c:if test="${empty appraisal.rebuttal}">
                        <br />
                        <input type="submit" id="<portlet:namespace />show-rebuttal"
                            value="<liferay-ui:message key="appraisal-want-rebuttal" />" />
                        <div class="pass-hide pass-appraisal-rebuttal">
                    </c:if>
                    <label for="<portlet:namespace />appraisal.rebuttal"><liferay-ui:message key="appraisal-employee-response" /></label>
                    <liferay-ui:input-textarea param="appraisal.rebuttal"
                        defaultValue="${appraisal.rebuttal}" /><br />
                    <c:if test="${empty appraisal.rebuttal}">
                        </div><!-- end pass-hide-->
                    </c:if>
                </c:when>
                <c:when test="${permissionRule.employeeResponse == 'v' && not empty appraisal.rebuttal}">
                    <fieldset>
                        <legend><liferay-ui:message key="appraisal-employee-response" /></legend>
                        <p class="pass-form-text"><%= CWSUtil.escapeHtml(formAppraisal.getRebuttal()) %></p>
                    </fieldset>
                </c:when>
            </c:choose>

            <c:choose>
                <c:when test="${permissionRule.rebuttalRead == 'e'}">
                    <input type="checkbox" id="<portlet:namespace />appraisal.readRebuttal">
                        <liferay-ui:message key="appraisal-supervisor-ack-read-rebuttal" />
                </c:when>
                <c:when test="${permissionRule.rebuttalRead == 'v' and not empty appraisal.supervisorRebuttalRead}">
                    <p><strong><liferay-ui:message key="appraisal-supervisor-rebuttal-read" />
                    ${appraisal.job.supervisor.employee.name} on
                    <fmt:formatDate value="${appraisal.supervisorRebuttalRead}" pattern="MM/dd/yy"/> at
                    <fmt:formatDate value="${appraisal.supervisorRebuttalRead}" pattern="h:m a"/>
                    </strong></p>
                </c:when>
            </c:choose>
        <c:if test="${not empty permissionRule.employeeResponse}">
            </fieldset>
        </c:if>
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
        <input name="${permissionRule.submit}" type="submit" id="<portlet:namespace />${permissionRule.submit}"
        value="<liferay-ui:message key="${permissionRule.submit}" />">
        </c:if>

        <c:if test="${not empty permissionRule.saveDraft || not empty permissionRule.requireModification || not empty permissionRule.submit}">
        </form>
    </div><!-- end pass-actions-->

    <script type="text/javascript">
    jQuery(document).ready(function() {

      // Handle acknowledge appraisal rebuttal read by supervisor
      jQuery("pass-appraisal-rebuttal").hide();

      jQuery("#<portlet:namespace />fm").submit(function() {
        var errors = "";
        if (jQuery("#<portlet:namespace />acknowledge-read-appraisal").length > 0 &&
                !jQuery("#<portlet:namespace />acknowledge-read-appraisal").is(':checked')) {
          errors = "<li><%= Appraisal.signatureRequired %></li>";
          alert("<%= Appraisal.signatureRequired %>");
        }
        if (errors != "") {
          jQuery("#<portlet:namespace />flash").html(
            '<span class="portlet-msg-error"><ul>'+errors+'</ul></span>'
          );
          return false;
        }

        return true;
      });

      // Handle validation of rating
      jQuery("#<portlet:namespace />submit-appraisal").click(function() {
        var errors = "";
        if (jQuery("input[name=submit-appraisal]").length > 0 &&
              jQuery("input[name=<portlet:namespace />appraisal.rating]:checked",
                "#<portlet:namespace />fm").val() == undefined) {
          errors = "<li><%= Appraisal.ratingRequired %></li>";
          alert("<%= Appraisal.ratingRequired %>");
        }

        if (errors != "") {
          jQuery("#<portlet:namespace />flash").html(
            '<span class="portlet-msg-error"><ul>'+errors+'</ul></span>'
          );
          return false;
        }

        return true;
      });


      // Handle rebuttal show/hide
      jQuery("#<portlet:namespace />show-rebuttal").click(function() {
          jQuery("#<portlet:namespace />show-rebuttal").hide();
          jQuery(".pass-appraisal-rebuttal").show();
          return false;
      });
      
      // Using jQuery plugin to expand textareas as you type
      jQuery('textarea').autogrow();
      
    });
    </script>
    </c:if>


    <hr />
    <h3><liferay-ui:message key="demo-settings"/></h3>
    <p><liferay-ui:message key="demo-settings-description"/></p>

    <a href="<portlet:actionURL windowState="<%= WindowState.MAXIMIZED.toString() %>">
        <portlet:param name="id" value="${appraisal.id}" />
        <portlet:param name="action" value="demoResetAppraisal" />
        <portlet:param name="status" value="goalsDue" />
        </portlet:actionURL>">
    <liferay-ui:message key="demo-settings-appraisal-reset-goals-due"/>
    </a><br />

    <a href="<portlet:actionURL windowState="<%= WindowState.MAXIMIZED.toString() %>">
        <portlet:param name="id" value="${appraisal.id}" />
        <portlet:param name="action" value="demoResetAppraisal" />
        <portlet:param name="status" value="resultsDue" />
        </portlet:actionURL>">
    <liferay-ui:message key="demo-settings-appraisal-reset-results-due"/>
    </a><br />

</div><!-- end appraisal -->
<%@ include file="/jsp/footer.jsp" %>
