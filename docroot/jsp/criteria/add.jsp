<%@ include file="/jsp/init.jsp" %>

<jsp:useBean id="criterionArea" class="edu.osu.cws.pass.models.CriterionArea" scope="request" />
<jsp:useBean id="criterionDetail" class="edu.osu.cws.pass.models.CriterionDetail" scope="request" />
<c:set var="action" value="addCriteria" scope="request"/>
<c:set var="titleKey" value="criteria-add-classified" scope="request"/>
<c:if test="${criterionArea.id != 0}">
    <c:set var="action" value="editCriteria" scope="request"/>
    <c:set var="titleKey" value="criteria-edit" scope="request"/>
</c:if>

<%
List appointmentTypes = (List) renderRequest.getAttribute("appointmentTypes");
%>

<h2><liferay-ui:message key="${titleKey}"/></h2>

<div id="pass-add-criteria">
    <form action="<portlet:actionURL>
        <portlet:param name="action" value="${action}"/>
        </portlet:actionURL>" id="<portlet:namespace />fm" name="<portlet:namespace />fm" method="post">

    <input name="<portlet:namespace />criterionAreaId" type="hidden" value="${criterionArea.id}" />

    <table class="lfr-table">
        <tr>
            <td>
                <liferay-ui:message key="name" />
            </td>
            <td width="80%">
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
        <c:if test="${action = 'addCriteria'}">
            <tr>
                <td><liferay-ui:message key="appointment-type" />
                <td><select name="<portlet:namespace />appointmentTypeID">
                    <c:forEach var="appointmentType" items="${appointmentTypes}">
                        <option value="${appointmentType.name}"
                            ${(appointmentType.name == criterionArea.appointmentType)? 'selected="selected"': ''}>
                            ${appointmentType.name}</option>

                    </c:forEach>
                    </select>
            </tr>
        </c:if>
        <c:if test="${action = 'editCriteria'}">
            <tr>
                <td colspan="2">
                    <input type="checkbox" id="<portlet:namespace />propagateEdit"
                    name="<portlet:namespace />propagateEdit"/> <label for="<portlet:namespace />propagateEdit">
                    <liferay-ui:message key="criteria-propagate-edit" /></label>
                </td>
            </tr>
        </c:if>


    </table>
    <br />
    <input type="submit" value="<liferay-ui:message key="save" />" />
    <input type="button" value="<liferay-ui:message key="cancel" />"
        onClick="location.href = '<portlet:renderURL windowState="<%= WindowState.NORMAL.toString() %>" />';" />

    </form>
</div>

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