<%@ include file="/jsp/init.jsp" %>

<h2><liferay-ui:message key="my-information"/></h2>

<div id="pass-my-info">
    <h3>Contact Information</h3>  
    <div class="section">
        <div class="col1">Name</div>
        <div class="col2">
            <c:out value="${employee.name}"/>
        </div>
    </div>
    <div class="section">
        <div class="col1"><liferay-ui:message key="email"/></div>
        <div class="col2"><c:out value="${employee.email}"/></div>
    </div>
    <hr />

    <h3><liferay-ui:message key="job-information"/></h3>
    <c:forEach var="job" items="${employee.nonTerminatedJobs}" varStatus="loopStatus">
        <div class="section">
            <div class="col1"><liferay-ui:message key="position-no"/></div>
            <div class="col2"><c:out value="${job.positionNumber}"/></div>
        </div>   
         
        <div class="section">    
            <div class="col1"><liferay-ui:message key="jobTitle"/></div>
            <div class="col2"><c:out value="${job.jobTitle}"/></div>
        </div>  
          
        <div class="section">    
            <div class="col1"><liferay-ui:message key="supervisor"/></div>
            <div class="col2"><c:out value="${job.supervisor.employee.name}"/></div>
        </div>
            
        <div class="section">    
            <div class="col1"><liferay-ui:message key="job-start-date"/></div>
            <div class="col2"><fmt:formatDate value="${job.beginDate}" pattern="MM/dd/yy"/></div>
        </div>
        
        <div class="section">
            <div class="col1"><liferay-ui:message key="appointment-type"/></div>
            <div class="col2"><c:out value="${job.appointmentType}"/></div>
        </div>
        
        <div class="section">
            <div class="col1"><liferay-ui:message key="status"/></div>
            <div class="col2"><liferay-ui:message key="job-status-${job.status}"/></div>
        </div>    
        <hr />
    </c:forEach>

    <p><a href="https://hr.oregonstate.edu/about-us/contact-information-hr-teams"><liferay-ui:message key="how-to-correct-job-info"/></a></p>
</div>
<%@ include file="/jsp/footer.jsp" %>
