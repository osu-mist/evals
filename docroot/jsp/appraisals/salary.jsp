<fieldset class="recommended-salary">
    <legend><liferay-ui:message key="appraisal-salary-section-title"/></legend>
    <table>
        <thead>
            <tr class="results-header">
                <th><liferay-ui:message key="appraisal-salary-eligibility-date"/></th>
                <th><liferay-ui:message key="appraisal-salary-current"/></th>
                <th><liferay-ui:message key="appraisal-salary-control-point"/></th>
                <th><liferay-ui:message key="appraisal-salary-control-point-value"/></th>
                <th><liferay-ui:message key="appraisal-salary-control-high"/></th>
                <th><liferay-ui:message key="appraisal-salary-recommended-increase"/></th>
                <th><liferay-ui:message key="appraisal-salary-after-increase"/></th>
            </tr>
        </thead>
        <tbody>
            <tr class="results-row">
                <%--remove the year and verify it's correct--%>
                <td><fmt:formatDate value="${appraisal.salaryEligibilityDate}" pattern="MM/dd"/></td>
                <td><fmt:formatNumber type="currency" value="${appraisal.salary.current}"/></td>
                <td><liferay-ui:message key="appraisal-salary-above"/>
                    <span class="control-point">
                        <c:if test="${appraisal.salary.current >= appraisal.salary.midPoint}">
                            X
                        </c:if>&nbsp;
                    </span>
                    <br />

                    <liferay-ui:message key="appraisal-salary-below"/>
                    <span class="control-point">
                        <c:if test="${appraisal.salary.current < appraisal.salary.midPoint}">
                            X
                        </c:if>&nbsp;
                    </span>
                </td>
                <td><fmt:formatNumber type="currency" value="${appraisal.salary.midPoint}"/></td>
                <td><fmt:formatNumber type="currency" value="${appraisal.salary.high}"/></td>
                <td><input type="text" id="<portlet:namespace />appraisal.salary.increase"
                           name="<portlet:namespace />appraisal.salary.increase"
                           value="<c:out value="${appraisal.salary.increase}"/>"/>%</td>
                <td></td>
            </tr>
        </tbody>
    </table>
</fieldset>