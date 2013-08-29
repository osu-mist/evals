<%@ page import="edu.osu.cws.evals.portlet.Constants" %>
<%@ include file="/jsp/init.jsp"%>
<% Appraisal formAppraisal = (Appraisal) renderRequest.getAttribute("appraisal"); %>

<jsp:useBean id="appraisal" class="edu.osu.cws.evals.models.Appraisal" scope="request" />
<jsp:useBean id="permissionRule" class="edu.osu.cws.evals.models.PermissionRule" scope="request" />
<c:set var="showForm" scope="request"
       value="${not empty permissionRule.saveDraft || not empty permissionRule.secondarySubmit || not empty permissionRule.submit}"/>
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
<portlet:actionURL var="requestGoalsReactivation" escapeXml="false">
    <portlet:param name="id" value="${appraisal.id}"/>
    <portlet:param name="action" value="requestGoalsReactivation"/>
    <portlet:param name="controller" value="AppraisalsAction"/>
</portlet:actionURL>


<div id="pass-appraisal-form" class="osu-cws">

<c:if test="${showForm and !empty appraisalNotice.text}">
    <span class="portlet-msg-alert">
    <c:out value = "${appraisalNotice.text}"/>

    </span>
</c:if>

    <h2><c:out value = "${appraisal.job.appointmentType} "/><liferay-ui:message key="appraisal-title" />: <liferay-ui:message key="${appraisal.viewStatus}" /></h2>
    <liferay-ui:success key="draft-saved" message="draft-saved" />
    <liferay-ui:success key="appraisal-sent-to-nolij-success" message="appraisal-sent-to-nolij-success" />
    <liferay-ui:success key="appraisal-set-status-success" message="appraisal-set-status-success" />

    <ul class="actions">
        <c:if test="${not empty displayDownloadPdf}">
            <li><liferay-ui:icon
                image="../document_library/pdf"
                url="<%=renderResponse.encodeURL(downloadPDFURL.toString())%>"
                label="true"
                message="appraisal-download-pdf"
            /></li>
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
        <fieldset>
            <legend><liferay-ui:message key="appraisal-details"/></legend>
            <c:if test="${not empty appraisal.approvedGoalsVersions}">
                <c:forEach var="goalsVersion" items="${appraisal.approvedGoalsVersions}" varStatus="loopStatus">
                    <c:out value = "Goals approved on ${goalsVersion.approvedDate}:"/>
                    <c:forEach var="assessment" items="${goalsVersion.sortedAssessments}" varStatus="loopStatus">
                        <%@ include file="/jsp/appraisals/assessments.jsp"%>
                    </c:forEach>
                </c:forEach>
            </c:if>

            <c:if test="${not empty appraisal.unapprovedGoalsVersion}">
                <c:if test="${permissionRule.unapprovedGoals == 'e' || permissionRule.unapprovedGoals == 'v'}">
                    <c:out value = "Goals to be approved:"/>
                    <c:forEach var="assessment" items="${appraisal.unapprovedGoalsVersion.sortedAssessments}" varStatus="loopStatus">
                        <%@ include file="/jsp/appraisals/assessments.jsp"%>
                    </c:forEach>
                    <c:if test="${permissionRule.unapprovedGoals == 'e'}">
                        <ul class="ul-h-nav">
                            <li><a href="#" class="img-txt add" id="addAssessment">
                                <liferay-ui:message key="appraisal-assessment-add"/></a>
                            </li>
                        </ul>
                    </c:if>
                </c:if>
            </c:if>
        </fieldset>
    </div>

    <c:if test="${permissionRule.approvedGoals == 'e'}">
        <ul class="ul-h-nav">
            <li><a href="#" class="img-txt add" id="addAssessment">
                <liferay-ui:message key="appraisal-assessment-add"/></a>
            </li>
        </ul>
    </c:if>

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

        <c:if test="${not empty permissionRule.secondarySubmit}">
        <input name="${permissionRule.secondarySubmit}" class="evals-show-confirm"
               type="submit" value="<liferay-ui:message key="${permissionRule.secondarySubmit}" />">
        </c:if>

        <c:if test="${not empty permissionRule.submit}">
        <input name="${permissionRule.submit}" class="evals-show-confirm"
               type="submit" id="<portlet:namespace />${permissionRule.submit}"
        value="<liferay-ui:message key="${permissionRule.submit}" />">
        </c:if>

        <c:if test="${not empty permissionRule.saveDraft || not empty permissionRule.secondarySubmit || not empty permissionRule.submit}">
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

      // Handles validation
      jQuery("#<portlet:namespace />submit-appraisal").click(function(event) {

        //Supervisor Results and Overall Evaluation cannot be empty when submitting appraisal
        var emptyResults = areResultsEmpty();
        if(emptyResults){
            alert('<liferay-ui:message key="appraisal-supervisor-empty-results"/>');
            event.isDefaultPrevented = true;
            return false;
        }

        var errors = "";
        if (jQuery("input[name=submit-appraisal]").length > 0 &&
              jQuery("input[name=<portlet:namespace />appraisal.rating]:checked",
                "#<portlet:namespace />fm").val() == undefined) {
          errors = "<li><%= bundle.getString("appraisal-ratingRequired") %></li>";
          alert("<%= bundle.getString("appraisal-ratingRequired") %>");
          event.isDefaultPrevented = true;
        }

        // add validation specific to salary increase for IT
        <c:if test="${appraisal.job.appointmentType == 'Classified IT'}">
            if(jQuery(".osu-cws input.recommended-salary").val() == '' ||
                 jQuery(".osu-cws input.recommended-salary").val() == '0.0'){
                alert('<liferay-ui:message key="appraisal-salary-increase-required"/>');
                event.isDefaultPrevented = true;
            }
            var increaseValidationError = validateIncrease();
            if (validateIncrease() != '') {
                errors += '<li>' + increaseValidationError + '</li>';
            }
        </c:if>

        if (errors != "") {
          jQuery("#<portlet:namespace />flash").html(
            '<span class="portlet-msg-error"><ul>'+errors+'</ul></span>'
          );
          return false;
        }

        return true;
      });

            //@todo: add erors to the top and scroll to it.
      // Handle validation of assessments' goals
      jQuery("#<portlet:namespace />submit-goals, #<portlet:namespace />approve-goals").click(function(event) {
         // remove any previous goal errors
        jQuery('.appraisal-criteria fieldset>div').each(function(index, element) {
          removeJSError.call(element);
        })

        if (!validateGoals()) {
          window.readyToSubmit = false;
            //set property so that we can check in other handlers if we can abort
          event.isDefaultPrevented = true;

          // set error message at the top and scroll to the top of eval form
          jQuery("#<portlet:namespace />flash").html(
            '<span class="portlet-msg-error"><ul><liferay-ui:message key="appraisal-assessment-goalErrors" /></ul></span>'
          );
          location.href="#evals-flash";

          return false;
        }

        // remove empty assessments because it's much easier to do it via js than in java
        deleteEmptyAssessments();
        window.readyToSubmit = true;

        return true;
      });

      //Employee Results cannot be empty when submitting results
      jQuery("#<portlet:namespace />submit-results").click(function(event) {
          var emptyResults = areResultsEmpty();
          if(emptyResults){
              alert('<liferay-ui:message key="appraisal-employee-empty-results"/>');
              event.isDefaultPrevented = true;
              return false;
          }
      });

      //Supervisor Results and Overall Evaluation cannot be empty when releasing appraisal
      jQuery("#<portlet:namespace />release-appraisal").click(function(event) {
          var emptyResults = areResultsEmpty();
          if(emptyResults){
              alert('<liferay-ui:message key="appraisal-supervisor-empty-results"/>');
              event.isDefaultPrevented = true;
              return false;
          }
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

      /**
       * Sets the goal delete flag for an assessment and hides the assement from the form.
       * This method is called after the user confirms the deletion by clicking the delete button
       * and internally to remove empty assessments before submitting the form.
       */
      function setGoalDeleteFlag() {
        var allAssessments = jQuery('.appraisal-criteria>fieldset').children().length - 1;
        var deletedAssessments = jQuery('.appraisal-criteria>fieldset>div:hidden').length;
        if((allAssessments - deletedAssessments) == 1) {
            alert('<liferay-ui:message key="appraisal-assessment-delete-all"/>');
            return false;
        }
        var classes = jQuery(this).attr('class').split(/\s+/);
        var deleteFlagSelector = ".appraisal-assessment-deleted-";
        var assessmentSelector = ".appraisal-assessment-";

        // Find the id of the assessment we're deleting
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

      /**
       * Handles deletion of assessments. The html coressponding to the assessments is not removed
       * from the DOM. Instead the assessment is hidden and the hidden input with class:
       * appraisal-assessment-deleted-NUM where NUM is the id of the assessment is set to 1 to let the
       * java code know that this assesment has been deleted. A confirmation js pop-up is displayed to the
       * user to let them know that they are about to delete an assessment. This method returns false to
       * prevent the anchor link from being followed since this is just a js action.
       *
       * @return {Boolean}
       */
      function assessmentDelete() {
        // Verify that the user wants to delete the assessment
        var response = confirm("<liferay-ui:message key="appraisal-assessment-delete-confirm"/>");
        if (response) {
          setGoalDeleteFlag.call(this);
        }
        return false;
      }

      jQuery(".assessment-delete").click(function() {
        return assessmentDelete.call(this);
      });

      /**
       * Handles adding a new assessment to the DOM. This js method clones the last .appraisal-criteria
       * fieldset in the form. The logic in this function is to update the various classes, names and ids
       * of the various html elements in the .appraisal-criteria fieldset. The various labels, inputs,
       * textareas and checkboxes in an assessment have in the html classes/name/id information about what
       * assessment the property belongs to. This is so that the java side knows what assessment a goal is
       * associated to and what assessment the assessment criteria are associated to.
       *
       * We chose to use js to clone & add an assessment because we didn't find an easy what for an ajax
       * serveResource call to return html from the assessment.jsp. The ajax serveResource calls can return
       * json, but we couldn't figure out how to get the serveResource method to parse a single jsp file.
       */
      jQuery(".osu-cws #addAssessment").click(function() {
        // clone last assessment in the form for modification
        var newAssessment = jQuery('.appraisal-criteria fieldset>div:last-child').clone(true);
        newAssessment.show(); // last assessment could have been deleted
        var assessmentCount = jQuery('.appraisal-criteria fieldset>div').size() + 1;

        // The rest of this function takes care of updating ids, names and classes

        // h3 for accessibility
        newAssessment.attr('class', 'appraisal-assessment-' + assessmentCount);
        newAssessment.find('h3.secret').html('<liferay-ui:message key="appraisal-assessment-header"/>' + assessmentCount);

        // Delete Assessment Link
        var removeLinkClass = newAssessment.find('a.delete').attr('class');
        removeLinkClass = removeLinkClass.replace(/\.\d+/, '') + "." + assessmentCount;
        newAssessment.find('a.delete').attr('class', removeLinkClass);

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
        var goalLabelVal = newAssessment.find('label:first').html().replace(/\d+/, '');
        goalLabelVal += + assessmentCount;
        jQuery(newAssessment.find('label')[0]).html(goalLabelVal);
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

        newAssessment.appendTo('.appraisal-criteria>fieldset'); // add new assessment to form
        jQuery('.appraisal-assessment-' + assessmentCount + ' textarea').val(''); // clearing this before appending it didn't work

        // update # of assessment in form
        jQuery('#assessmentCount').val(assessmentCount);

        return false;
      });

      /**
       * Whether or not the goals textarea is empty in an assessment.
       *
       * @return {Boolean}
       */
      function areGoalsEmpty() {
          return jQuery(this).find('textarea').val().trim() == "";
      }

      /**
       * Whether or not all criteria assessment checkboxes in an assessment are unchecked.
       *
       * @return {Boolean}
       */
      function areGoalsCheckboxesEmpty() {
          return jQuery(this).find('input:checkbox:checked').size() == 0;
      }

      /**
      * Whether or not there is at least one empty result.
      *
      * @return {Boolean}
      */
      function areResultsEmpty() {
          var isEmpty = jQuery('.lfr-textarea').filter(function(){
              return jQuery.trim(this.value) == '';
          });
          return isEmpty.length > 0;
      }

      /**
       * Whether or not a given assessment has been deleted by the user via js.
       *
       * @return {Boolean}
       */
      function isAssessmentDeleted() {
        // find the delete flag hidden input
        var deleteFlag = jQuery(this).find('input:hidden').filter(function(index) {
            return jQuery(this).attr('class').indexOf('appraisal-assessment-deleted-') == 0;
        });
        return deleteFlag.val() != 0
      }

      /**
       * Removes a goal js error if set.
       */
      function removeJSError() {
        jQuery(this).find('.js-error').remove();
      }

      /**
       * Validates the goals. It checks:
       * 1) at least Constants.MIN_REQUIRED_ASSESSMENTS of non-empty goals are provided in form
       * 2) User doesn't have empty goals with criteria assessments checked
       * 3) User doesn't have criteria assessments completely unchecked with entered goals.
       * @return {Boolean}
       */
      function validateGoals() {
        // get a list of non empty and not-deleted assessments
        var nonEmptyAssessments = jQuery('.appraisal-criteria fieldset>div').filter(function(index) {
          if (isAssessmentDeleted.call(this)) { // filter out deleted assessments
            return false;
          }

          return !areGoalsEmpty.call(this);
        }).size();

        // specify validation error of too low # of assessments
        if (nonEmptyAssessments < <%= Constants.MIN_REQUIRED_ASSESSMENTS %>) {
          alert('<liferay-ui:message key="appraisal-assessment-min" /> <%= Constants.MIN_REQUIRED_ASSESSMENTS %>');
          return false;
        }

        // validate each assessment to verify that we don't have non-empty goals with no criteria checked or vice-versa
        var validGoals = true;
        jQuery('.appraisal-criteria fieldset>div').filter(function(index) {
          if (isAssessmentDeleted.call(this)) { // filter out deleted assessments
            return false;
          }

          var emptyGoals = areGoalsEmpty.call(this);
          var emptyCheckboxes = areGoalsCheckboxesEmpty.call(this);

          return !emptyGoals && emptyCheckboxes;
        }).each(function(index, element) {
          validGoals = false;
          // add error message
          jQuery(element).append('<span class="js-error"><liferay-ui:message key="appraisal-assessment-validation" /></span>');
        });

        return validGoals;
      }

      /**
       * Deletes any empty assessments in the form. This function is called right before the form is
       * submitted. We clear out the deleted assessments because it's easier to do it via js
       */
      function deleteEmptyAssessments() {
        jQuery('.appraisal-criteria fieldset>div').filter(function(index) {
          if (isAssessmentDeleted.call(this)) { // filter out deleted assessments
            return false;
          }

          // filter out assessments with empty goals (ignore criteria checkboxes)
          return areGoalsEmpty.call(this);
        }).each(function(index, element) {
          // delete the empty assessment so it won't be saved.
          var deleteLink = jQuery(element).find('.assessment-delete');
          setGoalDeleteFlag.call(deleteLink);
        });

      }
    });

    </script>
    </c:if>

<c:if test="${isDemo}">
    <%@ include file="/jsp/appraisals/demoSettings.jsp"%>

</c:if>

</div><!-- end appraisal -->
<%@ include file="/jsp/footer.jsp" %>
