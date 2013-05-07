<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt_rt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib uri="http://liferay.com/tld/portlet" prefix="liferay-portlet" %>
<%@ taglib uri="http://liferay.com/tld/theme" prefix="liferay-theme" %>
<%@ taglib uri="http://liferay.com/tld/ui" prefix="liferay-ui" %>

<%@ page import="com.liferay.portal.kernel.language.LanguageUtil" %>
<%@ page import="com.liferay.portal.kernel.util.ParamUtil" %>

<%@ page import="javax.portlet.ActionRequest" %>
<%@ page import="javax.portlet.PortletURL" %>
<%@ page import="javax.portlet.WindowState" %>

<%@ page import="edu.osu.cws.evals.models.*" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="edu.osu.cws.util.CWSUtil" %>
<%@ page import="edu.osu.cws.evals.portlet.ActionHelper" %>
<%@ page import="javax.portlet.*" %>

<%@ page import="java.util.List" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.ResourceBundle" %>

<%@ page isELIgnored ="false" %>

<portlet:defineObjects />

<liferay-theme:defineObjects />

<%
String errorMsg = (String) renderRequest.getAttribute("errorMsg");
if (errorMsg != null && !errorMsg.equals("")) {
    errorMsg = "<ul><li>"+StringUtils.replace(errorMsg, "\n", "</li><li>")+"</ul>";
}
PortletContext ctx = portletConfig.getPortletContext();
ResourceBundle bundle = (ResourceBundle)ctx.getAttribute("resourceBundle");
%>

<div class="osu-cws"> <!-- Full wrapper for portlet (closed in footer.jsp) -->
  
<c:if test="${not empty menuHome}">
    <%@ include file="/jsp/menuHome.jsp"%>
</c:if>
<c:if test="${not empty menuMax}">
    <%@ include file="/jsp/menuMax.jsp"%>
</c:if>

<div id="<portlet:namespace />flash">
    <c:if test="${!empty errorMsg}">
    <span class="portlet-msg-error">${errorMsg}</span>
    </c:if>

    <c:if test="${!empty hasNoEvalsAccess}">
    <span class="portlet-msg-error"><liferay-ui:message key="no-pass-access"/></span>
    </c:if>

    <c:if test="${!empty alertMsg and alertMsg == 'true' and !empty homePageNotice.text}">
    <span class="portlet-msg-alert">
    <c:out value = "${homePageNotice.text}"/>
    </span>
    </c:if>
</div>