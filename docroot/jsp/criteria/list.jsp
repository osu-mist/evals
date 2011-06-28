<%@ include file="/jsp/init.jsp" %>

<%
List criteria = (List) renderRequest.getAttribute("criteria");

PortletURL addCriteriaURL = renderResponse.createRenderURL();
addCriteriaURL.setWindowState(WindowState.NORMAL);
addCriteriaURL.setParameter("action", "addCriteria");
%>

<h2><liferay-ui:message key="Criteria" /></h2>
<liferay-ui:success key="criteria-saved" message="criteria-saved" />
<liferay-ui:success key="criteria-saved" message="criteria-deleted" />

<div class="separator"></div>
<table class="taglib-search-iterator">
    <tr class="portlet-section-header results-header">
        <th>Name</th>
        <th>Description</th>
        <th>Actions</th>
    </tr>

<c:forEach var="criterion" items="${criteria}" varStatus="loopStatus">
    <tr class="${loopStatus.index % 2 == 0 ? 'portlet-section-body results-row' : 'portlet-section-alternate results-row alt'}"
        onmouseover="this.className = 'portlet-section-body-hover results-row hover';"
        onmouseout="this.className = '${loopStatus.index % 2 == 0 ? 'portlet-section-body results-row' : 'portlet-section-alternate results-row alt'}';"
        id="<portlet:namespace/>criterion-${criterion.id}"
    >
        <td>${criterion.name}</td>
        <td>${criterion.currentDetail.description}</td>
        <td>
        <a class="<portlet:namespace/>criterion-edit"
            onclick="return <portlet:namespace/>edit(${criterion.id});" href="<portlet:renderURL>
            <portlet:param name="criterionAreaId" value="${criterion.id}"/>
            <portlet:param name="action" value="editCriteria"/>
        </portlet:renderURL>"><liferay-ui:message key="edit"/></a>
        <a class="<portlet:namespace/>criterion-delete"
            onclick="return <portlet:namespace/>delete(${criterion.id}, '${criterion.name}');" href="<portlet:renderURL>
            <portlet:param name="id" value="${criterion.id}"/>
            <portlet:param name="action" value="deleteCriteria"/>
        </portlet:renderURL>"><liferay-ui:message key="delete"/></a></td>
    </tr>
</c:forEach>

<liferay-ui:icon
    image="add_article"
    url="<%= addCriteriaURL.toString() %>"
    label="true"
    message="Add Evaluation Criteria"
/>

</table>

<portlet:resourceURL var="deleteAJAXURL" id="deleteCriteria" escapeXml="false" />
<script type="text/javascript">
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
</script>