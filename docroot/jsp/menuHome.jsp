<div id="pass-top-menu" class="action-menu-wrapper">
    <ul class="portlet-action-menu">
        <li
          <c:if test="${not empty isHome}">
            class="active"
          </c:if>
        ><a href="<portlet:renderURL windowState="<%= WindowState.NORMAL.toString() %>">
                        <portlet:param name="action" value="${homeAction}"/>
                        </portlet:renderURL>"><liferay-ui:message key="home"/></a></li>
        <li><a href="#"><liferay-ui:message key="settings"/></a>
            <ul>
                <li><a href="<portlet:renderURL windowState="<%= WindowState.NORMAL.toString() %>">
                        <portlet:param name="action" value="displayMyInformation"/>
                        </portlet:renderURL>"><liferay-ui:message key="my-information"/></a></li>
            </ul>
        </li>

        <c:if test="${isAdmin == 'true' || isReviewer == 'true' || isSupervisor == 'true'}">
        
          <li class="pass-roles">
            <em>Roles:</em>
            <select name="roles" onchange="if(this.options[this.selectedIndex].value != ''){window.top.location.href=this.options[this.selectedIndex].value}">
              <c:if test="${isAdmin == 'true'}">
                <option value="<portlet:renderURL windowState="<%= WindowState.NORMAL.toString() %>">
                        <portlet:param name="action" value="displayAdminHomeView"/>
                        </portlet:renderURL>"
                        <c:if test="${currentRole == 'administrator'}">selected="selected"</c:if>
                        >
                        <liferay-ui:message key="role-admin"/></option>
              </c:if>
              <c:if test="${isReviewer == 'true'}">
                <option value="<portlet:renderURL windowState="<%= WindowState.NORMAL.toString() %>">
                      <portlet:param name="action" value="displayReviewerHomeView"/>
                      </portlet:renderURL>"
                      <c:if test="${currentRole == 'reviewer'}">selected="selected"</c:if>
                      >
                      <liferay-ui:message key="role-reviewer"/></option>
              </c:if>
              <c:if test="${isSupervisor == 'true'}">
                <option value="<portlet:renderURL windowState="<%= WindowState.NORMAL.toString() %>">
                      <portlet:param name="action" value="displaySupervisorHomeView"/>
                      </portlet:renderURL>"
                      <c:if test="${currentRole == 'supervisor'}">selected="selected"</c:if>
                      >
                      <liferay-ui:message key="role-supervisor"/></option>
              </c:if>
              <option value="<portlet:renderURL windowState="<%= WindowState.NORMAL.toString() %>">
                      <portlet:param name="action" value="displayHomeView"/>
                      </portlet:renderURL>"  
                      <c:if test="${currentRole == 'self'}">selected="selected"</c:if>
                      >
                      <liferay-ui:message key="role-self"/>
              </option>
            </select>
          </li>
        </c:if>
    </ul>
</div>
<c:if test="${isAdmin == 'true' || isReviewer == 'true' || isSupervisor == 'true'}">
<div class="role-description">
  <em><liferay-ui:message key="role-view-${currentRole}"/></em> <liferay-ui:message key="role-help-message"/>
</div>
</c:if>