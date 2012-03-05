<!--Load the AJAX API-->
<script type="text/javascript" src="https://www.google.com/jsapi"></script>

<script type="text/javascript">

  // Load the Visualization API and the piechart package.
  google.load('visualization', '1', {'packages':['corechart', 'table']});

  // Set a callback to run when the Google Visualization API is loaded.
  google.setOnLoadCallback(drawChart);
  var chart;
  var chartType = "${chartType}";
  var chartData;
  var trimmedChartData;
  var report = "${report}";

  // Callback that creates and populates a data table,
  // instantiates the pie chart, passes in the data and
  // draws it.
  function drawChart() {
    // Create the data table.
    chartData = new google.visualization.DataTable();
    chartData.addColumn('string', '<liferay-ui:message key="${reportHeader}"/>');
    chartData.addColumn('number', '<liferay-ui:message key="report-drilldown-num-evaluations"/> <liferay-ui:message key="${reportTitle}"/>');
    chartData.addRows([
        <c:forEach var="row" items="${chartData}" varStatus="loopStatus">
            ['<liferay-ui:message key="${row[1]}"/>', ${row[0]}]
            <c:if test="${!loopStatus.last}">
                ,
            </c:if>
        </c:forEach>
    ]);

    trimmedChartData = new google.visualization.DataTable();
    trimmedChartData.addColumn('string', '<liferay-ui:message key="${reportHeader}"/>');
    trimmedChartData.addColumn('number', '<liferay-ui:message key="report-drilldown-num-evals"/>');
    trimmedChartData.addRows([
        <c:forEach var="row" items="${trimmedChartData}" varStatus="loopStatus">
            ['<liferay-ui:message key="${row[1]}"/>', ${row[0]}]
            <c:if test="${!loopStatus.last}">
                ,
            </c:if>
        </c:forEach>
    ]);

    // Set chart options
    var options = {
      'title':'<liferay-ui:message key="${reportTitle}"/>',
      'width':620,
      'height':450
    };

    switch (chartType) {
      case "column":
        chart = new google.visualization.ColumnChart(document.getElementById('<portlet:namespace/>chart-div'));
        break;
      case "bar":
        chart = new google.visualization.BarChart(document.getElementById('<portlet:namespace/>chart-div'));
        break;
      default:
        chart = new google.visualization.PieChart(document.getElementById('<portlet:namespace/>chart-div'));
        break;
      }

      // Instantiate and draw our chart, passing in some options.
      chart.draw(trimmedChartData, options);

      var table = new google.visualization.Table(document.getElementById('<portlet:namespace/>chart-data-div'));

      var tableOptions = {
        sortColumn: 0,
        cssClassNames: {
            headerRow: 'google-header-row',
            hoverTableRow: 'google-hover-table-row',
            oddTableRow: 'google-odd-table-row',
            tableRow: 'google-table-row'
        }
      }
      table.draw(chartData, tableOptions);

      // When the table is selected, update the orgchart.
      google.visualization.events.addListener(chart, 'select', function() {
          chartDrillDown();
      });
  }

  /**
   * Handles the user clicking on the drill down links
   */
  function chartDrillDown() {
    var chartSelection = chart.getSelection();

    if (typeof chartSelection == "undefined" || typeof chartSelection[0] == "undefined") {
        return;
    }

    // drill down by clicking on the chart is allowed on all by unit reports
    if (report.indexOf('<%= ReportMgr.UNIT %>') != -1) {
      var unitName = trimmedChartData.getValue(chartSelection[0].row, 0);

      var scope = '${scope}';
      var allowAllDrillDown = ${allowAllDrillDown};
      var reviewerBCName = "${reviewerBCName}";

      if (scope == "<%= ReportsAction.DEFAULT_SCOPE%>") {
        if (!allowAllDrillDown && reviewerBCName != unitName) {
          return;
        }
      }

      // right now we don't support drilling down to the grouped "other"
      if (unitName == '<liferay-ui:message key="other"/>') {
          return;
      }

      var drillDownURL= '<portlet:actionURL windowState="<%= WindowState.MAXIMIZED.toString() %>">
        <portlet:param name="action" value="report"/>
        <portlet:param name="controller" value="ReportsAction"/>
        <portlet:param name="<%= ReportsAction.SCOPE %>" value="${nextScope}"/>
        <portlet:param name="<%= ReportsAction.REPORT %>" value="${report}"/>
        <portlet:param name="<%= ReportsAction.SCOPE_VALUE %>" value="unitName"/>
        <portlet:param name="requestBreadcrumbs" value="${requestBreadcrumbs}"/>
        </portlet:actionURL>';

      drillDownURL = drillDownURL.replace("unitName", unitName);
      window.location = drillDownURL;
    }
  }

jQuery(document).ready(function() {
  jQuery("#<portlet:namespace/>changeToPieType").click(function(e) {
    return changeChartType("pie", e);
  });

  jQuery("#<portlet:namespace/>changeToColumnType").click(function(e) {
    return changeChartType("column", e);
  });

  jQuery("#<portlet:namespace/>changeToBarType").click(function(e) {
    return changeChartType("bar", e);
  });
});
    
<portlet:resourceURL var="updateChartType" id="saveChartType" escapeXml="false" />
function saveChartType() {
  var querystring = {'chartType': chartType, 'controller': "ReportsAction"};
  jQuery.ajax({
    type: "POST",
    url: "<%=renderResponse.encodeURL(updateChartType.toString())%>",
    data: querystring
  });
}

function changeChartType(type, e) {
  chartType = type;
  e.preventDefault();
  chart.clearChart();
  drawChart();
  saveChartType(chartType);
  return false;
}
</script>