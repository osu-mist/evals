<%@ include file="/jsp/init.jsp"%>
<c:set var="deleteAction" value="deleteAdmin"/>
<c:set var="addAction" value="addAdmin"/>

<h2><liferay-ui:message key="admins-list-title"/></h2>
<liferay-ui:success key="admin-saved" message="admin-saved" />
<liferay-ui:success key="admin-deleted" message="admin-deleted" />

<c:if test="${isMaster == true}">
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

        <form action="<portlet:actionURL windowState="<%= WindowState.MAXIMIZED.toString() %>">
            <portlet:param name="action" value="${addAction}"/>
            </portlet:actionURL>" id="<portlet:namespace />fm" name="<portlet:namespace />fm" method="post">

            <table>
                <tr>
                    <th><liferay-ui:message key="employee-onid" /></th>
                    <td><input type="text" name="<portlet:namespace/>onid" id="<portlet:namespace/>onid"/></td>
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
                    <td><a target="_new" href="http://oregonstate.edu/main/campus-online-directory">
                    Online Directory</a></td>
                    <td>
                        <input type="submit" value="<liferay-ui:message key="save" />" />
                        <input type="submit" value="<liferay-ui:message key="cancel" />"
                            id="<portlet:namespace/>cancel"/>
                    </td>
                </tr>
            </table>
            </form>
    </fieldset>
</c:if>

<div class="separator"></div>
<table class="taglib-search-iterator">
    <tr class="portlet-section-header results-header">
        <th><liferay-ui:message key="name"/></th>
        <th>&nbsp;</th>
        <c:if test="${isMaster == true}">
            <th><liferay-ui:message key="actions"/></th>
        </c:if>
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
        <c:if test="${isMaster == true}">
            <td>
            <a class="<portlet:namespace/>user-delete" href="<portlet:renderURL
                windowState="<%= WindowState.MAXIMIZED.toString() %>">
                <portlet:param name="id" value="${user.id}"/>
                <portlet:param name="action" value="${deleteAction}"/>
            </portlet:renderURL>"><liferay-ui:message key="delete"/></a></td>
        </c:if>
    </tr>
    </c:forEach>
    </table>

<c:if test="${isMaster == true}">
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

      // Validate form submission
      jQuery("#<portlet:namespace />fm").submit(function() {
        var errors = "";
        if (jQuery("#<portlet:namespace />onid").val() == "") {
          errors = "<li><%= Admin.validEmployeeRequired %></li>";
        }
        var isRadioChecked = false;
        jQuery("#<portlet:namespace />fm input[type='radio']").each(function() {
          isRadioChecked = isRadioChecked || jQuery(this).is(':checked');
        });
        if (!isRadioChecked) {
          errors += "<li><%= Admin.isMasterCannotBeEmpty %></li>"
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
</c:if>