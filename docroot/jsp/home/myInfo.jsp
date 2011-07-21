<%@ include file="/jsp/init.jsp" %>

<h2><liferay-ui:message key="my-information"/></h2>

<div id="pass-my-info">
      
    <div class="section">
        <div class="col1">Name</div>
        <div class="col2">
            <c:out value="${employee.firstName}"/>
            <c:out value="${employee.middleName}"/>
            <c:out value="${employee.lastName}"/>
        </div>
    </div>
    <div class="section">
        <div class="col1"><liferay-ui:message key="email"/></div>
        <div class="col2"><c:out value="${employee.email}"/></div>
    </div>
    <hr />

    <h3><liferay-ui:message key="job-information"/></h3>
    <c:forEach var="job" items="${employee.jobs}" varStatus="loopStatus">
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
            <div class="col2"><fmt:formatDate value="${job.beginDate}" pattern="dd/MM/yy"/></div>
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

    <p><em><liferay-ui:message key="how-to-correct-job-info"/></em></p>
</div>
<%@ include file="/jsp/footer.jsp" %>