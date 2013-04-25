<%@ include file="/jsp/init.jsp"%>
<% Appraisal formAppraisal = (Appraisal) renderRequest.getAttribute("appraisal"); %>

<jsp:useBean id="appraisal" class="edu.osu.cws.evals.models.Appraisal" scope="request" />
<jsp:useBean id="permissionRule" class="edu.osu.cws.evals.models.PermissionRule" scope="request" />
<c:set var="showForm" scope="request"
       value="${not empty permissionRule.saveDraft || not empty permissionRule.requireModification || not empty permissionRule.submit}"/>
<portlet:resourceURL var="downloadPDFURL" id="downloadPDF" escapeXml="false">
    <portlet:param name="id" value="${appraisal.id}"/>
    <portlet:param name="controller" value="AppraisalsAction"/>
</portlet:resourceURL>
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

<div id="pass-appraisal-form" class="osu-cws">

<c:if test="${showForm}">
    <span class="portlet-msg-alert">
    <c:out value = "${appraisalNotice.text}"/>

    </span>
</c:if>

    <h2><liferay-ui:message key="appraisal-classified-title" />: <liferay-ui:message key="${appraisal.viewStatus}" /></h2>
    <liferay-ui:success key="draft-saved" message="draft-saved" />
    <liferay-ui:success key="appraisal-sent-to-nolij-success" message="appraisal-sent-to-nolij-success" />
    <liferay-ui:success key="appraisal-set-status-success" message="appraisal-set-status-success" />

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
    </ul>

    <%@ include file="/jsp/appraisals/info.jsp"%>

    <c:if test="${showForm}">
    <form class="appraisal" id="<portlet:namespace />fm"
        action="<portlet:actionURL windowState="<%= WindowState.NORMAL.toString() %>">
        <portlet:param name="action" value="update" />
        <portlet:param name="controller" value="AppraisalsAction" />
        </portlet:actionURL>" method="post" name="<portlet:namespace />request_form">

        <input type="hidden" name="id" value="${appraisal.id}"/>
        <input type="hidden" id="assessmentCount" name="assessmentCount"
               value="<%= appraisal.getCurrentGoalVersion().getSortedAssessments().size()%>"/>
        <input type="hidden" id="assessmentSequence" name="assessmentSequence"
               value="${appraisal.currentGoalVersion.lastSequence}"/>
    </c:if>

    <div class="appraisal-criteria">
    <c:forEach var="assessment" items="${appraisal.currentGoalVersion.sortedAssessments}" varStatus="loopStatus">
        <%@ include file="/jsp/appraisals/assessments.jsp"%>
    </c:forEach>
    </div>

    <c:choose>
        <c:when test="${permissionRule.goalComments == 'e'}">
            <fieldset>
                <h3 class="secret"><liferay-ui:message key="appraisal-goals-legend" /></h3>
                <legend><liferay-ui:message key="appraisal-goals-legend" /></legend>
                <label for="<portlet:namespace />.appraisal.goalsComments"><liferay-ui:message key="appraisal-goals-comments" /></label>
                <liferay-ui:input-textarea param="appraisal.goalsComments"
                    defaultValue="${appraisal.goalsComments}" />
            </fieldset>
        </c:when>
        <c:when test="${permissionRule.goalComments == 'v'}">
            <fieldset>
                <h3 class="secret"><liferay-ui:message key="appraisal-goals-legend" /></h3>
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
                        <div class="pass-appraisal-rebuttal">
                    </c:if>
                    <label for="<portlet:namespace />appraisal.rebuttal"><liferay-ui:message key="appraisal-employee-response" /></label>
                    <liferay-ui:input-textarea param="appraisal.rebuttal"
                        defaultValue="${appraisal.rebuttal}" />
                    <c:if test="${empty appraisal.rebuttal}">
                        </div><!-- end pass-appraisal-rebuttal-->
                    </c:if>
                </c:when>
                <c:when test="${permissionRule.employeeResponse == 'v' && not empty appraisal.rebuttal}">
                    <fieldset>
                        <h4 class="secret"><liferay-ui:message key="appraisal-employee-response" /></h4>
                        <legend><liferay-ui:message key="appraisal-employee-response" /></legend>
                        <p class="pass-form-text"><%= CWSUtil.escapeHtml(formAppraisal.getRebuttal()) %></p>
                    </fieldset>
                </c:when>
            </c:choose>

            <c:choose>
                <c:when test="${permissionRule.rebuttalRead == 'e'}">
                    <input type="checkbox" id="<portlet:namespace />appraisal-readRebuttal">
                        <label for="<portlet:namespace />appraisal-readRebuttal">
                            <liferay-ui:message key="appraisal-supervisor-ack-read-rebuttal" />
                        </label>
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
        <input name="${permissionRule.requireModification}" class="evals-show-confirm"
               type="submit" value="<liferay-ui:message key="${permissionRule.requireModification}" />">
        </c:if>

        <c:if test="${not empty permissionRule.submit}">
        <input name="${permissionRule.submit}" class="evals-show-confirm"
               type="submit" id="<portlet:namespace />${permissionRule.submit}"
        value="<liferay-ui:message key="${permissionRule.submit}" />">
        </c:if>

        <c:if test="${permissionRule.goals == 'e'}">
        <input name="addAssessment"
               type="submit" id="addAssessment"
        value="<liferay-ui:message key="appraisal-assessment-add"/>">
        </c:if>

        <c:if test="${not empty permissionRule.saveDraft || not empty permissionRule.requireModification || not empty permissionRule.submit}">
        </form>
    </div><!-- end pass-actions-->

    <script type="text/javascript">
    jQuery(document).ready(function() {

      // Handle acknowledge appraisal rebuttal read by supervisor
      jQuery(".pass-appraisal-rebuttal").hide();

      jQuery("#<portlet:namespace />fm").submit(function() {
        var errors = "";
        if (jQuery("#<portlet:namespace />acknowledge-read-appraisal").length > 0 &&
                !jQuery("#<portlet:namespace />acknowledge-read-appraisal").is(':checked')) {
          errors = "<li><%= bundle.getString("appraisal-signatureRequired")%></li>";
          alert("<%= bundle.getString("appraisal-signatureRequired") %>");
        }
        if (jQuery("#<portlet:namespace />appraisal-readRebuttal").length > 0 && !jQuery("#<portlet:namespace />appraisal-readRebuttal").is(':checked')) {
          errors = "<li><%= bundle.getString("appraisal-rebuttalReadRequired") %></li>";
          alert("<%= bundle.getString("appraisal-rebuttalReadRequired") %>");
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
          errors = "<li><%= bundle.getString("appraisal-ratingRequired") %></li>";
          alert("<%= bundle.getString("appraisal-ratingRequired") %>");
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
          jQuery('textarea').autogrow();
          return false;
      });
      

      // Using jQuery plugin to expand textareas as you type
      <c:if test="${appraisal.viewStatus != '<%= Appraisal.STATUS_SIGNATURE_DUE%>' && appraisal.viewStatus != '<%= Appraisal.STATUS_SIGNATURE_OVERDUE%>' ||  not empty appraisal.rebuttal}">
        jQuery('textarea').autogrow();
      </c:if>

        // @todo: need to think about accessibility of delete/add assessments.
        // @todo: need to be able to delete newly added assessments

      // Handle deletion of assessments
      function assessmentDelete() {
        // Verify that the user wants to delete the assessment
        var response = confirm('<liferay-ui:message key="appraisal-assessment-delete-confirm"/>');
        if (response) {
          var classes = jQuery(this).attr('class').split(/\s+/);

          // Find the id of the assessment we're deleting
          var deleteFlagSelector = ".appraisal-assessment-deleted-";
          var assessmentSelector = ".appraisal-assessment-";
          jQuery.each(classes, function(index, item) {
            if (item.indexOf("delete.id.") != -1) {
              var assessmentId = item.replace("delete.id.", "");
              deleteFlagSelector += assessmentId;
              assessmentSelector += assessmentId;
            }
          });

          jQuery(deleteFlagSelector).val(1); // set the deleted flag
          jQuery(assessmentSelector).hide('slow'); // hide assessment
        }
        return false;
      }

      jQuery(".assessment-delete").click(function() {
        return assessmentDelete.call(this);
      });

      // Handle add assessments
      jQuery(".osu-cws #addAssessment").click(function() {
        // clone first assessment as a model
        var newAssessment = jQuery('.appraisal-criteria fieldset:last-child').clone(true);
        newAssessment.show(); // last assessment could have been deleted
        var assessmentCount = jQuery('.appraisal-criteria fieldset').size() + 1;

        // legend, fieldset class and h3 for accessibility
        newAssessment.attr('class', 'appraisal-assessment-' + assessmentCount);
        var legendHtml = newAssessment.find('legend').html();
        legendHtml = legendHtml.replace(/#\d+/, '#' + assessmentCount);
        newAssessment.find('legend').html(legendHtml);
        newAssessment.find('h3.secret').html('<liferay-ui:message key="appraisal-assessment-header"/>' + assessmentCount);

        // Update remove link @todo: the jquery handler neds to be added
        var removeLinkClass = newAssessment.find('legend a').attr('class');
        removeLinkClass = removeLinkClass.replace(/\.\d+/, '') + "." + assessmentCount;
        newAssessment.find('legend a').attr('class', removeLinkClass);

        // delete flag hidden input
        var deleteFlagInput = jQuery(newAssessment.find(':input:hidden')[0]);
        var deleteFlagClass = deleteFlagInput.attr('class').replace(/\d+/, '');
        deleteFlagClass += assessmentCount;
        deleteFlagInput.attr('class', deleteFlagClass);
        var deleteFlagName = deleteFlagInput.attr('name').replace(/\.\d+/, '');
        deleteFlagName += "." + assessmentCount;
        deleteFlagInput.attr('name', deleteFlagName);
        deleteFlagInput.val(0);

        // goal label + textarea
        var goalLabelFor = newAssessment.find('label:first').attr('for').replace(/\.\d+/, '');
        goalLabelFor += "." + assessmentCount;
        jQuery(newAssessment.find('label')[0]).attr('for', goalLabelFor);
        var goalTextAreaId = newAssessment.find('textarea').attr('id').replace(/\.\d+/, '');
        goalTextAreaId += "." + assessmentCount;
        newAssessment.find('textarea').attr('id', goalTextAreaId);
        newAssessment.find('textarea').attr('name', goalTextAreaId);
        newAssessment.find('textarea').attr('class', ''); // clear any class inputs

        // assessment criterias checkboxes
        jQuery.each(newAssessment.find(':checkbox'), function(index, element) {
            // The AssessmentCriteria checkboxes have suffixes. In order to make them unique ids in
            // in the form, we're using multiples of assmentCount starting with assessmentCount
            var checkBoxName = jQuery(element).attr('name').replace(/\.\d+/, '');
            checkBoxName += "." + (assessmentCount * (index + 1));
            jQuery(element).attr('name', checkBoxName);
            jQuery(element).attr('id', checkBoxName);
            jQuery(element).removeAttr('checked');
        });

        // assessment criterias labels
        jQuery.each(newAssessment.find('label'), function(index, element) {
            var checkBoxName = jQuery(element).attr('for').replace(/\.\d+/, '');
            checkBoxName += "." + (assessmentCount * index);
            // The first label is the Goals label, the rest should be assessment criterias
            if (index != 0) {
              jQuery(element).attr('for', checkBoxName);
            }
        });

        newAssessment.appendTo('.appraisal-criteria');
        jQuery('.appraisal-assessment-' + assessmentCount + ' textarea').val(''); // clearing this before appending it didn't work

        jQuery('#assessmentCount').val(assessmentCount);
        jQuery('.appraisal-assessment-' + assessmentCount + ' a.assessment-delete').click(function() {
          return assessmentDelete.call(this);
        });

        return false;
      })

    });
    </script>
    </c:if>

<c:if test="${isDemo}">
    <%@ include file="/jsp/appraisals/demoSettings.jsp"%>

</c:if>

</div><!-- end appraisal -->
<%@ include file="/jsp/footer.jsp" %>
