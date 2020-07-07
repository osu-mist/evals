<h2 class="secret"><liferay-ui:message key="evals-title"/></h2>
<div class="portlet-top-menu action-menu-wrapper">
    <ul class="portlet-action-menu">
        <li
          <c:if test="${not empty isHome}">
            class="active"
          </c:if>
        ><a href="<portlet:renderURL windowState="<%= WindowState.NORMAL.toString() %>">
                        <portlet:param name="action" value="display"/>
                        <portlet:param name="controller" value="HomeAction"/>
                        </portlet:renderURL>"><liferay-ui:message key="evals-home"/></a></li>
        <li><a href="#"><liferay-ui:message key="settings"/></a>
            <ul>
                <li><a href="<portlet:renderURL windowState="<%= WindowState.NORMAL.toString() %>">
                        <portlet:param name="action" value="displayMyInformation"/>
                        <portlet:param name="controller" value="HomeAction"/>
                        </portlet:renderURL>"><liferay-ui:message key="my-information"/></a></li>
            </ul>
        </li>
        <li class="reports"><a href="#"><liferay-ui:message key="reports"/></a>
            <ul>
                <li><a href="<portlet:renderURL windowState="<%= WindowState.MAXIMIZED.toString() %>">
                        <portlet:param name="action" value="report"/>
                        <portlet:param name="controller" value="ReportsAction"/>
                        </portlet:renderURL>"><liferay-ui:message key="appointment-type-classified"/></a></li>
            </ul>
        </li>

        <c:if test="${isAdmin == 'true' || isReviewer == 'true' || isSupervisor == 'true'}">
        
          <li class="pass-roles"><a href="#">Roles</a>
            <ul>
              <c:if test="${isAdmin == 'true'}">
                  <li><a href="<portlet:renderURL windowState="<%= WindowState.NORMAL.toString() %>">
                      <portlet:param name="action" value="display"/>
                      <portlet:param name="controller" value="HomeAction"/>
                      <portlet:param name="currentRole" value="<%= ActionHelper.ROLE_ADMINISTRATOR %>"/>
                      </portlet:renderURL>"><liferay-ui:message key="role-admin"/></a>
                  </li>
              </c:if>
              <c:if test="${isReviewer == 'true'}">
                  <li><a href="<portlet:renderURL windowState="<%= WindowState.NORMAL.toString() %>">
                      <portlet:param name="action" value="display"/>
                      <portlet:param name="controller" value="HomeAction"/>
                      <portlet:param name="currentRole" value="<%= ActionHelper.ROLE_REVIEWER %>"/>
                      </portlet:renderURL>"> <liferay-ui:message key="role-reviewer"/> </a>
                  </li>
              </c:if>
              <c:if test="${isSupervisor == 'true'}">
                  <li><a href="<portlet:renderURL windowState="<%= WindowState.NORMAL.toString() %>">
                      <portlet:param name="action" value="display"/>
                      <portlet:param name="controller" value="HomeAction"/>
                      <portlet:param name="currentRole" value="<%= ActionHelper.ROLE_SUPERVISOR %>"/>
                          </portlet:renderURL>"><liferay-ui:message key="role-supervisor"/></a>
                  </li>
              </c:if>
                  <li><a href="<portlet:renderURL windowState="<%= WindowState.NORMAL.toString() %>">
                      <portlet:param name="action" value="display"/>
                      <portlet:param name="controller" value="HomeAction"/>
                      <portlet:param name="currentRole" value="<%= ActionHelper.ROLE_SELF %>"/>
                      </portlet:renderURL>"><liferay-ui:message key="role-self"/></a>
                  </li>
            </ul>
          </li>
        </c:if>
    </ul>
</div>
<c:if test="${isAdmin == 'true' || isReviewer == 'true' || isSupervisor == 'true'}">
<div class="role-description">
  <em><liferay-ui:message key="role-view-${currentRole}"/></em> <liferay-ui:message key="role-help-message"/>
</div>
</c:if>
