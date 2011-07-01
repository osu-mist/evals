<%@ include file="/jsp/init.jsp"%>
<c:set var="deleteAction" value="deleteReviewer"/>
<c:set var="addAction" value="addReviewer"/>

<%
List criteria = (List) renderRequest.getAttribute("reviewersList");
%>

<h2><liferay-ui:message key="reviewers-list-title"/></h2>
<liferay-ui:success key="reviewer-saved" message="reviewer-saved" />
<liferay-ui:success key="reviewer-deleted" message="reviewer-deleted" />

<c:if test="${isMaster == true}">
    <div id="<portlet:namespace/>reviewer-add-link">
        <p><liferay-ui:icon
            image="add_user"
            url="#"
            label="true"
            message="Add Reviewer"
        />
        </p>
    </div>

    <fieldset id="pass-user-add">
        <legend><liferay-ui:message key="reviewer-add"/></legend>

        <form action="<portlet:actionURL>
            <portlet:param name="action" value="${addAction}"/>
            </portlet:actionURL>" id="<portlet:namespace />fm" name="<portlet:namespace />fm" method="post">

            <table>
                <tr>
                    <th><liferay-ui:message key="employee-onid" /></th>
                    <td><input type="text" name="<portlet:namespace/>onid" id="<portlet:namespace/>onid"/>
                </tr>
                <tr>
                    <th><liferay-ui:message key="business-center"/></th>
                    <td><select name="<portlet:namespace/>businessCenterName">
                        <c:forEach var="bcName" items="${businessCenters}">
                            <option value="${bcName.name}">${bcName.name}</option>
                        </c:forEach>
                        </select>
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
</c:if>

<div class="separator"></div>
<table class="taglib-search-iterator">
    <tr class="portlet-section-header results-header">
        <th><liferay-ui:message key="name"/></th>
        <th><liferay-ui:message key="business-center"/></th>
        <c:if test="${isMaster == true}">
            <th><liferay-ui:message key="actions"/></th>
        </c:if>
    </tr>

    <c:forEach var="user" items="${reviewersList}" varStatus="loopStatus">
    <tr class="${loopStatus.index % 2 == 0 ? 'portlet-section-body results-row' : 'portlet-section-alternate results-row alt'}"
        onmouseover="this.className = 'portlet-section-body-hover results-row hover';"
        onmouseout="this.className = '${loopStatus.index % 2 == 0 ? 'portlet-section-body results-row' : 'portlet-section-alternate results-row alt'}';"
        id="<portlet:namespace/>users-${user.id}"
    >
        <td>${user.employee.name}</td>
        <td>${user.businessCenterName}</td>
        <c:if test="${isMaster == true}">
            <td>
            <a class="<portlet:namespace/>user-delete" href="<portlet:renderURL>
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
        jQuery("#<portlet:namespace/>reviewer-add-link").show();
        return false;
      });
      // When user clicks cancel in add form, hide the form.
      jQuery("#<portlet:namespace/>reviewer-add-link").click(function() {
        jQuery("#pass-user-add").show();
        jQuery("#<portlet:namespace/>reviewer-add-link").hide();
      });

      // Validate form submission
      jQuery("#<portlet:namespace />fm").submit(function() {
        var errors = "";
        if (jQuery("#<portlet:namespace />onid").val() == "") {
          errors = "<li><%= Reviewer.validEmployeeRequired %></li>";
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