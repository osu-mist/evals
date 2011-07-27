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

<%@ page import="edu.osu.cws.pass.models.*" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>


<%@ page import="java.util.List" %>
<%@ page import="java.util.HashMap" %>

<style>
<%@ include file="/css/pass.css"%>
</style>


<%@ page isELIgnored ="false" %>

<portlet:defineObjects />

<liferay-theme:defineObjects />

<%
String errorMsg = (String) renderRequest.getAttribute("errorMsg");
if (errorMsg != null && !errorMsg.equals("")) {
    errorMsg = "<ul><li>"+StringUtils.replace(errorMsg, "\n", "</li><li>")+"</ul>";
}
%>

<div id="<portlet:namespace />flash">
    <c:if test="${!empty errorMsg}">
    <span class="portlet-msg-error">${errorMsg}</span>
    </c:if>
</div>