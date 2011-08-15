<%@ include file="/jsp/init.jsp"%>
<c:set var="deleteAction" value="deleteReviewer"/>
<c:set var="addAction" value="addReviewer"/>

<%
List criteria = (List) renderRequest.getAttribute("reviewersList");
%>

<h2><liferay-ui:message key="reviewers-list-title"/></h2>
<liferay-ui:success key="reviewer-saved" message="reviewer-saved" />
<liferay-ui:success key="reviewer-deleted" message="reviewer-deleted" />

<ul class="actions">
    <li id="<portlet:namespace/>reviewer-add-link">
        <liferay-ui:icon
            image="add_user"
            url="#"
            label="true"
            message="Add Reviewer"
        />

    </li>
</ul>


  <form action="<portlet:actionURL windowState="<%= WindowState.MAXIMIZED.toString() %>">
      <portlet:param name="action" value="${addAction}"/>
      </portlet:actionURL>" id="<portlet:namespace />fm" name="<portlet:namespace />fm" method="post">
      <fieldset id="pass-user-add">
          <legend><liferay-ui:message key="reviewer-add"/></legend>
          <label><liferay-ui:message key="employee-onid" /></label>
          <input type="text" class="narrow inline" name="<portlet:namespace/>onid" id="<portlet:namespace/>onid"/>
          <a href="http://oregonstate.edu/main/campus-online-directory">Online Directory</a>

          <label><liferay-ui:message key="business-center"/></label>
              <select name="<portlet:namespace/>businessCenterName">
                  <c:forEach var="bcName" items="${businessCenters}">
                      <option value="${bcName.name}">${bcName.name}</option>
                  </c:forEach>
              </select>

              <input type="submit" value="<liferay-ui:message key="save" />" />
              <input type="submit" class="cancel" value="<liferay-ui:message key="cancel" />"
                  id="<portlet:namespace/>cancel"/>
      </fieldset>
  </form>

<table class="taglib-search-iterator">
    <thead>
        <tr class="portlet-section-header results-header">
            <th><liferay-ui:message key="name"/></th>
            <th><liferay-ui:message key="business-center"/></th>
            <th><liferay-ui:message key="actions"/></th>
        </tr>
    </thead>
    <tbody>
        <c:forEach var="user" items="${reviewersList}" varStatus="loopStatus">
        <tr class="${loopStatus.index % 2 == 0 ? 'portlet-section-body results-row' : 'portlet-section-alternate results-row alt'}"
            id="<portlet:namespace/>users-${user.id}"
        >
            <td>${user.employee.name}</td>
            <td>${user.businessCenterName}</td>
            <td>
            <a class="<portlet:namespace/>user-delete" href="<portlet:renderURL
                windowState="<%= WindowState.MAXIMIZED.toString() %>">
                <portlet:param name="id" value="${user.id}"/>
                <portlet:param name="action" value="${deleteAction}"/>
            </portlet:renderURL>"><liferay-ui:message key="delete"/></a></td>
        </tr>
        </c:forEach>
    </tbody>
</table>

<script type="text/javascript">
jQuery(document).ready(function() {
  jQuery("#pass-user-add").hide();

  // When user clicks ADD in add form, hide the form.<portlet:namespace/>reviewer-add-link
  jQuery("#<portlet:namespace/>reviewer-add-link").click(function() {
    jQuery("#pass-user-add").show("slow");
    jQuery("#<portlet:namespace/>reviewer-add-link").hide("slow");
  });

  // When user clicks CANCEL in add form, hide the form.
  jQuery("#<portlet:namespace/>cancel").click(function() {
    jQuery("#pass-user-add").hide("slow");
    jQuery("#<portlet:namespace/>reviewer-add-link").show("slow");
    return false;
  });

  // Validate form submission
  jQuery("#<portlet:namespace />fm").submit(function() {
    var errors = "";
    if (jQuery("#<portlet:namespace />onid").val() == "") {
      errors = "<li><%= Reviewer.validEmployeeRequired %></li>";
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
<%@ include file="/jsp/footer.jsp" %>