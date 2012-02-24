<%@ page import="edu.osu.cws.evals.portlet.ReportsAction" %>
<%@ page import="edu.osu.cws.evals.hibernate.ReportMgr" %>
<%@ include file="/jsp/init.jsp"%>

<h2><liferay-ui:message key="reports"/></h2>
<div class="osu-cws-report-left">
    <div id="<portlet:namespace/>chartMenu" class="accordion-menu pass-notification chart-area">
        <div class="osu-accordion-header chart-menu">
            <ul class="chart-type-menu">
                <li><a href="#" class="evals-choose-chart"><liferay-ui:message key="report-chart-types"/></a>
                    <ul>
                        <li><a href="#" id ="<portlet:namespace/>changeToPieType">Pie Chart</a></li>
                        <li><a href="#" id ="<portlet:namespace/>changeToColumnType">Column Chart</a></li>
                        <li><a href="#" id ="<portlet:namespace/>changeToBarType">Bar Chart</a></li>
                    </ul>
                </li>
                <c:if test="${scope != 'orgCode' && (allowAllDrillDown || reviewerBCName != '')}">
                    <li><a href="#"><liferay-ui:message key="report-drilldown"/></a>
                        <ul>
                        <c:forEach var="unit" items="${drillDownData}" varStatus="loopStatus">
                            <c:if test="${(scope == 'root' && reviewerBCName == unit[1])
                            || allowAllDrillDown || scope != 'root'}">
                                <li><a href="<portlet:actionURL windowState="<%= WindowState.MAXIMIZED.toString() %>">
                                    <portlet:param name="action" value="report"/>
                                    <portlet:param name="controller" value="ReportsAction"/>
                                    <portlet:param name="<%= ReportsAction.SCOPE %>" value="${nextScope}"/>
                                    <portlet:param name="<%= ReportsAction.SCOPE_VALUE %>" value="${unit[1]}"/>
                                    <portlet:param name="requestBreadcrumbs" value="${requestBreadcrumbs}"/>
                                    </portlet:actionURL>">${unit[1]}</a>
                                </li>
                            </c:if>
                        </c:forEach>
                        </ul>
                    </li>
                </c:if>
            </ul>
        </div>
        <div class="accordion-content chart-content" id="<portlet:namespace/>chartContent" style="display: block;">
            <%@ include file="breadcrumbs.jsp"%>
            <div id="<portlet:namespace/>chart-div" class="chart-div"></div>
            <div id="<portlet:namespace/>chart-data-div"></div>
        </div>
    </div>
</div>

