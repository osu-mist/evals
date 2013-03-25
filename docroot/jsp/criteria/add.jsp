<%@ include file="/jsp/init.jsp" %>

<jsp:useBean id="criterionArea" class="edu.osu.cws.evals.models.CriterionArea" scope="request" />

<c:set var="action" value="add" scope="request"/>
<c:set var="titleKey" value="criteria-add-classified" scope="request"/>
<c:if test="${criterionArea.id != 0}">
    <c:set var="action" value="edit" scope="request"/>
    <c:set var="titleKey" value="criteria-edit" scope="request"/>
</c:if>

<%
List appointmentTypes = (List) renderRequest.getAttribute("appointmentTypes");
%>

<h2><liferay-ui:message key="${titleKey}"/></h2>

<div id="pass-add-criteria">
    <form action="<portlet:actionURL windowState="<%= WindowState.MAXIMIZED.toString() %>">
        <portlet:param name="action" value="${action}"/>
        <portlet:param name="controller" value="CriteriaAreasAction"/>
        </portlet:actionURL>" id="<portlet:namespace />fm" name="<portlet:namespace />fm" method="post">
        <fieldset>
            <legend><liferay-ui:message key="Criteria" /></legend>
          
            <input name="<portlet:namespace />criterionAreaId" type="hidden" value="${criterionArea.id}" />

            <label for="<portlet:namespace />name"><liferay-ui:message key="name" /></label>
            <input type="text" id="<portlet:namespace />name" name="<portlet:namespace />name" value="${criterionArea.name}" />
           
            <label for="<portlet:namespace />description"><liferay-ui:message key="description" /></label>
            <liferay-ui:input-textarea param="description" defaultValue="${criterionArea.description}"/>
    
            <c:if test="${action == 'add'}">
              <label for="<portlet:namespace />appointmentTypeID"><liferay-ui:message key="appointment-type" /></label>
              <select name="<portlet:namespace />appointmentTypeID">
                <c:forEach var="appointmentType" items="${appointmentTypes}">
                  <option value="${appointmentType.name}"
                    ${(appointmentType.name == criterionArea.appointmentType)? 'selected="selected"': ''}>
                    ${appointmentType.name}
                  </option>
                </c:forEach>
              </select>
            </c:if>
    
            <c:if test="${action == 'edit'}">
              <input type="checkbox" id="<portlet:namespace />propagateEdit"name="<portlet:namespace />propagateEdit"/> 
              <label for="<portlet:namespace />propagateEdit"><liferay-ui:message key="criteria-propagate-edit" /></label>
            </c:if>
        </fieldset>
        
        <input type="submit" value="<liferay-ui:message key="save" />" />
        <input type="button" class="cancel" value="<liferay-ui:message key="cancel" />"
            onClick="location.href = '<portlet:renderURL windowState="<%= WindowState.MAXIMIZED.toString() %>">
            <portlet:param name="action" value="list"/>
            <portlet:param name="controller" value="CriteriaAreasAction"/></portlet:renderURL>';" />
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
      errors += "<li><%= CriterionArea.descriptionRequired %></li>"
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