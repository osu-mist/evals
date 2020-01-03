<jsp:useBean id="permissionRule" class="edu.osu.cws.evals.models.PermissionRule" scope="request" />
<portlet:resourceURL var="saveDraftAJAXURL" id="updateTest" escapeXml="false">
    <!--<portlet:param name="action" value="updateTest"/>-->
</portlet:resourceURL>

<div>
    <!--<form id="testForm" action=
        "<portlet:actionURL windowState="<%= WindowState.NORMAL.toString() %>">
        <portlet:param name="action" value="updateTest" />
        <portlet:param name="controller" value="AppraisalsAction" />
        </portlet:actionURL>" method="post" name="<portlet:namespace />request_form">-->
    <form id="testForm">
        <input type="submit" class="cancel" value="im a button in test tools"/>
        <input name="${permissionRule.saveDraft}" type="submit" value="<liferay-ui:message key="${permissionRule.saveDraft}" />">
        <input name="${permissionRule.saveDraft}" type="submit" value="<liferay-ui:message key="test button" />">
        <input name="${permissionRule.saveDraft}" type="submit" value="<liferay-ui:message key="${saveDraftAJAXURL}" />">
    </form>
</div>

<script type="text/javascript">
    <%@ include file="/jsp/home/testTools.js"%>
</script>
