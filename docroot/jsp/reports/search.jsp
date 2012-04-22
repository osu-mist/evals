<form id="<portlet:namespace/>searchForm" action="<portlet:actionURL windowState="<%= WindowState.MAXIMIZED.toString() %>">
    <portlet:param name="action" value="report" />
    <portlet:param name="controller" value="ReportsAction" />
    <portlet:param name="requestBreadcrumbs" value="${breadcrumbsWithRootOnly}" />
    </portlet:actionURL>" method="post">

    <c:set var="searchTip" value="report-search-tip-default"/>
    <c:if test="${isSupervisor}">
        <c:set var="searchTip" value="report-search-tip-supervisor"/>
    </c:if>
    <label class="pass-hide" for="<portlet:namespace/>searchTerm"><liferay-ui:message key="search-employee-searchTerm"/></label>
    <input type="text" id="<portlet:namespace/>searchTerm" class="inline narrow"
           value="<liferay-ui:message key="${searchTip}"/>"
           name="<portlet:namespace/>searchTerm" />
    <input type="submit" id="#<portlet:namespace/>searchSubmit"
           value="<liferay-ui:message key="search"/>"/>
</form>

<script type="text/javascript">
jQuery(document).ready(function() {
  jQuery("#<portlet:namespace/>searchTerm").click(function(e) {
    clearSearchBox();
  });

  jQuery("#<portlet:namespace/>searchTerm").focus(function(e) {
    clearSearchBox();
  });

  jQuery("#<portlet:namespace/>searchForm").submit(function() {
      return validateSearch();
  });

});

function clearSearchBox() {
    jQuery("#<portlet:namespace/>searchTerm").val("");
}

function validateSearch() {
  var errors = "";
  var searchTerm = jQuery("#<portlet:namespace/>searchTerm").val();
  <c:choose>
    <c:when test="${isAdmin || isReviewer}">
      var errorString = "${searchJsErrorDefault}";
    </c:when>
    <c:otherwise>
      var errorString = "${searchJsErrorSupervisor}";
    </c:otherwise>
  </c:choose>
  errorString = "<li>"+ errorString + "</li>";

  if (searchTerm == "") {
    errors = errorString;
  } else {
    var isNumber = false;
    if (parseFloat(searchTerm) == parseInt(searchTerm)) {
      isNumber = true;
    }

    var searchTermLength = searchTerm.length;
    if (isNumber && (searchTermLength != 9 && searchTermLength != 6)) {
        errors = errorString;
    }
  }

  if (errors != "") {
    jQuery("#<portlet:namespace />flash").html(
      '<span class="portlet-msg-error"><ul>'+errors+'</ul></span>'
    );
    return false;
  }
  return true;
}

</script>