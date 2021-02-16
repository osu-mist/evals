<%@ include file="/jsp/init.jsp" %>

<portlet:renderURL windowState="<%= WindowState.MAXIMIZED.toString() %>"  var="listNotices" escapeXml="false">
    <portlet:param name="action" value="list"/>
    <portlet:param name="controller" value="NoticeAction"/>
</portlet:renderURL>

<jsp:useBean id="notice" class="edu.osu.cws.evals.models.Notice" scope="request" />


<h2><liferay-ui:message key="notice-edit"/></h2>

<div id="pass-add-criteria">
    <form action="<portlet:actionURL windowState="<%= WindowState.MAXIMIZED.toString() %>">
    <portlet:param name="action" value="edit"/>
    <portlet:param name="controller" value="NoticeAction"/>
    </portlet:actionURL>" id="<portlet:namespace />fm" name="<portlet:namespace />fm" method="post">
    <fieldset>
        <legend><liferay-ui:message key="notices"/></legend>
        <input name="<portlet:namespace />ancestorID" type="hidden" value="${notice.ancestorID}" />
        <input type="hidden" id="<portlet:namespace />name" name="<portlet:namespace />name"
               value="${notice.name}" />

        <label for="<portlet:namespace />text"><liferay-ui:message key="text" /></label>
        <liferay-ui:input-textarea param="text" defaultValue="${notice.text}"/>
    </fieldset>

    <input type="submit" value="<liferay-ui:message key="save" />" />
    <input type="button" class="cancel" value="<liferay-ui:message key="cancel" />"
    onClick="location.href = '<%=renderResponse.encodeURL(listNotices.toString())%>';" />
    </form>
</div>

<%@ include file="/jsp/footer.jsp" %>