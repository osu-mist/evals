<%@ page import="edu.osu.cws.evals.portlet.ReportsAction" %>
<%@ include file="/jsp/init.jsp"%>

<h2><liferay-ui:message key="reports"/></h2>
<%@ include file="breadcrumbs.jsp"%>

<div id="<portlet:namespace/>chartDiv"></div>

<h3><liferay-ui:message key="report-types"/></h3>
<ul>
    <li><a href="<portlet:actionURL windowState="<%= WindowState.MAXIMIZED.toString() %>">
                        <portlet:param name="action" value="report"/>
                        <portlet:param name="controller" value="ReportsAction"/>
                        <portlet:param name="scope" value="${scope}"/>
                        <portlet:param name="scopeValue" value="${scopeValue}"/>
                        <portlet:param name="report" value="<%= ReportsAction.REPORT_DEFAULT%>"/>
                        </portlet:actionURL>"><liferay-ui:message key="report-title-unitBreakdown"/></a></li>
    <li><a href="<portlet:actionURL windowState="<%= WindowState.MAXIMIZED.toString() %>">
                        <portlet:param name="action" value="report"/>
                        <portlet:param name="controller" value="ReportsAction"/>
                        <portlet:param name="scope" value="${scope}"/>
                        <portlet:param name="scopeValue" value="${scopeValue}"/>
                        <portlet:param name="report" value="<%= ReportsAction.REPORT_UNIT_OVERDUE%>"/>
                        </portlet:actionURL>"><liferay-ui:message key="report-title-unitOverdue"/></a></li>
    <li><a href="<portlet:actionURL windowState="<%= WindowState.MAXIMIZED.toString() %>">
                        <portlet:param name="action" value="report"/>
                        <portlet:param name="controller" value="ReportsAction"/>
                        <portlet:param name="scope" value="${scope}"/>
                        <portlet:param name="scopeValue" value="${scopeValue}"/>
                        <portlet:param name="report" value="<%= ReportsAction.REPORT_UNIT_WAYOVERDUE%>"/>
                        </portlet:actionURL>"><liferay-ui:message key="report-title-unitWayOverdue"/></a></li>
    <li><a href="<portlet:actionURL windowState="<%= WindowState.MAXIMIZED.toString() %>">
                        <portlet:param name="action" value="report"/>
                        <portlet:param name="controller" value="ReportsAction"/>
                        <portlet:param name="scope" value="${scope}"/>
                        <portlet:param name="scopeValue" value="${scopeValue}"/>
                        <portlet:param name="report" value="<%= ReportsAction.REPORT_STAGE_BREAKDOWN%>"/>
                        </portlet:actionURL>"><liferay-ui:message key="report-title-stageBreakdown"/></a></li>
    <li><a href="<portlet:actionURL windowState="<%= WindowState.MAXIMIZED.toString() %>">
                        <portlet:param name="action" value="report"/>
                        <portlet:param name="controller" value="ReportsAction"/>
                        <portlet:param name="scope" value="${scope}"/>
                        <portlet:param name="scopeValue" value="${scopeValue}"/>
                        <portlet:param name="report" value="<%= ReportsAction.REPORT_STAGE_OVERDUE%>"/>
                        </portlet:actionURL>"><liferay-ui:message key="report-title-stageOverdue"/></a></li>
    <li><a href="<portlet:actionURL windowState="<%= WindowState.MAXIMIZED.toString() %>">
                        <portlet:param name="action" value="report"/>
                        <portlet:param name="controller" value="ReportsAction"/>
                        <portlet:param name="scope" value="${scope}"/>
                        <portlet:param name="scopeValue" value="${scopeValue}"/>
                        <portlet:param name="report" value="<%= ReportsAction.REPORT_STAGE_WAYOVERDUE%>"/>
                        </portlet:actionURL>"><liferay-ui:message key="report-title-stageWayOverdue"/></a></li>
</ul>

<h3><liferay-ui:message key="report-chart-types"/></h3>
<ul>
    <li><a href="#" id ="<portlet:namespace/>changeToPieType">Pie Chart</a></li>
    <li><a href="#" id ="<portlet:namespace/>changeToColumnType">Column Chart</a></li>
    <li><a href="#" id ="<portlet:namespace/>changeToBarType">Bar Chart</a></li>
</ul>

<table>
    <caption><liferay-ui:message key="report-drilldown"/></caption>
    <tr>
        <th><liferay-ui:message key="report-drilldown-unit"/></th>
        <th><liferay-ui:message key="report-drilldown-num-evals"/></th>
    </tr>
