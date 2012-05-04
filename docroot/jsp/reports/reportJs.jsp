<!--Load the AJAX API-->
<script type="text/javascript" src="https://www.google.com/jsapi"></script>

<script type="text/javascript">

  // Load the Visualization API and the piechart package.
  google.load('visualization', '1', {'packages':['corechart', 'table']});

  // Set a callback to run when the Google Visualization API is loaded.
  google.setOnLoadCallback(drawChart);
  var chart;
  var chartType = "${chartType}";
  var tableData;
  var chartData;
  var chartDataScopeMap = eval('(' + '${chartDataScopeMap}' + ')');
  var report = "${report}";
  var currentSupervisorName = "${currentSupervisorName}";
  var nextScope = "${nextScope}";

  // Callback that creates and populates a data table,
  // instantiates the pie chart, passes in the data and
  // draws it.
  function drawChart() {
    // Create the data table.
    tableData = new google.visualization.DataTable();
    tableData.addColumn('string', '<liferay-ui:message key="${reportHeader}"/>');
    tableData.addColumn('number', '<liferay-ui:message key="report-drilldown-num-evaluations"/> <liferay-ui:message key="${reportTitle}"/>');
    tableData.addRows([
        <c:forEach var="row" items="${tableData}" varStatus="loopStatus">
            [getGoogleTableUnitText('${row[2]}', "<liferay-ui:message key="${row[1]}"/>"), ${row[0]}]
            <c:if test="${!loopStatus.last}">
                ,
            </c:if>
        </c:forEach>
    ]);

    chartData = new google.visualization.DataTable();
    chartData.addColumn('string', '<liferay-ui:message key="${reportHeader}"/>');
    chartData.addColumn('number', '<liferay-ui:message key="report-drilldown-num-evals"/>');
    chartData.addRows([
        <c:forEach var="row" items="${chartData}" varStatus="loopStatus">
            ["<liferay-ui:message key="${row[1]}"/>", ${row[0]}]
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

      chart.draw(chartData, options);

      var table = new google.visualization.Table(document.getElementById('<portlet:namespace/>chart-data-div'));

      var tableOptions = {
        sortColumn: 0,
        allowHtml: true,
        cssClassNames: {
            headerRow: 'google-header-row',
            hoverTableRow: 'google-hover-table-row',
            oddTableRow: 'google-odd-table-row',
            tableRow: 'google-table-row'
        }
      }
      table.draw(tableData, tableOptions);

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
      var displayValue = chartData.getValue(chartSelection[0].row, 0);
      var unitName = chartDataScopeMap[displayValue];

      // if the drillDown level is supervisor, we don't allow drill down for currentSupervisor
      if (nextScope == 'supervisor' && displayValue == "${currentSupervisorName}") {
          return;
      }

      var scope = '${scope}';
      var allowAllDrillDown = ${allowAllDrillDown};
      var reviewerBCName = "${reviewerBCName}";

      if (scope == "<%= ReportsAction.DEFAULT_SCOPE%>") {
        if (!allowAllDrillDown && reviewerBCName != unitName) {
          return;
        }
      }

      // right now we don't support drilling down to the grouped "other".
      if (typeof unitName == "undefined") {
          return;
      }

      var drillDownURL= getDrillDownUrl(unitName);
      window.location = drillDownURL;
    }
  }

  function getDrillDownUrl(unitName) {
    var drillDownURL = '<portlet:actionURL windowState="<%= WindowState.MAXIMIZED.toString() %>">
      <portlet:param name="action" value="report"/>
      <portlet:param name="controller" value="ReportsAction"/>
      <portlet:param name="<%= ReportsAction.SCOPE %>" value="${nextScope}"/>
      <portlet:param name="<%= ReportsAction.REPORT %>" value="${report}"/>
      <portlet:param name="<%= ReportsAction.SCOPE_VALUE %>" value="unitName"/>
      <portlet:param name="requestBreadcrumbs" value="${requestBreadcrumbs}"/>
      </portlet:actionURL>';
    drillDownURL = drillDownURL.replace("unitName", unitName);

    return drillDownURL;
  }

  function getGoogleTableUnitText(unitName, displayName) {
    if (unitName == "") {
        return displayName;
    }

    var text = '<a href="' + getDrillDownUrl(unitName) + '">' + displayName + '</a>';

    <c:if test="${scope == 'root' || scope == 'supervisor' || scope == 'orgCode'}">
      var allowedDrillDown = {
      <c:forEach var="unit" items="${drillDownData}" varStatus="loopStatus">
          <c:choose>
            <c:when test="${(scope == 'root' && reviewerBCName == unit[1])
                            || allowAllDrillDown || scope != 'root'}">
            '${unit[2]}' : ''
            </c:when>
            <c:otherwise>
                '_fail${unit[2]}' : ''
            </c:otherwise>
          </c:choose>
          <c:if test="${!loopStatus.last}">, </c:if>
      </c:forEach>
      };

      if (!(unitName in allowedDrillDown)) {
        text = displayName;
      }
    </c:if>
    return text;
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