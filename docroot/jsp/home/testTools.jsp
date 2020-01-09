<jsp:useBean id="testAppraisals" class="java.util.ArrayList" scope="request" />

<jsp:useBean id="permissionRule" class="edu.osu.cws.evals.models.PermissionRule" scope="request" />
<portlet:resourceURL var="saveDraftAJAXURL" id="updateTest" escapeXml="false">
    <portlet:param name="controller" value="TestsAction"/>
</portlet:resourceURL>

<div id="dialog" title="dialog box">
    <c:forEach var="appraisal" items="${testAppraisals}" varStatus="loopStatus">
        ${appraisal.job.jobTitle}<br/>
    </c:forEach>
</div>

<div>
    <input id="addAppraisal" name="testName" type="submit" value="<liferay-ui:message key="Create Appraisal" />">
    <input id="deleteAppraisal" name="testName" type="submit" value="<liferay-ui:message key="Delete Appraisal" />">
</div>

<script type="text/javascript">
    <%@ include file="/jsp/home/testTools.js"%>
</script>
