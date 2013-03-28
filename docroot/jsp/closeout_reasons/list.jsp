<%@ include file="/jsp/init.jsp"%>
<c:set var="addAction" value="add"/>
<c:set var="deleteAction" value="delete"/>

<h2><liferay-ui:message key="closeout-reason-list-title"/></h2>
<liferay-ui:success key="closeout-reason-added" message="closeout-reason-added" />
<liferay-ui:success key="closeout-reason-deleted" message="closeout-reason-deleted" />

<ul id="search-parent" class="actions">
    <li id="<portlet:namespace/>reason-add-link">
        <liferay-ui:icon
            image="add_article"
            url="#"
            label="true"
            message="closeout-reason-add"
        />
    </li>
</ul>



<form action="<portlet:actionURL windowState="<%= WindowState.MAXIMIZED.toString() %>">
    <portlet:param name="action" value="${addAction}"/>
    <portlet:param name="controller" value="CloseOutAction"/>
    </portlet:actionURL>" id="<portlet:namespace />fm" name="<portlet:namespace />fm" method="post">
    <fieldset id="pass-user-add">
        <legend><liferay-ui:message key="closeout-reason-add"/></legend>
        <label><liferay-ui:message key="closeout-reason" /></label>
        <input type="text" name="<portlet:namespace/>reason" class="inline narrow" id="<portlet:namespace/>reason"/>

        <input type="submit" value="<liferay-ui:message key="save" />" />
        <input type="submit" value="<liferay-ui:message key="cancel" />" id="<portlet:namespace/>cancel"/>

  </fieldset>
</form>


<table class="taglib-search-iterator">
    <thead>
        <tr class="portlet-section-header results-header">
            <th><liferay-ui:message key="closeout-reason"/></th>
            <th><liferay-ui:message key="actions"/></th>
        </tr>
    </thead>
    <tbody>
        <c:forEach var="closeOutReason" items="${reasonsList}" varStatus="loopStatus">
        <tr class="${loopStatus.index % 2 == 0 ? 'portlet-section-body results-row' : 'portlet-section-alternate results-row alt'}"
            id="<portlet:namespace/>reason-${closeOutReason.id}"
        >
            <td><c:out value="${closeOutReason.reason}"/></td>
            <td>
                <a class="<portlet:namespace/>user-delete" href="<portlet:renderURL
                    windowState="<%= WindowState.MAXIMIZED.toString() %>">
                    <portlet:param name="id" value="${closeOutReason.id}"/>
                    <portlet:param name="action" value="${deleteAction}"/>
                    <portlet:param name="controller" value="CloseOutAction"/>
                </portlet:renderURL>"><liferay-ui:message key="delete"/></a>
            </td>
        </tr>
        </c:forEach>
    </tbody>
</table>

<script type="text/javascript">
jQuery(document).ready(function() {
  jQuery("#pass-user-add").hide();

  // When user clicks cancel in add form, hide the form.
  jQuery("#<portlet:namespace/>cancel").click(function() {
    jQuery("#pass-user-add").hide("slow");
    jQuery("#search-parent").show("slow");
    return false;
  });
  // When user clicks cancel in add form, hide the form.
  jQuery("#<portlet:namespace/>reason-add-link").click(function() {
    jQuery("#pass-user-add").show("slow");
    jQuery("#search-parent").hide("slow");
  });

  // Validate form submission
  jQuery("#<portlet:namespace />fm").submit(function() {
    var errors = "";
    if (jQuery("#<portlet:namespace />reason").val() == "") {
      errors = "<li><%= bundle.getString("CloseOutReason-validReasonRequired") %></li>";
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