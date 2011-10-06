<c:if test="${isReviewer == 'true'}">
<jsp:useBean id="appraisals" class="java.util.ArrayList" scope="request" />
    <div id="<portlet:namespace/>accordionMenuPendingReview" class="accordion-menu">
        <div class="osu-accordion-header" onclick="<portlet:namespace/>toggleContent('<portlet:namespace/>PendingReview');">
          <img id="<portlet:namespace/>PendingReviewImageToggle" src="/cps/images/accordion/accordion_arrow_up.png"/>
          <liferay-ui:message key="pending-reviews" />
        </div>
        <div class="accordion-content" id="<portlet:namespace/>PendingReview" style="display: block;">
            <c:if test="${!empty appraisals}">
                <table class="taglib-search-iterator narrow">
                    <thead>
                        <tr class="portlet-section-header results-header">
                            <th><liferay-ui:message key="name" /></th>
                            <th><liferay-ui:message key="type"/></th>
                            <th><liferay-ui:message key="reviewPeriod" /></th>
                            <th><liferay-ui:message key="status" /></th>
                        </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="shortAppraisal" items="${appraisals}" varStatus="loopStatus">
                        <tr class="${loopStatus.index % 2 == 0 ? 'portlet-section-body results-row' : 'portlet-section-alternate results-row alt'}"
                        >
                            <td>${shortAppraisal.job.employee.name}</td>
                            <td><liferay-ui:message key="appraisal-type-${shortAppraisal.type}"/></td>
                            <td>${shortAppraisal.reviewPeriod}
                            </td>
                            <td><a href="<portlet:actionURL windowState="<%= WindowState.MAXIMIZED.toString()%>">
                                <portlet:param name="id" value="${shortAppraisal.id}"/>
                                <portlet:param  name="action" value="displayAppraisal"/>
                               </portlet:actionURL>"><liferay-ui:message key="${shortAppraisal.viewStatus}" /></a>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>

                <a href="<portlet:renderURL windowState="<%= WindowState.MAXIMIZED.toString() %>">
                        <portlet:param name="action" value="displayReviewList"/>
                        </portlet:renderURL>"><liferay-ui:message key="pending-reviews-more"/></a>
            </c:if>
            <c:if test="${empty appraisals}">
                <p><em><liferay-ui:message key="no-pending-reviews" /></em></p>
            </c:if>
        </div>
    </div>
</c:if>