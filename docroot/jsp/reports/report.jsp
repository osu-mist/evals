<%@ page import="edu.osu.cws.evals.portlet.ReportsAction" %>
<%@ page import="edu.osu.cws.evals.hibernate.ReportMgr" %>
<%@ include file="/jsp/init.jsp"%>

<h2><liferay-ui:message key="reports"/></h2>
<div class="osu-cws-report-left">
    <div id="<portlet:namespace/>chartMenu" class="accordion-menu pass-notification chart-area">
        <div class="osu-accordion-header chart-menu">
            <ul class="chart-type-menu">
                <c:if test="${!searchView}">
                    <li><a href="#" class="evals-choose-chart"><liferay-ui:message key="report-chart-types"/></a>
                        <ul>
                            <li><a href="#" id ="<portlet:namespace/>changeToPieType">Pie Chart</a></li>
                            <li><a href="#" id ="<portlet:namespace/>changeToColumnType">Column Chart</a></li>
                            <li><a href="#" id ="<portlet:namespace/>changeToBarType">Bar Chart</a></li>
                        </ul>
                    </li>
                </c:if>
                <c:if test="${showDrillDownMenu}">
                    <li><a href="#"><liferay-ui:message key="report-drilldown"/></a>
                        <ul>
                        <c:forEach var="unit" items="${drillDownData}" varStatus="loopStatus">
                            <c:if test="${(scope == 'root' && reviewerBCName == unit[1])
                            || allowAllDrillDown || scope != 'root'}">
                                <li><a href="<portlet:actionURL windowState="<%= WindowState.MAXIMIZED.toString() %>">
                                    <portlet:param name="action" value="report"/>
                                    <portlet:param name="controller" value="ReportsAction"/>
                                    <portlet:param name="<%= ReportsAction.SCOPE %>" value="${nextScope}"/>
                                    <portlet:param name="<%= ReportsAction.SCOPE_VALUE %>" value="${unit[2]}"/>
                                    <portlet:param name="requestBreadcrumbs" value="${requestBreadcrumbs}"/>
                                    </portlet:actionURL>">${unit[1]}</a>
                                </li>
                            </c:if>
                        </c:forEach>
                        </ul>
                    </li>
                </c:if>
                <c:if test="${showMyReportLink}">
                    <li><a href="#"><liferay-ui:message key="report-my-report"/></a>
                        <ul>
                            <c:if test="${not empty supervisorJobTitle}">
                                <li><a href="<portlet:actionURL windowState="<%= WindowState.MAXIMIZED.toString() %>">
                                    <portlet:param name="action" value="report"/>
                                    <portlet:param name="controller" value="ReportsAction"/>
                                    <portlet:param name="<%= ReportsAction.SCOPE %>" value="<%= ReportsAction.SCOPE_SUPERVISOR %>"/>
                                    <portlet:param name="<%= ReportsAction.SCOPE_VALUE %>" value="${myReportSupervisorKey}"/>
                                    </portlet:actionURL>"><c:out value="${supervisorJobTitle}"/></a></li>
                            </c:if>
                            <c:if test="${not empty myReportBcName}">
                                <li><a href="<portlet:actionURL windowState="<%= WindowState.MAXIMIZED.toString() %>">
                                    <portlet:param name="action" value="report"/>
                                    <portlet:param name="controller" value="ReportsAction"/>
                                    <portlet:param name="<%= ReportsAction.SCOPE %>" value="<%= ReportsAction.SCOPE_BC %>"/>
                                    <portlet:param name="<%= ReportsAction.SCOPE_VALUE %>" value="${myReportBcName}"/>
                                    </portlet:actionURL>"><c:out value="${myReportBcName}"/></a></li>
                            </c:if>
                            <c:if test="${isAdmin == true}">
                                <li><a href="<portlet:actionURL windowState="<%= WindowState.MAXIMIZED.toString() %>">
                                    <portlet:param name="action" value="report"/>
                                    <portlet:param name="controller" value="ReportsAction"/>
                                    <portlet:param name="<%= ReportsAction.SCOPE %>" value="<%= ReportsAction.DEFAULT_SCOPE %>"/>
                                    <portlet:param name="<%= ReportsAction.SCOPE_VALUE %>" value="osu"/>
                                    </portlet:actionURL>">OSU</a></li>
                            </c:if>
                        </ul>
                    </li>
                </c:if>
            </ul>
        </div>
        <div class="accordion-content chart-content" id="<portlet:namespace/>chartContent" style="display: block;">
            <%@ include file="breadcrumbs.jsp"%>

            <strong><liferay-ui:message key="report-time-period"/></strong>
            <liferay-ui:message key="report-time-period-active"/> <fmt:formatDate value="${now}" pattern="MM/dd/yy"/>

            <c:if test="${!searchView and !isAppraisalSearch}">
                <div id="<portlet:namespace/>chart-div" class="chart-div"></div>
                <div id="<portlet:namespace/>chart-data-div"></div>
            </c:if>

            <c:if test="${searchView or isAppraisalSearch}">
                <h3><liferay-ui:message key="report-search-results-title"/> <c:out value="${searchTerm}"/></h3>
            </c:if>
            <c:if test="${searchView}">
                <%@ include file="/jsp/reports/reportSearchResults.jsp"%>
            </c:if>

            <c:if test="${!empty listAppraisals}">
                <%@ include file="/jsp/reports/reportList.jsp"%>
            </c:if>
        </div>
    </div>
</div>

<div class="osu-cws-report-right">

    <%@ include file="/jsp/reports/search.jsp"%>

    <c:if test="${!searchView}">
        <div id="<portlet:namespace/>accordionMenuChooseReport" class="accordion-menu">
            <div class="osu-accordion-header" onclick="<portlet:namespace/>toggleContent('<portlet:namespace/>ChooseReport');">
                <img id="<portlet:namespace/>ChooseReportImageToggle" src="/cps/images/accordion/accordion_arrow_up.png"/>
                <liferay-ui:message key="report-types" />
            </div>
            <div class="accordion-content" id="<portlet:namespace/>ChooseReport" style="display: block;">
                <ul>
                    <c:if test="${enableByUnitReports}">
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
    </c:if>

</div>

<div class="osu-cws-clear-both"></div>

<c:if test="${!searchView and !isAppraisalSearch}">
    <%@ include file="/jsp/reports/reportJs.jsp"%>
</c:if>

<%@ include file="/jsp/footer.jsp"%>
