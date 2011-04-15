<%@ include file="/jsp/init.jsp" %>

<%
List criteria = (List) renderRequest.getAttribute("criteria");
%>

<h2>Evaluation Criteria</h2>

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

</table>