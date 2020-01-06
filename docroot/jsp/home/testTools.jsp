<jsp:useBean id="permissionRule" class="edu.osu.cws.evals.models.PermissionRule" scope="request" />
<portlet:resourceURL var="saveDraftAJAXURL" id="updateTest" escapeXml="false">
    <portlet:param name="controller" value="TestsAction"/>
</portlet:resourceURL>

<div>
    <form id="testForm">
        <input name="testName" type="submit" value="<liferay-ui:message key="Create Appraisal" />">
    </form>
</div>

<script type="text/javascript">
    <%@ include file="/jsp/home/testTools.js"%>
</script>