<div class="osu-cws-report-right">
        
    <div id="<portlet:namespace/>accordionMenuChooseReport" class="accordion-menu">
        <div class="osu-accordion-header" onclick="<portlet:namespace/>toggleContent('<portlet:namespace/>ChooseReport');">
            <img id="<portlet:namespace/>ChooseReportImageToggle" src="/cps/images/accordion/accordion_arrow_up.png"/>
            <liferay-ui:message key="report-types" />
        </div>
        <div class="accordion-content" id="<portlet:namespace/>ChooseReport" style="display: block;">
            <ul>
                <c:if test="${scope != 'orgCode'}">
                    <li><a href="<portlet:actionURL windowState="<%= WindowState.MAXIMIZED.toString() %>">
                            <portlet:param name="action" value="report"/>
                            <portlet:param name="controller" value="ReportsAction"/>
                            <portlet:param name="scope" value="${scope}"/>
                            <portlet:param name="scopeValue" value="${scopeValue}"/>
                            <portlet:param name="requestBreadcrumbs" value="${requestBreadcrumbs}"/>
                            <portlet:param name="report" value="<%= ReportsAction.REPORT_DEFAULT%>"/>
                            </portlet:actionURL>"><liferay-ui:message key="report-title-unitBreakdown"/></a></li>
                    <li><a href="<portlet:actionURL windowState="<%= WindowState.MAXIMIZED.toString() %>">
                            <portlet:param name="action" value="report"/>
                            <portlet:param name="controller" value="ReportsAction"/>
                            <portlet:param name="scope" value="${scope}"/>
                            <portlet:param name="scopeValue" value="${scopeValue}"/>
                            <portlet:param name="requestBreadcrumbs" value="${requestBreadcrumbs}"/>
                            <portlet:param name="report" value="<%= ReportsAction.REPORT_UNIT_OVERDUE%>"/>
                            </portlet:actionURL>"><liferay-ui:message key="report-title-unitOverdue"/></a></li>
                    <li><a href="<portlet:actionURL windowState="<%= WindowState.MAXIMIZED.toString() %>">
                            <portlet:param name="action" value="report"/>
                            <portlet:param name="controller" value="ReportsAction"/>
                            <portlet:param name="scope" value="${scope}"/>
                            <portlet:param name="scopeValue" value="${scopeValue}"/>
                            <portlet:param name="requestBreadcrumbs" value="${requestBreadcrumbs}"/>
                            <portlet:param name="report" value="<%= ReportsAction.REPORT_UNIT_WAYOVERDUE%>"/>
                            </portlet:actionURL>"><liferay-ui:message key="report-title-unitWayOverdue"/></a></li>
                </c:if>
                <li><a href="<portlet:actionURL windowState="<%= WindowState.MAXIMIZED.toString() %>">
                        <portlet:param name="action" value="report"/>
                        <portlet:param name="controller" value="ReportsAction"/>
                        <portlet:param name="scope" value="${scope}"/>
                        <portlet:param name="scopeValue" value="${scopeValue}"/>
                        <portlet:param name="requestBreadcrumbs" value="${requestBreadcrumbs}"/>
                        <portlet:param name="report" value="<%= ReportsAction.REPORT_STAGE_BREAKDOWN%>"/>
                        </portlet:actionURL>"><liferay-ui:message key="report-title-stageBreakdown"/></a></li>
                <li><a href="<portlet:actionURL windowState="<%= WindowState.MAXIMIZED.toString() %>">
                        <portlet:param name="action" value="report"/>
                        <portlet:param name="controller" value="ReportsAction"/>
                        <portlet:param name="scope" value="${scope}"/>
                        <portlet:param name="scopeValue" value="${scopeValue}"/>
                        <portlet:param name="requestBreadcrumbs" value="${requestBreadcrumbs}"/>
                        <portlet:param name="report" value="<%= ReportsAction.REPORT_STAGE_OVERDUE%>"/>
                        </portlet:actionURL>"><liferay-ui:message key="report-title-stageOverdue"/></a></li>
                <li><a href="<portlet:actionURL windowState="<%= WindowState.MAXIMIZED.toString() %>">
                        <portlet:param name="action" value="report"/>
                        <portlet:param name="controller" value="ReportsAction"/>
                        <portlet:param name="scope" value="${scope}"/>
                        <portlet:param name="scopeValue" value="${scopeValue}"/>
                        <portlet:param name="requestBreadcrumbs" value="${requestBreadcrumbs}"/>
                        <portlet:param name="report" value="<%= ReportsAction.REPORT_STAGE_WAYOVERDUE%>"/>
                        </portlet:actionURL>"><liferay-ui:message key="report-title-stageWayOverdue"/></a></li>
            </ul>
        </div>
    </div>

</div>

<div class="osu-cws-clear-both"></div>

<%@ include file="/jsp/reports/reportList.jsp"%>

<!--Load the AJAX API-->
<script type="text/javascript" src="https://www.google.com/jsapi"></script>

<script type="text/javascript">

  // Load the Visualization API and the piechart package.
  google.load('visualization', '1', {'packages':['corechart', 'table']});

  // Set a callback to run when the Google Visualization API is loaded.
  google.setOnLoadCallback(drawChart);
  var chart;
  var chartType = "pie";
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
    chartData.addColumn('number', '<liferay-ui:message key="report-drilldown-num-evals"/>');
    chartData.addRows([
        <c:forEach var="row" items="${chartData}" varStatus="loopStatus">
            ['${row[1]}', ${row[0]}]
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
            ['${row[1]}', ${row[0]}]
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
      var drillDownUrl = '<a href="<portlet:actionURL windowState="<%= WindowState.MAXIMIZED.toString() %>">
                                    <portlet:param name="action" value="report"/>
                                    <portlet:param name="controller" value="ReportsAction"/>
                                    <portlet:param name="<%= ReportsAction.SCOPE %>" value="${nextScope}"/>
                                    <portlet:param name="<%= ReportsAction.SCOPE_VALUE %>" value="unitName"/>
                                    </portlet:actionURL>">{0}</a>';
      drillDownUrl = drillDownUrl.replace("unitName", '{0}');
      var formatter = new google.visualization.TablePatternFormat(drillDownUrl);
      formatter.format(chartData, [0]); // Apply formatter and set the formatted value of the first column.

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
      if (unitName == "other") {
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
    e.preventDefault();
    chartType = "pie";
    chart.clearChart();
    drawChart();
    return false;
  });

  jQuery("#<portlet:namespace/>changeToColumnType").click(function(e) {
    e.preventDefault();
    chartType = "column";
    chart.clearChart();
    drawChart();
    return false;
  });

  jQuery("#<portlet:namespace/>changeToBarType").click(function(e) {
    e.preventDefault();
    chartType = "bar";
    chart.clearChart();
    drawChart();
    return false;
  });
});
</script>

<%@ include file="/jsp/footer.jsp"%>
