<%@ include file="/jsp/init.jsp"%>
<c:set var="deleteAction" value="deleteAdmin"/>
<c:set var="addAction" value="addAdmin"/>

<%
List criteria = (List) renderRequest.getAttribute("adminsList");
%>

<h2><liferay-ui:message key="admins-list-title"/></h2>
<liferay-ui:success key="admin-saved" message="admin-saved" />
<liferay-ui:success key="admin-deleted" message="admin-deleted" />

<div id="<portlet:namespace/>admin-add-link">
    <p><liferay-ui:icon
        image="add_user"
        url="#"
        label="true"
        message="Add Admin User"
    />
    </p>
</div>
<fieldset id="pass-user-add">
    <legend><liferay-ui:message key="admin-add"/></legend>

    <form action="<portlet:actionURL>
        <portlet:param name="action" value="${addAction}"/>
        </portlet:actionURL>" id="<portlet:namespace />fm" name="<portlet:namespace />fm" method="post">

        <table>
            <tr>
                <th><liferay-ui:message key="employee-onid" /></th>
                <td><input type="text" name="<portlet:namespace/>onid" id="<portlet:namespace/>onid"/>
            </tr>
            <tr>
                <td>&nbsp;</td>
                <td><fieldset>
                    <input type="radio" name="<portlet:namespace/>isAdmin" value="1" />
                    <label for="<portlet:namespace/>isAdmin"><liferay-ui:message key="admins-master"/></label>
                    <input type="radio" name="<portlet:namespace/>isAdmin" value="0" />
                    <label for="<portlet:namespace/>isAdmin"><liferay-ui:message key="admins-non-master"/></label>
                    </fieldset>
                 </td>
            </tr>
            <tr>
                <td><a href="http://oregonstate.edu/main/campus-online-directory">Online Directory</a></td>
                <td>
                    <input type="submit" value="<liferay-ui:message key="save" />" />
                    <input type="submit" value="<liferay-ui:message key="cancel" />"
                        id="<portlet:namespace/>cancel"/>
                </td>
            </tr>
        </table>
        </form>
</fieldset>

<div class="separator"></div>
<table class="taglib-search-iterator">
    <tr class="portlet-section-header results-header">
        <th><liferay-ui:message key="name"/></th>
        <th>&nbsp;</th>
        <th><liferay-ui:message key="actions"/></th>
    </tr>

    <c:forEach var="user" items="${adminsList}" varStatus="loopStatus">
    <tr class="${loopStatus.index % 2 == 0 ? 'portlet-section-body results-row' : 'portlet-section-alternate results-row alt'}"
        onmouseover="this.className = 'portlet-section-body-hover results-row hover';"
        onmouseout="this.className = '${loopStatus.index % 2 == 0 ? 'portlet-section-body results-row' : 'portlet-section-alternate results-row alt'}';"
        id="<portlet:namespace/>users-${user.id}"
    >
        <td>${user.employee.name}</td>
        <td>
            <c:if test="${user.isMaster}">
                <liferay-ui:message key="admins-master"/>
            </c:if>
            <c:if test="${not user.isMaster}">
                <liferay-ui:message key="admins-non-master"/>
            </c:if>
        </td>
        <td>
        <a class="<portlet:namespace/>user-delete" href="<portlet:renderURL>
            <portlet:param name="id" value="${user.id}"/>
            <portlet:param name="action" value="${deleteAction}"/>
        </portlet:renderURL>"><liferay-ui:message key="delete"/></a></td>
    </tr>
    </c:forEach>
    </table>

<script type="text/javascript">
  jQuery(document).ready(function() {
    jQuery("#pass-user-add").hide();

    // When user clicks cancel in add form, hide the form.
    jQuery("#<portlet:namespace/>cancel").click(function() {
      jQuery("#pass-user-add").hide();
      jQuery("#<portlet:namespace/>admin-add-link").show();
      return false;
    });
    // When user clicks cancel in add form, hide the form.
    jQuery("#<portlet:namespace/>admin-add-link").click(function() {
      jQuery("#pass-user-add").show();
      jQuery("#<portlet:namespace/>admin-add-link").hide();
    });
  });
</script>