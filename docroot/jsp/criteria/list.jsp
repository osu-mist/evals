<%@ include file="/jsp/init.jsp" %>

<%
List criteria = (List) renderRequest.getAttribute("criteria");

PortletURL addCriteriaURL = renderResponse.createRenderURL();
addCriteriaURL.setWindowState(WindowState.NORMAL);
addCriteriaURL.setParameter("action", "addCriteria");
%>

<h2>Evaluation Criteria</h2>
<liferay-ui:success key="criteria-saved" message="criteria-saved" />

<div class="separator"></div>
<table class="taglib-search-iterator">
    <tr class="portlet-section-header results-header">
        <th>Name</th>
        <th>Description</th>
        <th>Actions</th>
    </tr>

<c:forEach var="criterion" items="${criteria}" varStatus="loopStatus">
    <tr class="${loopStatus.index % 2 == 0 ? 'portlet-section-body results-row' : 'portlet-section-alternate results-row alt'}"
        onmouseover="this.className = 'portlet-section-body-hover results-row hover';"
        onmouseout="this.className = '${loopStatus.index % 2 == 0 ? 'portlet-section-body results-row' : 'portlet-section-alternate results-row alt'}';"
    >
        <td>${criterion.name}</td>
        <td>${criterion.currentDetail.description}</td>
        <td><a href="#">Edit</a> <a href="#">Delete</a>
    </tr>
</c:forEach>

<liferay-ui:icon
    image="add_article"
    url="<%= addCriteriaURL.toString() %>"
    label="true"
    message="Add Evaluation Criteria"
/>

</table>