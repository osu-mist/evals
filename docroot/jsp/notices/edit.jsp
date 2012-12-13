<%@ include file="/jsp/init.jsp" %>

<jsp:useBean id="notice" class="edu.osu.cws.evals.models.Notice" scope="request" />


<h2><liferay-ui:message key="notice-edit"/></h2>

<div id="pass-add-criteria">
    <form action="<portlet:actionURL windowState="<%= WindowState.MAXIMIZED.toString() %>">
    <portlet:param name="action" value="edit"/>
    <portlet:param name="controller" value="NoticeAction"/>
    </portlet:actionURL>" id="<portlet:namespace />fm" name="<portlet:namespace />fm" method="post">
    <fieldset>
        <legend>Notice</legend>
        <input name="<portlet:namespace />ancestorID" type="hidden" value="${notice.ancestorID}" />
        <input type="hidden" id="<portlet:namespace />name" name="<portlet:namespace />name"
               value="${notice.name}" />

        <label for="<portlet:namespace />text"><liferay-ui:message key="text" /></label>
        <liferay-ui:input-textarea param="text" defaultValue="${notice.text}"/>
        <c:if test="${action == 'edit'}">
            <input type="checkbox" id="<portlet:namespace />propagateEdit"name="<portlet:namespace />propagateEdit"/>
            <label for="<portlet:namespace />propagateEdit"><liferay-ui:message key="criteria-propagate-edit" /></label>
        </c:if>
    </fieldset>

    <input type="submit" value="<liferay-ui:message key="save" />" />
    <input type="button" class="cancel" value="<liferay-ui:message key="cancel" />"
    onClick="location.href = '<portlet:renderURL windowState="<%= WindowState.MAXIMIZED.toString() %>">
    <portlet:param name="action" value="list"/>
    <portlet:param name="controller" value="NoticeAction"/></portlet:renderURL>';" />
    </form>
</div>

<%@ include file="/jsp/footer.jsp" %>