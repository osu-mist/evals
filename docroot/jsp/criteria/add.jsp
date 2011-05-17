<%@ include file="/jsp/init.jsp" %>

<jsp:useBean id="criterionArea" class="edu.osu.cws.pass.models.CriterionArea" scope="request" />
<jsp:useBean id="criterionDetail" class="edu.osu.cws.pass.models.CriterionDetail" scope="request" />

<%
List appointmentTypes = (List) renderRequest.getAttribute("appointmentTypes");

PortletURL addCriteriaURL = renderResponse.createActionURL();
addCriteriaURL.setWindowState(WindowState.NORMAL);
addCriteriaURL.setParameter("action", "addCriteria");
%>

<h2>Add an Evaluation Criteria for Classified</h2>

<form action="<%= addCriteriaURL.toString() %>" id="<portlet:namespace />fm" name="<portlet:namespace />fm" method="post">

<input name="<portlet:namespace />criterionAreaId" type="hidden" value="${criterionArea.id}" />

<table class="lfr-table">
    <tr>
        <td>
            <liferay-ui:message key="name" />
        </td>
        <td>
            <input type="text" id="<portlet:namespace />name" name="<portlet:namespace />name" value="${criterionArea.name}" />
        </td>
    </tr>
    <tr>
        <td>
            <liferay-ui:message key="description" />
        </td>
        <td>
            <liferay-ui:input-textarea param="description" defaultValue="${criterionDetail.description}"/>
        </td>
    </tr>
    <tr>
        <td><liferay-ui:message key="appointmentType" />
        <td><select name="<portlet:namespace />appointmentTypeID">
            <c:forEach var="appointmentType" items="${appointmentTypes}">
                <option value="${appointmentType.id}"
                    ${(appointmentType.id == criterionArea.appointmentType.id)? 'selected="selected"': ''}>
                    ${appointmentType.name}</option>

            </c:forEach>
            </select>

    </tr>
</table>
<br />
<input type="submit" value="<liferay-ui:message key="save" />" />
<input type="button" value="<liferay-ui:message key="cancel" />"
    onClick="location.href = '<portlet:renderURL windowState="<%= WindowState.NORMAL.toString() %>" />';" />

</form>

<script type="text/javascript">
jQuery(document).ready(function() {
  jQuery("#<portlet:namespace />fm").submit(function() {
    var errors = "";
    if (jQuery("#<portlet:namespace />name").val() == "") {
      errors = "<li><%= CriterionArea.nameRequired %></li>";
    }
    if (jQuery("#<portlet:namespace />description").val() == "") {
      errors += "<li><%= CriterionDetail.descriptionRequired %></li>"
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