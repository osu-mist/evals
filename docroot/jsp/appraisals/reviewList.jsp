<%@ include file="/jsp/init.jsp" %>

<h2><liferay-ui:message key="${pageTitle}" /></h2>
<div id="<portlet:namespace/>appraisal-search-link">
    <p><liferay-ui:icon
        image="search"
        url="#"
        label="true"
        message="Search"
    />
    </p>
</div>

<fieldset id="pass-user-add">
    <legend><liferay-ui:message key="search"/></legend>

    <form action="<portlet:actionURL windowState="<%= WindowState.MAXIMIZED.toString() %>">
        <portlet:param name="action" value="searchAppraisals"/>
        </portlet:actionURL>" id="<portlet:namespace />fm" name="<portlet:namespace />fm" method="post">

        <table>
            <tr>
                <th><label for="<portlet:namespace/>osuid"><liferay-ui:message key="osuid"/>
                </label></th>
                <td><input type="text" id="<portlet:namespace/>osuid" name="<portlet:namespace/>osuid" /></td>
            </tr>
            <tr>
                <td>&nbsp;</td>
                <td>
                    <input type="submit" value="<liferay-ui:message key="search" />" />
                    <input type="submit" value="<liferay-ui:message key="cancel" />"
                        id="<portlet:namespace/>cancel"/>
                </td>
            </tr>
        </table>
        </form>
</fieldset>

<c:if test="${!empty appraisals}">
    <div class="separator"></div>
    <table class="taglib-search-iterator">
        <tr class="portlet-section-header results-header">
            <th><liferay-ui:message key="reviewPeriod"/></th>
            <th><liferay-ui:message key="type"/></th>
            <th><liferay-ui:message key="employee"/></th>
            <th><liferay-ui:message key="supervisor"/></th>
            <th><liferay-ui:message key="job-title"/></th>
            <th><liferay-ui:message key="position-no"/></th>
            <th><liferay-ui:message key="orgn-code-desc"/></th>
            <th><liferay-ui:message key="status"/></th>
        </tr>

    <c:forEach var="appraisal" items="${appraisals}" varStatus="loopStatus">
        <tr class="${loopStatus.index % 2 == 0 ? 'portlet-section-body results-row' : 'portlet-section-alternate results-row alt'}"
            onmouseover="this.className = 'portlet-section-body-hover results-row hover';"
            onmouseout="this.className = '${loopStatus.index % 2 == 0 ? 'portlet-section-body results-row' : 'portlet-section-alternate results-row alt'}';"
        >
            <td>${appraisal.reviewPeriod}</td>
            <td><liferay-ui:message key="appraisal-type-${appraisal.type}"/></td>
            <td>${appraisal.job.employee.name}</td>
            <td>${appraisal.job.supervisor.employee.name}</td>
            <td>${appraisal.job.jobTitle}</td>
            <td>${appraisal.job.positionNumber}</td>
            <td>${appraisal.job.orgCodeDescription}</td>
            <td><a href="<portlet:actionURL windowState="<%= WindowState.MAXIMIZED.toString()%>">
                                <portlet:param name="id" value="${appraisal.id}"/>
                                <portlet:param  name="action" value="displayAppraisal"/>
                               </portlet:actionURL>">
               <liferay-ui:message key="${appraisal.status}"/></a></td>
        </tr>
    </c:forEach>
    </table>
</c:if>
<c:if test="${empty appraisals}">
<p><liferay-ui:message key="no-pending-reviews"/></p>
</c:if>

<script type="text/javascript">
jQuery(document).ready(function() {
  jQuery("#pass-user-add").hide();

  // When user clicks cancel in add form, hide the form.
  jQuery("#<portlet:namespace/>cancel").click(function() {
    jQuery("#pass-user-add").hide();
    jQuery("#<portlet:namespace/>appraisal-search-link").show();
    return false;
  });
  // When user clicks cancel in add form, hide the form.
  jQuery("#<portlet:namespace/>appraisal-search-link").click(function() {
    jQuery("#pass-user-add").show();
    jQuery("#<portlet:namespace/>appraisal-search-link").hide();
  });

  // Validate form submission
  jQuery("#<portlet:namespace />fm").submit(function() {
    var errors = "";
    if (jQuery("#<portlet:namespace />osuid").val() == "") {
      errors = "<li>Please enter the employee's OSU ID</li>";
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
