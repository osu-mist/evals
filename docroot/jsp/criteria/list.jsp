<%@ include file="/jsp/init.jsp" %>

<%
List criteria = (List) renderRequest.getAttribute("criteria");

PortletURL addCriteriaURL = renderResponse.createRenderURL();
addCriteriaURL.setWindowState(WindowState.MAXIMIZED);
addCriteriaURL.setParameter("action", "addCriteria");
%>

<h2><liferay-ui:message key="Criteria" /></h2>
<liferay-ui:success key="criteria-saved" message="criteria-saved" />
<liferay-ui:success key="criteria-deleted" message="criteria-deleted" />

<div class="separator"></div>
<div class="actions">
<liferay-ui:icon
    image="add_article"
    url="<%= addCriteriaURL.toString() %>"
    label="true"
    message="Add Evaluation Criteria"
/>
</div>
<table class="taglib-search-iterator" id="<portlet:namespace/>criteria-list">
    <thead>
        <tr class="portlet-section-header results-header">
            <th><!-- place holder for sortable icon --></th>
            <th>Name</th>
            <th>Description</th>
            <th>Actions</th>
        </tr>
    </thead>
    <tbody>
<c:forEach var="criterion" items="${criteria}">
    <tr id="<portlet:namespace/>criterion-${criterion.id}">
        <td>
            <img src="<%=renderRequest.getContextPath()%>/images/sortable.png"
            alt="Click and drag to update sequence" class="pass-pass-sortable-icon"/>
            <input class="<portlet:namespace/>criteria-id" type="hidden" value="${criterion.id}"/>
            <input class="<portlet:namespace/>criteria-sequence" type="hidden" value="${criterion.sequence}"/>
        </td>
        <td>${criterion.name}</td>
        <td>${criterion.currentDetail.description}</td>
        <td>
        <a class="<portlet:namespace/>criterion-edit"
            onclick="return <portlet:namespace/>edit(${criterion.id});" href="<portlet:renderURL
            windowState="<%= WindowState.MAXIMIZED.toString() %>">
            <portlet:param name="criterionAreaId" value="${criterion.id}"/>
            <portlet:param name="action" value="editCriteria"/>
        </portlet:renderURL>"><liferay-ui:message key="edit"/></a>
        <a class="<portlet:namespace/>criterion-delete"
            onclick="return <portlet:namespace/>delete(${criterion.id}, '${criterion.name}');" href="<portlet:renderURL
            windowState="<%= WindowState.MAXIMIZED.toString() %>">
            <portlet:param name="id" value="${criterion.id}"/>
            <portlet:param name="action" value="deleteCriteria"/>
        </portlet:renderURL>"><liferay-ui:message key="delete"/></a></td>
    </tr>
</c:forEach>
    </tbody>

</table>

<portlet:resourceURL var="deleteAJAXURL" id="deleteCriteria" escapeXml="false" />
<portlet:resourceURL var="updateSequenceAJAXURL" id="updateCriteriaSequence" escapeXml="false" />
<script type="text/javascript">
  // Delete Criteria JS
  function <portlet:namespace/>delete(id, name) {
    var answer = confirm("<liferay-ui:message key="criteria-delete-confirm"/>: " + name + " ?");
    if (answer == false) {
      return false;
    }
    var querystring = {'id': id};
    jQuery.ajax({
      type: "POST",
      url: "<%=renderResponse.encodeURL(deleteAJAXURL.toString())%>",
      data: querystring,
      success: function(msg) {
        if (msg == "success") {
          jQuery("#<portlet:namespace/>criterion-" + id).hide();
          jQuery("#<portlet:namespace />flash").html(
            '<span class="portlet-msg-success"><liferay-ui:message key="criteria-deleted"/></span>'
          );
        } else {
          jQuery("#<portlet:namespace />flash").html(
            '<span class="portlet-msg-error"><ul>'+msg+'</ul></span>'
          );
        }
      }
    });

    return false;
  }

  // Update Sequence JS
  jQuery(document).ready(function() {
    // Display our sortable icon - this is hidden initially for the non-JS fallback
    jQuery('.pass-sortable-icon').show();

    // Update the alternate colors in the criteria list table
    updateCriteriaColors();

    // Bind the jQuery UI Sortable object to the values table
    jQuery("#<portlet:namespace/>criteria-list tbody").sortable({
      axis: 'y',
      containment: '#<portlet:namespace/>criteria-list',
      placeholder: 'ui-state-highlight',

      // Prevents the "highlight" tr from collapsing width-wise
      change: function() {
        jQuery(".ui-state-highlight").html("<td colspan='4' width='100%'><!-- placeholder --></td>");
      },

      stop: function() {
        // Update the alternate colors in the criteria list table
        updateCriteriaColors();
      },

      // Posts request to the update_sequence action when order is updated
      update: function(event, ui) {
        var value_id = ui.item.find('.<portlet:namespace/>criteria-id').val();
        var list_item = document.getElementById("<portlet:namespace/>criterion-"+value_id);
        var new_position = parseInt(jQuery('#<portlet:namespace/>criteria-list tbody tr').index(list_item))+1;
        var querystring = {'id': value_id, 'sequence': new_position};

        // Send request to server to update sequence
        jQuery.ajax({
          type: 'POST',
          url: '<%=renderResponse.encodeURL(updateSequenceAJAXURL.toString())%>',
          data: querystring,
          error: function(event) {
            // Display error message on unsuccessful request
            alert("There was a problem updating the sequence")
          },
          success: function(msg) {
            if (msg == "success") {
              // On a successful request, we re-order the updated sequence column numbers
              sequence = 1;
              jQuery.each(jQuery('#<portlet:namespace/>criteria-list tbody tr'), function() {
                jQuery(this).find('<portlet:namespace/>criteria-sequence').html(sequence);
                sequence++;
              });
              jQuery("#<portlet:namespace />flash").html(
                '<span class="portlet-msg-success"><liferay-ui:message key="criteria-sequence-updated"/></span>'
          );
            } else {
              jQuery("#<portlet:namespace />flash").html(
                '<span class="portlet-msg-error"><ul>'+msg+'</ul></span>'
              );
            }
          }
        });
      }
    }).disableSelection();
  });

  // Updates the colors in the criteria list table
  function updateCriteriaColors() {
    jQuery("#<portlet:namespace/>criteria-list tbody tr:odd").attr("class", "results-row portlet-section-body");
    jQuery("#<portlet:namespace/>criteria-list tbody tr:even").attr("class",
      "results-row portlet-section-alternate alt");
  }
</script>
<%@ include file="/jsp/footer.jsp" %>