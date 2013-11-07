<fieldset class="recommended-salary">
    <legend><liferay-ui:message key="appraisal-salary-section-title"/></legend>
    <table>
        <thead>
            <tr class="results-header">
                <th><liferay-ui:message key="appraisal-salary-current"/></th>
                <th><liferay-ui:message key="appraisal-salary-control-point-value"/></th>
                <th><liferay-ui:message key="appraisal-salary-control-low"/></th>
                <th><liferay-ui:message key="appraisal-salary-control-high"/></th>
                <th><liferay-ui:message key="appraisal-salary-control-point"/></th>
                <th><liferay-ui:message key="appraisal-salary-recommended-increase"/></th>
                <th><liferay-ui:message key="appraisal-salary-after-increase"/></th>
                <th><liferay-ui:message key="appraisal-salary-eligibility-date"/></th>
            </tr>
        </thead>
        <tbody>
            <tr class="results-row">
                <td><fmt:formatNumber type="currency" value="${appraisal.salary.current}"/></td>
                <td><fmt:formatNumber type="currency" value="${appraisal.salary.midPoint}"/></td>
                <td><fmt:formatNumber type="currency" value="${appraisal.salary.low}"/></td>
                <td><fmt:formatNumber type="currency" value="${appraisal.salary.high}"/></td>
                <td>
                    <c:if test="${appraisal.salary.current > appraisal.salary.midPoint}">
                        <liferay-ui:message key="appraisal-salary-above"/>
                    </c:if>
                    <c:if test="${appraisal.salary.current < appraisal.salary.midPoint}">
                        <liferay-ui:message key="appraisal-salary-below"/>
                    </c:if>
                    <c:if test="${appraisal.salary.current == appraisal.salary.midPoint}">
                        <liferay-ui:message key="appraisal-salary-at"/>
                    </c:if>
                </td>
                <td><input type="text" id="<portlet:namespace />appraisal.salary.increase"
                           name="<portlet:namespace />appraisal.salary.increase"
                           value="<c:out value="${appraisal.salary.increase}"/>"
                           class="recommended-salary" disabled="" readonly="" maxlength="3"/>
                    <span class="recommended-salary-hint"></span>
                </td>
                <td class="salary-after-increase"></td>
                <td><fmt:formatDate value="${appraisal.salaryEligibilityDate}" pattern="MM/dd"/></td>
            </tr>
        </tbody>
    </table>
    <div class="salary-comment"></div>
</fieldset>

