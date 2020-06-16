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
    <input id="showCreateEmployee" type="submit" value="<liferay-ui:message key="Create Employee" />">

  <div id="dialog" title="Basic dialog" style="display:none">
    <form>
      <!--<liferay-ui:input-textarea param="<portlet:namespace/>firstName"/>-->
      <input  type="text" name="<portlet:namespace/>firstName" id="<portlet:namespace/>firstName"/>
      <input id="createEmployee" name="createEmployee" type="submit" value="<liferay-ui:message key="Create Employee" />">
    </form>
  </div>
</div>


<script type="text/javascript">
    <%@ include file="/jsp/home/testTools.js"%>
</script>
