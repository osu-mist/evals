<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib uri="http://liferay.com/tld/portlet" prefix="liferay-portlet" %>
<%@ taglib uri="http://liferay.com/tld/theme" prefix="liferay-theme" %>
<%@ taglib uri="http://liferay.com/tld/ui" prefix="liferay-ui" %>
<%@ page import="com.liferay.portal.kernel.language.LanguageUtil" %>

<%@ page import="javax.portlet.ActionRequest" %>
<%@ page import="javax.portlet.PortletURL" %>
<%@ page import="javax.portlet.WindowState" %>

<%@ page import="edu.osu.cws.pass.models.*" %>
<%@ page import="edu.osu.cws.pass.util.HibernateUtil" %>

<%@ page import="java.util.List" %>

<%@ page isELIgnored ="false" %>

<portlet:defineObjects />

<liferay-theme:defineObjects />