<jsp:useBean id="testAppraisals" class="java.util.ArrayList" scope="request" />

<jsp:useBean id="permissionRule" class="edu.osu.cws.evals.models.PermissionRule" scope="request" />
<portlet:resourceURL var="createAppraisalAction" id="createAppraisal" escapeXml="false">
    <portlet:param name="controller" value="TestsAction"/>
</portlet:resourceURL>
<portlet:resourceURL var="createEmployeeAction" id="createEmployee" escapeXml="false">
    <portlet:param name="controller" value="TestsAction"/>
</portlet:resourceURL>

<div>
    <input id="addAppraisal" name="testName" type="submit" value="<liferay-ui:message key="Create Appraisal" />">
    <input id="createEmployee" name="createEmployee" type="submit" value="<liferay-ui:message key="Create Employee" />">
</div>

<div id="dialog" title="Basic dialog" style="display:none">
  <p>This is the default dialog which is useful for displaying information. The dialog window can be moved, resized and closed with the 'x' icon.</p>
</div>

<script type="text/javascript">
    <%@ include file="/jsp/home/testTools.js"%>
</script>
