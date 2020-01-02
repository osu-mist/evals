<jsp:useBean id="permissionRule" class="edu.osu.cws.evals.models.PermissionRule" scope="request" />
<portlet:resourceURL var="saveDraftAJAXURL" id="update" escapeXml="false" />

<div>
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
