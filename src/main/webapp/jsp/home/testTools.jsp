<jsp:useBean id="testAppraisals" class="java.util.ArrayList" scope="request" />

<jsp:useBean id="permissionRule" class="edu.osu.cws.evals.models.PermissionRule" scope="request" />
<portlet:resourceURL var="createAppraisalAction" id="createAppraisal" escapeXml="false">
    <portlet:param name="controller" value="TestsAction"/>
</portlet:resourceURL>
<portlet:resourceURL var="createEmployeeAction" id="createPerson" escapeXml="false">
    <portlet:param name="controller" value="TestsAction"/>
</portlet:resourceURL>

<div>
    <input id="addAppraisal" name="testName" type="submit" value="<liferay-ui:message key="Create Appraisal" />">
    <input id="showCreateEmployee" type="submit" value="<liferay-ui:message key="Create Employee" />">

  <div id="dialog" title="Basic dialog" style="display:none">
    <form>
      <label>First name:</label>
      <input type="text" id="firstName"/><br/>
      <label>Last name:</label>
      <input type="text" id="lastName"/><br/>
      <label>Email:</label>
      <input type="text" id="email"/><br/>
      <label>onid:</label>
      <input type="text" id="onid"/><br/>
      <label>Appointment type:</label>
      <input type="text" id="appointmentType"/><br/>
      <label>Admin:</label>
      <input type="checkbox" id="admin"/><br/>
      <label>Supervisor:</label>
      <input type="checkbox" id="supervisor"/><br/>
      <label>Reviewer:</label>
      <input type="checkbox" id="reviewer"/><br/>
      <label>Business center:</label>
      <input type="text" id="businessCenter"/><br/>
      <input id="createEmployee" name="createEmployee" type="submit" value="<liferay-ui:message key="Create Employee" />">
    </form>
  </div>
</div>


<script type="text/javascript">
    <%@ include file="/jsp/home/testTools.js"%>
</script>
