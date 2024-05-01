<%@ include file="/jsp/init.jsp"%>

<h2><liferay-ui:message key="bulk-update-title"/></h2>

<div>
    <c:if test="${updateResult == 'Success'}">
        <span class="portlet-msg-success"><liferay-ui:message key="Update Successful"/></span>
    </c:if>
    <c:if test="${updateResult == 'Failure'}">
        <span class="portlet-msg-fail"><liferay-ui:message key="Update Failed"/></span>
    </c:if>
    <h3>Filters</h3>
    <div class="bulk-update-filters">
        <form action="<portlet:actionURL windowState="<%= WindowState.MAXIMIZED.toString() %>">
            <portlet:param name="action" value="filter"/>
            <portlet:param name="controller" value="BulkUpdateAction"/>
            </portlet:actionURL>" id="<portlet:namespace />fm" name="<portlet:namespace />fm" method="post">
            <!-- Date fields -->
            <div class="bulk-date-conditionals">
                <div>
                    <div>
                        Start Date
                    </div>
                    <div>
                        <select name="startDateConditional" value="${startDateConditional}">
                            <c:forEach var="conditional" items="${rangeConditionals}">
                                <option value="${conditional.value}" ${startDateConditional == conditional.value ? 'selected':''}>${conditional.display}</option>
                            </c:forEach>
                        </select>
                    </div>
                    <div>
                        <input name="startDateValue" type="date" value="${startDateValue}"/>
                    </div>
                </div>
                <div>
                    <div>
                        End Date
                    </div>
                    <div>
                        <select name="endDateConditional" value="${endDateConditional}">
                            <c:forEach var="conditional" items="${rangeConditionals}">
                                <option value="${conditional.value}" ${endDateConditional == conditional.value ? 'selected':''}>${conditional.display}</option>
                            </c:forEach>
                        </select>
                    </div>
                    <div>
                        <input name="endDateValue" type="date" value="${endDateValue}"/>
                    </div>
                </div>
            </div>

            <!-- org codes -->
            <div class="bulk-conditionals">
                <div>
                    Org Code
                </div>
                <div>
                    <select name="orgCodeConditional" value="${orgCodeConditional}">
                        <c:forEach var="conditional" items="${equalityConditionals}">
                            <option value="${conditional.value}" ${orgCodeConditional == conditional.value ? 'selected':''}>${conditional.display}</option>
                        </c:forEach>
                    </select>
                </div>
                <div>
                    <input name="orgCodeValue" value="${orgCodeValue}"/>
                </div>
            </div>

            <!-- Appointment Type -->
            <div class="bulk-conditionals">
                <div>
                    Appointment Type
                </div>
                <div>
                    <select name="appointmentTypeConditional" value="${appointmentTypeConditional}">
                        <c:forEach var="conditional" items="${equalityConditionals}">
                            <option value="${conditional.value}" ${appointmentTypeConditional == conditional.value ? 'selected':''}>${conditional.display}</option>
                        </c:forEach>
                    </select>
                </div>
                <div>
                    <input name="appointmentTypeValue" value="${appointmentTypeValue}"/>
                </div>
            </div>

            <!-- Active appraisals -->
            <div>
                Active Appraisals
            </div>
            <div>
                <input name="activeAppraisalsValue" type="checkbox" ${activeAppraisalsValue == 'true' ? 'checked':''}/>
                <input name="activeAppraisalsConditional" type="hidden" value="in"/>
            </div>

            <input type="submit" value="Filter"/>
        </form>
    </div>

    <!-- Updates section -->
    <div>
        <h3>Update</h3>

        <form action="<portlet:actionURL windowState="<%= WindowState.MAXIMIZED.toString() %>">
            <portlet:param name="action" value="update"/>
            <portlet:param name="controller" value="BulkUpdateAction"/>
            </portlet:actionURL>" id="<portlet:namespace />fm" name="<portlet:namespace />fm" method="post">
            <div>
                Status
            </div>
            <div>
                <select name="statusUpdate" value="${statusUpdate}">
                    <c:forEach var="option" items="${statusOptions}">
                        <option value="${option}" ${statusUpdate == option ? 'selected':''}>${option}</option>
                    </c:forEach>
                </select>
            </div>

            <div>
                Opt Out
            </div>
            <div>
                <select name="optOutUpdate" value="${optOutUpdate}">
                    <option value=""/>
                    <option value="EVAL" ${optOutUpdate == 'EVAL' ? 'selected':''}>System</option>
                </select>
                <select name="optOutBoolean" value="${optOutBoolean}">
                    <option value=""/>
                    <option value="On" ${optOutUpdate == 'On' ? 'selected':''}>On</option>
                    <option value="Off" ${optOutUpdate == 'Off' ? 'selected':''}>Off</option>
                </select>
            </div>

            <input type="submit" value="Update"/>
        </form>
    </div>

    <div class="bulk-update-result bulk-update-result-header">
        <div>Name</div>
        <div>Status</div>
        <div>Date Range</div>
        <div>Job</div>
        <div>Org Code</div>
        <div>Appointment Type</div>
        <div>Type</div>
    </div>
    <c:forEach var="appraisal" items="${appraisals}">
        <div class="bulk-update-result">
            <div>
                <c:out value="${appraisal.job.employee.firstName}"/> <c:out value="${appraisal.job.employee.lastName}"/>
            </div>
            <div>
                <c:out value="${appraisal.status}"/>
            </div>
            <div>
                <c:out value="${appraisal.startDate}"/> - <c:out value="${appraisal.endDate}"/>
            </div>
            <div>
                <c:out value="${appraisal.job.employee.id}"/> <c:out value="${appraisal.job.positionNumber}"/>-<c:out value="${appraisal.job.suffix}"/>
            </div>
            <div>
                <c:out value="${appraisal.job.orgCodeDescription}"/>
            </div>
            <div>
                <c:out value="${appraisal.job.appointmentType}"/>
            </div>
            <div>
                <c:out value="${appraisal.type}"/>
            </div>
        </div>
    </c:forEach>
</div>

<%@ include file="/jsp/footer.jsp" %>
