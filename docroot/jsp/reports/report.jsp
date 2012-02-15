<%@ page import="edu.osu.cws.evals.portlet.ReportsAction" %>
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
                <li><a href="#"><liferay-ui:message key="report-drilldown"/></a>
                    <ul>
                    <c:forEach var="unit" items="${drillDownData}" varStatus="loopStatus">
                        <li><a href="<portlet:actionURL windowState="<%= WindowState.MAXIMIZED.toString() %>">
                            <portlet:param name="action" value="report"/>
                            <portlet:param name="controller" value="ReportsAction"/>
                            <portlet:param name="<%= ReportsAction.SCOPE %>" value="${nextScope}"/>
                            <portlet:param name="<%= ReportsAction.SCOPE_VALUE %>" value="${unit[1]}"/>
                            </portlet:actionURL>">${unit[1]}</a>
                        </li>
                    </c:forEach>
                    </ul>
                </li>
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

    <%--remove after demo--%>
    <div id="<portlet:namespace/>accordionMenupassNotification" class="accordion-menu">
        <div class="osu-accordion-header" onclick="<portlet:namespace/>toggleContent('<portlet:namespace/>passNotification');">
            <img id="<portlet:namespace/>passNotificationImageToggle" src="/cps/images/accordion/accordion_arrow_up.png"/>
            <liferay-ui:message key="notifications" />
        </div>
        <div class="accordion-content pass-notification" id="<portlet:namespace/>passNotification" style="display: block;">
            <h3><liferay-ui:message key="my-eval-actions" /></h3>
            <ul class="pass-menu-list">
                <li><a href="#">Software Architect Goals Overdue for 06/01/11 - 12/31/11 by 258 day(s)</a></li>
            </ul>
        </div>
    </div>
    <%--end of remove after demo--%>
        
    <div id="<portlet:namespace/>accordionMenuChooseReport" class="accordion-menu">
        <div class="osu-accordion-header" onclick="<portlet:namespace/>toggleContent('<portlet:namespace/>ChooseReport');">
            <img id="<portlet:namespace/>ChooseReportImageToggle" src="/cps/images/accordion/accordion_arrow_up.png"/>
            <liferay-ui:message key="report-types" />
        </div>
        <div class="accordion-content" id="<portlet:namespace/>ChooseReport" style="display: block;">
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
        </div>
    </div>

    <%--remove after demo--%>
    <div id="<portlet:namespace/>accordionMenukeyStatistics" class="accordion-menu">
        <div class="osu-accordion-header" onclick="<portlet:namespace/>toggleContent('<portlet:namespace/>keyStatistics');">
            <img id="<portlet:namespace/>keyStatisticsImageToggle" src="/cps/images/accordion/accordion_arrow_up.png"/>
            <liferay-ui:message key="report-key-statistics" />
        </div>
        <div class="accordion-content" id="<portlet:namespace/>keyStatistics" style="display: block;">
            <ul class="pass-menu-list">
                <li><a href="#">20 Evaluations Overdue</a></li>
                <li><a href="#">10 Evaluations 30+ days overdue</a></li>
                <li><a href="#">5 Evaluations recently completed</a></li>
            </ul>
        </div>
    </div>
    <%-- end remove after demo--%>

</div>

<div class="osu-cws-clear-both"></div>

<div class="pass-hide">
    <%@ include file="/jsp/reports/debug.jsp"%>
</div>

<!--Load the AJAX API-->
<script type="text/javascript" src="https://www.google.com/jsapi"></script>

<script type="text/javascript">

  // Load the Visualization API and the piechart package.
  google.load('visualization', '1', {'packages':['corechart', 'table']});

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
    data.addColumn('string', '<liferay-ui:message key="${reportHeader}"/>');
    data.addColumn('number', '<liferay-ui:message key="report-drilldown-num-evals"/>');
    data.addRows([
        <c:forEach var="row" items="${chartData}" varStatus="loopStatus">
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
      chart.draw(data, options);

      var table = new google.visualization.Table(document.getElementById('<portlet:namespace/>chart-data-div'));
      table.draw(data, {});

      // When the table is selected, update the orgchart.
      google.visualization.events.addListener(chart, 'select', function() {
          var chartSelection = chart.getSelection();
          console.log("row selected => " + data.getValue(chartSelection[0].row, 0));

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
