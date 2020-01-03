<jsp:useBean id="permissionRule" class="edu.osu.cws.evals.models.PermissionRule" scope="request" />
<portlet:resourceURL var="saveDraftAJAXURL" id="update" escapeXml="false">
    <!--<portlet:param name="foo" value="1234"/>-->
</portlet:resourceURL>

<div>
    <form id="testForm" action=
        "<portlet:actionURL windowState="<%= WindowState.NORMAL.toString() %>">
        <portlet:param name="action" value="updateTest" />
        <portlet:param name="controller" value="AppraisalsAction" />
        </portlet:actionURL>" method="post" name="<portlet:namespace />request_form">
        <input type="submit" class="cancel" value="im a button in test tools"/>
        <input name="${permissionRule.saveDraft}" type="submit" value="<liferay-ui:message key="${permissionRule.saveDraft}" />">
        <input name="${permissionRule.saveDraft}" type="submit" value="<liferay-ui:message key="test button" />">
        <input name="${permissionRule.saveDraft}" type="submit" value="<liferay-ui:message key="${saveDraftAJAXURL}" />">
    </form>
</div>

<script type="text/javascript">
    <%@ include file="/jsp/home/testTools.js"%>
</script>
