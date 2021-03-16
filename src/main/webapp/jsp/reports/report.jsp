<%@ page import="edu.osu.cws.evals.portlet.ReportsAction" %>
<%@ page import="edu.osu.cws.evals.hibernate.ReportMgr" %>
<%@ include file="/jsp/init.jsp"%>

<c:set var="drilldownClass" value=""/>
    <c:if test="${scope == 'supervisor'}">
    <c:set var="drilldownClass" value="supervisor-report"/>
</c:if>

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
                        <ul class="${drilldownClass}">
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
                    <li class="float-left first"><a href="#"><liferay-ui:message key="report-my-report"/></a>
                        <ul>
                            <c:if test="${not empty supervisorJobTitle}">
                                <li><a href="<portlet:actionURL windowState="<%= WindowState.MAXIMIZED.toString() %>">
                                    <portlet:param name="action" value="report"/>
                                    <portlet:param name="controller" value="ReportsAction"/>
                                    <portlet:param name="<%= ReportsAction.SCOPE %>" value="<%= ReportsAction.SCOPE_SUPERVISOR %>"/>
                                    <portlet:param name="<%= ReportsAction.SCOPE_VALUE %>" value="${myReportSupervisorKey}"/>
                                    <portlet:param name="requestBreadcrumbs" value="${breadcrumbsWithRootOnly}" />
                                    </portlet:actionURL>"><c:out value="${supervisorJobTitle}"/></a></li>
                            </c:if>
                            <c:if test="${not empty myReportBcName}">
                                <li><a href="<portlet:actionURL windowState="<%= WindowState.MAXIMIZED.toString() %>">
                                    <portlet:param name="action" value="report"/>
                                    <portlet:param name="controller" value="ReportsAction"/>
                                    <portlet:param name="<%= ReportsAction.SCOPE %>" value="<%= ReportsAction.SCOPE_BC %>"/>
                                    <portlet:param name="<%= ReportsAction.SCOPE_VALUE %>" value="${myReportBcName}"/>
                                    <portlet:param name="requestBreadcrumbs" value="${breadcrumbsWithRootOnly}" />
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
                <h3 class="clean"><liferay-ui:message key="report-search-results-title"/> <c:out value="${searchTerm}"/></h3>
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

    <c:if test="${isReviewer || isAdmin || isSupervisor}">
        <%@ include file="/jsp/reports/search.jsp"%>
    </c:if>

    <c:if test="${!searchView and !isAppraisalSearch}">
        <div id="<portlet:namespace/>accordionMenuChooseReport" class="accordion-menu">
            <div class="osu-accordion-header" onclick="<portlet:namespace/>toggleContent('<portlet:namespace/>ChooseReport');">
                <img id="<portlet:namespace/>ChooseReportImageToggle" src="/o/evals/images/accordion/accordion_arrow_up.png"/>
                <liferay-ui:message key="report-types" />
            </div>
            <div class="accordion-content" id="<portlet:namespace/>ChooseReport" style="display: block;">
                <ul>
                    <li><liferay-ui:message key="report-choose-active-overview"/>
                        (
                        <c:if test="${enableByUnitReports}">
                            <a href="<portlet:actionURL windowState="<%= WindowState.MAXIMIZED.toString() %>">
                                <portlet:param name="action" value="report"/>
                                <portlet:param name="controller" value="ReportsAction"/>
                                <portlet:param name="scope" value="${scope}"/>
                                <portlet:param name="scopeValue" value="${scopeValue}"/>
                                <portlet:param name="requestBreadcrumbs" value="${requestBreadcrumbs}"/>
                                <portlet:param name="report" value="<%= ReportsAction.REPORT_DEFAULT%>"/>
                                </portlet:actionURL>">
                                <c:choose>
                                    <c:when test="${scope == 'supervisor'}">
                                        <liferay-ui:message key="report-choose-by-supervisor"/>
                                    </c:when>
                                    <c:otherwise>
                                        <liferay-ui:message key="report-choose-by-unit"/>
                                    </c:otherwise>
                                </c:choose>
                            </a> |
                        </c:if>
                        <a href="<portlet:actionURL windowState="<%= WindowState.MAXIMIZED.toString() %>">
                            <portlet:param name="action" value="report"/>
                            <portlet:param name="controller" value="ReportsAction"/>
                            <portlet:param name="scope" value="${scope}"/>
                            <portlet:param name="scopeValue" value="${scopeValue}"/>
                            <portlet:param name="requestBreadcrumbs" value="${requestBreadcrumbs}"/>
                            <portlet:param name="report" value="<%= ReportsAction.REPORT_STAGE_BREAKDOWN%>"/>
                            </portlet:actionURL>"><liferay-ui:message key="report-choose-by-stage"/></a>
                        )
                    </li>
                    <li><liferay-ui:message key="report-choose-active-overdue"/>
                        (
                        <c:if test="${enableByUnitReports}">
                            <a href="<portlet:actionURL windowState="<%= WindowState.MAXIMIZED.toString() %>">
                                <portlet:param name="action" value="report"/>
                                <portlet:param name="controller" value="ReportsAction"/>
                                <portlet:param name="scope" value="${scope}"/>
                                <portlet:param name="scopeValue" value="${scopeValue}"/>
                                <portlet:param name="requestBreadcrumbs" value="${requestBreadcrumbs}"/>
                                <portlet:param name="report" value="<%= ReportsAction.REPORT_UNIT_OVERDUE%>"/>
                                </portlet:actionURL>">
                                <c:choose>
                                    <c:when test="${scope == 'supervisor'}">
                                        <liferay-ui:message key="report-choose-by-supervisor"/>
                                    </c:when>
                                    <c:otherwise>
                                        <liferay-ui:message key="report-choose-by-unit"/>
                                    </c:otherwise>
                                </c:choose>
                            </a> |
                        </c:if>
                        <a href="<portlet:actionURL windowState="<%= WindowState.MAXIMIZED.toString() %>">
                            <portlet:param name="action" value="report"/>
                            <portlet:param name="controller" value="ReportsAction"/>
                            <portlet:param name="scope" value="${scope}"/>
                            <portlet:param name="scopeValue" value="${scopeValue}"/>
                            <portlet:param name="requestBreadcrumbs" value="${requestBreadcrumbs}"/>
                            <portlet:param name="report" value="<%= ReportsAction.REPORT_STAGE_OVERDUE%>"/>
                            </portlet:actionURL>"><liferay-ui:message key="report-choose-by-stage"/></a>
                        )
                    </li>
                    <li><liferay-ui:message key="report-choose-active-wayOerdue"/>
                        (
                        <c:if test="${enableByUnitReports}">
                            <a href="<portlet:actionURL windowState="<%= WindowState.MAXIMIZED.toString() %>">
                                <portlet:param name="action" value="report"/>
                                <portlet:param name="controller" value="ReportsAction"/>
                                <portlet:param name="scope" value="${scope}"/>
                                <portlet:param name="scopeValue" value="${scopeValue}"/>
                                <portlet:param name="requestBreadcrumbs" value="${requestBreadcrumbs}"/>
                                <portlet:param name="report" value="<%= ReportsAction.REPORT_UNIT_WAYOVERDUE%>"/>
                                </portlet:actionURL>">
                                <c:choose>
                                    <c:when test="${scope == 'supervisor'}">
                                        <liferay-ui:message key="report-choose-by-supervisor"/>
                                    </c:when>
                                    <c:otherwise>
                                        <liferay-ui:message key="report-choose-by-unit"/>
                                    </c:otherwise>
                                </c:choose>
                            </a> |
                        </c:if>
                        <a href="<portlet:actionURL windowState="<%= WindowState.MAXIMIZED.toString() %>">
                            <portlet:param name="action" value="report"/>
                            <portlet:param name="controller" value="ReportsAction"/>
                            <portlet:param name="scope" value="${scope}"/>
                            <portlet:param name="scopeValue" value="${scopeValue}"/>
                            <portlet:param name="requestBreadcrumbs" value="${requestBreadcrumbs}"/>
                            <portlet:param name="report" value="<%= ReportsAction.REPORT_STAGE_WAYOVERDUE%>"/>
                            </portlet:actionURL>"><liferay-ui:message key="report-choose-by-stage"/></a>
                        )
                    </li>
                </ul>
            </div>
        </div>

        <c:if test="${scope == 'supervisor'}">
            <c:if test="${!isMyReport}">
                <%@ include file="/jsp/home/myStatus.jsp"%>
            </c:if>

            <%@ include file="/jsp/home/myTeam.jsp"%>
        </c:if>

        <strong><liferay-ui:message key="report-selection"/></strong><br />
        <c:if test="${not empty supervisorJobTitle}">
            <portlet:actionURL windowState="<%= WindowState.MAXIMIZED.toString() %>" var="supervisorReportLink" escapeXml="false">
                <portlet:param name="action" value="report"/>
                <portlet:param name="controller" value="ReportsAction"/>
                <portlet:param name="<%= ReportsAction.SCOPE %>" value="<%= ReportsAction.SCOPE_SUPERVISOR %>"/>
                <portlet:param name="<%= ReportsAction.SCOPE_VALUE %>" value="${myReportSupervisorKey}"/>
                <portlet:param name="requestBreadcrumbs" value="${breadcrumbsWithRootOnly}" />
            </portlet:actionURL>

            <input type="submit" value="<c:out value="${supervisorJobTitle}"/>"
            onclick="location.href ='<%= renderResponse.encodeURL(supervisorReportLink.toString())%>'" /><br />
        </c:if>
        <c:if test="${not empty myReportBcName}">
            <portlet:actionURL windowState="<%= WindowState.MAXIMIZED.toString() %>" var="bcReportLink" escapeXml="false">
                <portlet:param name="action" value="report"/>
                <portlet:param name="controller" value="ReportsAction"/>
                <portlet:param name="<%= ReportsAction.SCOPE %>" value="<%= ReportsAction.SCOPE_BC %>"/>
                <portlet:param name="<%= ReportsAction.SCOPE_VALUE %>" value="${myReportBcName}"/>
                <portlet:param name="requestBreadcrumbs" value="${breadcrumbsWithRootOnly}" />
            </portlet:actionURL>
            <input type="submit" value="<c:out value="${myReportBcName}"/>"
                onclick="location.href ='<%= renderResponse.encodeURL(bcReportLink.toString())%>'" /><br />
        </c:if>
        <c:if test="${isAdmin == true}">
            <portlet:actionURL windowState="<%= WindowState.MAXIMIZED.toString() %>" var="osuReportLink" escapeXml="false">
                <portlet:param name="action" value="report"/>
                <portlet:param name="controller" value="ReportsAction"/>
                <portlet:param name="<%= ReportsAction.SCOPE %>" value="<%= ReportsAction.DEFAULT_SCOPE %>"/>
                <portlet:param name="<%= ReportsAction.SCOPE_VALUE %>" value="osu"/>
            </portlet:actionURL>
            <input type="submit" value="<liferay-ui:message key="university-acronym"/>"
                onclick="location.href ='<%= renderResponse.encodeURL(osuReportLink.toString())%>'" /><br />
        </c:if>
    </c:if>

</div>

<div class="osu-cws-clear-both"></div>

<c:if test="${!searchView and !isAppraisalSearch}">
    <%@ include file="/jsp/reports/reportJs.jsp"%>
</c:if>

<%@ include file="/jsp/footer.jsp"%>