<script type="text/javascript">
    jQuery(document).ready(function() {
        // if a salary increase is present in db, we want to only reset the html properties
        var resetSalaryValue = true;
        // if no salary is saved in db, we want to reset the html and value as well.
        <c:if test="${appraisal.salary.increase != null}">
            resetSalaryValue = false;
        </c:if>

        // Only make the increase textarea editable when the evaluation is editable.
        <c:if test="${permissionRule.evaluation == 'e'}">
            resetRecommendedIncrease(resetSalaryValue);
        </c:if>
        // set the salary increase so the salary table gets populated correctly.
        setSalaryIncrease();

        /**
         * Resets the recommended increase value, by enabling or disabling the textbox depending
         * on the selected rating. It also sets the default recommended increase value based on the
         * rating.
         */
        function resetRecommendedIncrease(resetValue) {
            // reset any increase range hints if present
            jQuery('.recommended-salary-hint').html('');

            // rating value that the user selected
            var rating = jQuery('input:radio[name=<portlet:namespace />appraisal.rating]:checked').val();

             // set the hard coded increase to 0. If the rating is 2 or 1 (and not at top pay range)
             // , we'll change the value
            var increase = 0; // default value for rating 3
            var isSalaryAtHighPoint = ${appraisal.salary.current} >= ${appraisal.salary.high};

            // If the supervisor rates a 1, we only enable the input box if current salary is not at top range
            if (!isSalaryAtHighPoint) {
                if (rating == 1) {
                    if (resetValue) {
                        jQuery(".osu-cws input.recommended-salary").val('');
                    }
                    jQuery(".osu-cws input.recommended-salary").removeAttr('disabled');
                    jQuery(".osu-cws input.recommended-salary").removeAttr('readonly');

                    // set the range hint
                    var hint = '(<liferay-ui:message key="appraisal-salary-increase-hint-range"/> '
                            + ${increaseRate1MinVal} + ' - ' + ${increaseRate1MaxVal} + ')';
                    jQuery('.recommended-salary-hint').html(hint);
                } else if (rating == 2) {
                    increase = ${increaseRate2Value}; // disable the inputs as well if rating is 2 or 3
                }
            }

            // When the rating is 1 or 2, set the increase to 0 if the salary is at or above the control point high.
            if (isSalaryAtHighPoint && (rating == 1 || rating == 2 || rating == undefined)) {
                jQuery(".osu-cws input.recommended-salary").val('${increaseRate2Value}');
                jQuery(".osu-cws .salary-comment").text('<liferay-ui:message key="appraisal-salary-already-high-point"/>');
                jQuery(".osu-cws input.recommended-salary").attr('disabled','true');
                jQuery(".osu-cws input.recommended-salary").attr('readonly','readonly');
            }

            if ((!isSalaryAtHighPoint && (rating == 2 || rating == 3)) || rating == undefined) {
                if (resetValue) {
                    jQuery(".osu-cws input.recommended-salary").val(increase);
                }
                jQuery(".osu-cws input.recommended-salary").attr('disabled','true');
                jQuery(".osu-cws input.recommended-salary").attr('readonly','readonly');
            }

        }

        /**
         * Calculates the salary after increase and returns the formatted string.
         *
         * @return {String}
         */
        function getSalaryAfterIncrease() {
            // calculate salary after increase
            var increasePercentage = jQuery(".osu-cws input.recommended-salary").val();
            if (!(increasePercentage >= 0 && increasePercentage <= 999)) {
                increasePercentage = 0;
            }
            var salaryAfterIncrease = ${appraisal.salary.current} * (1 + increasePercentage / 100);
            salaryAfterIncrease = salaryAfterIncrease.toFixed(2); // round to 2 decimals

            // format salary
            var fmtSalary = salaryAfterIncrease.toString().replace(/(\d)(?=(\d{3})+(?!\d))/g, "$1,");
            return '$' + fmtSalary;
        }

        /**
         * Sets the salary after increase in the html table.
         */
        function setSalaryIncrease() {
            var salaryAfterIncrease = getSalaryAfterIncrease();
            jQuery('.salary-after-increase').html(salaryAfterIncrease);
        }

        jQuery('.pass-evaluation input:radio').change(function() {
            resetRecommendedIncrease(true);
        });

        jQuery('.pass-evaluation input:radio').change(function() {
            setSalaryIncrease();
        });

        jQuery(".osu-cws input.recommended-salary").keyup(function() {
            setSalaryIncrease();
        });

        jQuery(".osu-cws input.recommended-salary").change(function() {
            var increaseValidationError = validateIncrease();
            if (increaseValidationError != '') {
                alert(increaseValidationError); // display error message
                jQuery(".osu-cws input.recommended-salary").val('');
                setSalaryIncrease();
                return false;
            }
            return true;
        });

    });

    /**
     * Validates the recommended increase selection whenever the value is changed.
     *
     * @return {String}
     */
    function validateIncrease() {
        var rating = jQuery('input:radio[name=<portlet:namespace />appraisal.rating]:checked').val();
        var increase = jQuery(".osu-cws input.recommended-salary").val();
        if (isNaN(increase)) {
            return '<liferay-ui:message key="appraisal-salary-increase-error-nan"/>';
        }

        // can't give raise if at the top of pay range
        if (${appraisal.salary.current} >= ${appraisal.salary.high}) {
            return '';
        }

        switch(rating) {
            case "1":
                if (increase < ${increaseRate1MinVal} || increase > ${increaseRate1MaxVal}) {
                    return '<liferay-ui:message key="appraisal-salary-increase-error-out-of-range"/>';
                }
                break;
            case "2":
                if (increase != ${increaseRate2Value}) {
                    return '<liferay-ui:message key="appraisal-salary-increase-error-invalid-change"/>';
                }
                break;
            default:
                if (increase != 0) {
                    return '<liferay-ui:message key="appraisal-salary-increase-error-invalid-change"/>';
                }
                break;
        }

        return '';
    }
</script>