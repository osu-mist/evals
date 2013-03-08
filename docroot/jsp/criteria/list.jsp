<%@ include file="/jsp/init.jsp" %>

<%
List criteria = (List) renderRequest.getAttribute("criteria");

PortletURL addURL = renderResponse.createRenderURL();
addURL.setWindowState(WindowState.MAXIMIZED);
addURL.setParameter("action", "add");
addURL.setParameter("controller", "CriteriaAreasAction");
%>

<h2><liferay-ui:message key="Criteria" /></h2>
<liferay-ui:success key="criteria-saved" message="criteria-saved" />
<liferay-ui:success key="criteria-deleted" message="criteria-deleted" />

<ul class="actions">
    <li><liferay-ui:icon
        image="add_article"
        url="<%= addURL.toString() %>"
        label="true"
        message="criteria-add-short"
    /></li>
</ul>
<table class="taglib-search-iterator" id="<portlet:namespace/>criteria-list">
    <thead>
        <tr class="portlet-section-header results-header">
            <th>Name</th>
            <th>Description</th>
            <th><liferay-ui:message key="actions"/></th>
        </tr>
    </thead>
    <tbody>
<c:forEach var="criterion" items="${criteria}">
    <tr id="<portlet:namespace/>criterion-${criterion.id}">
        <td>${criterion.name}</td>
        <td>${criterion.description}</td>
        <td>
        <a class="<portlet:namespace/>criterion-edit"
            onclick="return <portlet:namespace/>edit(${criterion.id});" href="<portlet:renderURL
            windowState="<%= WindowState.MAXIMIZED.toString() %>">
            <portlet:param name="criterionAreaId" value="${criterion.id}"/>
            <portlet:param name="action" value="edit"/>
            <portlet:param name="controller" value="CriteriaAreasAction"/>
        </portlet:renderURL>"><liferay-ui:message key="edit"/></a>
        <a class="<portlet:namespace/>criterion-delete"
            onclick="return <portlet:namespace/>delete(${criterion.id}, '${criterion.name}');" href="<portlet:renderURL
            windowState="<%= WindowState.MAXIMIZED.toString() %>">
            <portlet:param name="id" value="${criterion.id}"/>
            <portlet:param name="action" value="delete"/>
            <portlet:param name="controller" value="CriteriaAreasAction"/>
        </portlet:renderURL>"><liferay-ui:message key="delete"/></a></td>
    </tr>
</c:forEach>
    </tbody>

</table>

<portlet:resourceURL var="deleteAJAXURL" id="delete" escapeXml="false" />

<script type="text/javascript">
  // Delete Criteria JS
  function <portlet:namespace/>delete(id, name) {
    var answer = confirm("<liferay-ui:message key="criteria-delete-confirm"/>: " + name + " ?");
    if (answer == false) {
      return false;
    }
    var querystring = {'id': id, 'controller': "CriteriaAreasAction"};
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
<%@ include file="/jsp/footer.jsp" %>