<c:forEach var="unit" items="${drillDownData}">
    <tr>
        <td><a href="<portlet:actionURL windowState="<%= WindowState.MAXIMIZED.toString() %>">
            <portlet:param name="action" value="report"/>
            <portlet:param name="controller" value="ReportsAction"/>
            <portlet:param name="<%= ReportsAction.SCOPE %>" value="${nextScope}"/>
            <portlet:param name="<%= ReportsAction.SCOPE_VALUE %>" value="${unit[1]}"/>
            <portlet:param name="<%= ReportsAction.REPORT %>" value="<%= ReportsAction.REPORT_UNIT_BREAKDOWN%>"/>
            </portlet:actionURL>">${unit[1]}
            </a>
        </td>
        <td>${unit[0]}</td>
    </tr>
</c:forEach>
</table>

<h2>Debug Information</h2>
<h3>Scope: ${scope} , ScopeValue: ${scopeValue}</h3>

<%--<h2>Data</h2>--%>
<%--<c:forEach var="row" items="${reportAppraisals}">--%>
    <%--<c:forEach var="column" items="${row}">--%>
        <%--<c:out value="${column}"/><br/>--%>
    <%--</c:forEach>--%>
    <%-----------------------------------------------------------------<br />--%>
<%--</c:forEach>--%>


<h3>Chart Data</h3>
<c:forEach var="row" items="${chartData}">
    <c:forEach var="column" items="${row}">
        <c:out value="${column}"/><br/>
    </c:forEach>
    ---------------------------------------------------------------<br />
</c:forEach>


<!--Load the AJAX API-->
<script type="text/javascript" src="https://www.google.com/jsapi"></script>

<script type="text/javascript">

  // Load the Visualization API and the piechart package.
  google.load('visualization', '1', {'packages':['corechart']});

  // Set a callback to run when the Google Visualization API is loaded.
  google.setOnLoadCallback(drawChart);
  var chart;
  var chartType = "pie";
  var data;
  var report = "${report}";

  // Callback that creates and populates a data table,
  // instantiates the pie chart, passes in the data and
  // draws it.
  function drawChart() {
    // Create the data table.
    data = new google.visualization.DataTable();
    data.addColumn('string', 'unit');
    data.addColumn('number', 'evals');
    data.addRows([
        <c:forEach var="row" items="${chartData}" varStatus="loopStatus">
            ['${row[1]}', ${row[0]}]
            <c:if test="${!loopStatus.last}">
                ,
            </c:if>
        </c:forEach>
    ]);

    // Set chart options
    var options = {'title':'<liferay-ui:message key="${reportTitle}"/>',
      'width':400,
      'height':300};

    switch (chartType) {
      case "column":
        chart = new google.visualization.ColumnChart(document.getElementById('<portlet:namespace/>chartDiv'));
        break;
      case "bar":
        chart = new google.visualization.BarChart(document.getElementById('<portlet:namespace/>chartDiv'));
        break;
      default:
        chart = new google.visualization.PieChart(document.getElementById('<portlet:namespace/>chartDiv'));
        break;
      }

      // Instantiate and draw our chart, passing in some options.
      chart.draw(data, options);

      // When the table is selected, update the orgchart.
      google.visualization.events.addListener(chart, 'select', function() {
          var chartSelection = chart.getSelection();
          alert("row selected => " + data.getValue(chartSelection[0].row, 0));

          if (report == "<%= ReportsAction.REPORT_UNIT_BREAKDOWN%>") {
            var unitName = data.getValue(chartSelection[0].row, 0);
            var drillDownURL= '<portlet:actionURL windowState="<%= WindowState.MAXIMIZED.toString() %>">
                <portlet:param name="action" value="report"/>
                <portlet:param name="controller" value="ReportsAction"/>
                <portlet:param name="<%= ReportsAction.SCOPE %>" value="${nextScope}"/>
                <portlet:param name="<%= ReportsAction.REPORT %>" value="<%= ReportsAction.REPORT_UNIT_BREAKDOWN%>"/>
                <portlet:param name="<%= ReportsAction.SCOPE_VALUE %>" value="unitName"/>
                </portlet:actionURL>';

            drillDownURL = drillDownURL.replace("unitName", unitName);
            window.location = drillDownURL;
          }
      });
  }

jQuery(document).ready(function() {
  jQuery("#<portlet:namespace/>changeToPieType").click(function() {
    chartType = "pie";
    chart.clearChart();
    drawChart();
  });

  jQuery("#<portlet:namespace/>changeToColumnType").click(function() {
    chartType = "column";
    chart.clearChart();
    drawChart();
  });

  jQuery("#<portlet:namespace/>changeToBarType").click(function() {
    chartType = "bar";
    chart.clearChart();
    drawChart();
  });
});
</script>