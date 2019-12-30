<jsp:useBean id="permissionRule" class="edu.osu.cws.evals.models.PermissionRule" scope="request" />

<div>
    <input type="button" class="cancel" value="im a button in test tools"/>
    <input name="${permissionRule.saveDraft}" type="submit" value="<liferay-ui:message key="${permissionRule.saveDraft}" />">
    <input name="${permissionRule.saveDraft}" type="submit" value="test button">

    <liferay-ui:message key="test key" />
</div>
