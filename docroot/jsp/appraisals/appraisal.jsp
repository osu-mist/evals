<%@ include file="/jsp/init.jsp"%>
<% Appraisal formAppraisal = (Appraisal) request.getAttribute("appraisal"); %>

<jsp:useBean id="appraisal" class="edu.osu.cws.pass.models.Appraisal" scope="request" />
<jsp:useBean id="permissionRule" class="edu.osu.cws.pass.models.PermissionRule" scope="request" />

<div id="pass-appraisal-form" class="osu-cws">
    <h2><liferay-ui:message key="appraisal-classified-title" /></h2>
    <liferay-ui:success key="draft-saved" message="draft-saved" />

    <c:if test="${showDraftMessage == 'true'}">
        <span class="portlet-msg-alert"><liferay-ui:message key="appraisal-draft-alert"/></span>
    </c:if>

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
            <label for="<portlet:namespace />.appraisal.goalsComments"><liferay-ui:message key="appraisal-goals-comments" /> </label>
            <liferay-ui:input-textarea param="appraisal.goalsComments"
                defaultValue="${appraisal.goalsComments}" /><br />
        </c:when>
        <c:when test="${permissionRule.goalComments == 'v'}">
            <p><strong><liferay-ui:message key="appraisal-goals-comments" /></strong></p>
    <p class="pass-form-text"><%= CWSUtil.escapeHtml(formAppraisal.getGoalsComments()) %></p>
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
            <input type="checkbox"  name="<portlet:namespace />acknowledge-read-appraisal"
                id="<portlet:namespace />acknowledge-read-appraisal"
                <c:if test="${appraisal.status != 'signatureDue' && appraisal.status != 'signatureOverdue'}">
                    checked="checked" disabled="disabled"
                </c:if>
            >
            <liferay-ui:message key="appraisal-acknowledge-read"/></input>

            <br />
            <p><c:if test="${not empty appraisal.employeeSignedDate}">
            <liferay-ui:message key="appraisal-employee-signed" />
            <fmt:formatDate value="${appraisal.employeeSignedDate}" pattern="MM/dd/yy h:m a"/>
            </c:if></p>
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
                <p><strong><liferay-ui:message key="appraisal-employee-response" /></strong></p>
        <p class="pass-form-text"><%= CWSUtil.escapeHtml(formAppraisal.getRebuttal()) %></p>
            </c:when>

        </c:choose>

        <c:choose>
            <c:when test="${permissionRule.rebuttalRead == 'e'}">
                <input type="checkbox" id="<portlet:namespace />appraisal.readRebuttal">
                    <liferay-ui:message key="appraisal-supervisor-ack-read-rebuttal" />
            </c:when>
            <c:when test="${permissionRule.rebuttalRead == 'v'}">
                <p><liferay-ui:message key="appraisal-supervisor-rebuttal-read" />
                <fmt:formatDate value="${appraisal.supervisorRebuttalRead}" pattern="MM/dd/yy h:m a"/>
                </p>
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
