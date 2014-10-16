<%@ include file="/jsp/init.jsp"%>
<c:set var="addAction" value="add"/>
<c:set var="deleteAction" value="delete"/>

<h2><liferay-ui:message key="review-cycle-option-list-title"/></h2>

<ul id="search-parent" class="actions">
    <li id="<portlet:namespace/>reason-add-link">
        <liferay-ui:icon
            image="add_article"
            url="#"
            label="true"
            message="review-cycle-option-add"
        />
    </li>
</ul>



<form action="<portlet:actionURL windowState="<%= WindowState.MAXIMIZED.toString() %>">
    <portlet:param name="action" value="${addAction}"/>
    <portlet:param name="controller" value="ReviewCycleAction"/>
    </portlet:actionURL>" id="<portlet:namespace />fm" name="<portlet:namespace />fm" method="post">
    <fieldset id="pass-user-add">
        <legend><liferay-ui:message key="review-cycle-option-add"/></legend>

        <label><liferay-ui:message key="review-cycle-option-name" /></label>
        <input type="text" name="<portlet:namespace/>name" class="inline narrow" id="<portlet:namespace/>name"/>

        <label><liferay-ui:message key="review-cycle-option-value" /></label>
        <input type="text" name="<portlet:namespace/>value" class="inline narrow" id="<portlet:namespace/>value"/>

        <label><liferay-ui:message key="review-cycle-option-sequence" /></label>
        <input type="text" name="<portlet:namespace/>sequence" class="inline narrow" id="<portlet:namespace/>sequence"/>

        <input type="submit" value="<liferay-ui:message key="save" />" />
        <input type="submit" value="<liferay-ui:message key="cancel" />" id="<portlet:namespace/>cancel"/>

  </fieldset>
</form>

<liferay-ui:success key="review-cycle-option-added" message="review-cycle-option-added" />
<liferay-ui:success key="review-cycle-option-deleted" message="review-cycle-option-deleted" />

<table class="taglib-search-iterator">
    <thead>
        <tr class="portlet-section-header results-header">
            <th><liferay-ui:message key="review-cycle-option-name"/></th>
            <th><liferay-ui:message key="review-cycle-option-value"/></th>
            <th><liferay-ui:message key="review-cycle-option-sequence"/></th>
            <th><liferay-ui:message key="actions"/></th>
        </tr>
    </thead>
    <tbody>
        <c:forEach var="option" items="${reviewCycleOptions}" varStatus="loopStatus">
        <tr class="${loopStatus.index % 2 == 0 ? 'portlet-section-body results-row' : 'portlet-section-alternate results-row alt'}"
            id="<portlet:namespace/>reason-${closeOutReason.id}"
        >
            <td><c:out value="${option.name}"/></td>
            <td><c:out value="${option.value}"/></td>
            <td><c:out value="${option.sequence}"/></td>
            <td>
                <a class="<portlet:namespace/>user-delete" href="<portlet:renderURL
                    windowState="<%= WindowState.MAXIMIZED.toString() %>">
                    <portlet:param name="id" value="${option.id}"/>
                    <portlet:param name="action" value="${deleteAction}"/>
                    <portlet:param name="controller" value="ReviewCycleAction"/>
                </portlet:renderURL>"><liferay-ui:message key="edit"/></a>
                <a class="<portlet:namespace/>user-delete" href="<portlet:renderURL
                    windowState="<%= WindowState.MAXIMIZED.toString() %>">
                    <portlet:param name="id" value="${option.id}"/>
                    <portlet:param name="action" value="${deleteAction}"/>
                    <portlet:param name="controller" value="ReviewCycleAction"/>
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
    if (jQuery("#<portlet:namespace />name").val() == "") {
      errors += "<li><%= bundle.getString("review-cycle-option-validNameRequired") %></li>";
    }
    if (jQuery("#<portlet:namespace />value").val() == "") {
      errors += "<li><%= bundle.getString("review-cycle-option-validValueRequired") %></li>";
    }
    if (jQuery("#<portlet:namespace />sequence").val() == "") {
      errors += "<li><%= bundle.getString("review-cycle-option-validSequenceRequired") %></li>";
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