<%@ include file="/jsp/init.jsp"%>
<c:set var="sectionName" value=""/>

<h2><liferay-ui:message key="configurations-list-title"/></h2>
<div class="separator"></div>

<c:forEach var="configuration" items="${configurations}" varStatus="loopStatus">
    <c:if test="${configuration.section != sectionName}">
        <c:if test="${sectionName != ''}">
            </table> <!-- end of configuration section -->
            <br />
        </c:if>

        <c:set var="sectionName" value="${configuration.section}"/>
        

        <table class="taglib-search-iterator configuration-list">
            <caption><liferay-ui:message key="${configuration.section}"/></caption>
            <thead>
                <tr class="portlet-section-header results-header">
                    <th><liferay-ui:message key="configurations-setting"/></th>
                    <th><liferay-ui:message key="description"/></th>
                    <th><liferay-ui:message key="value"/></th>
                    <th><liferay-ui:message key="actions"/></th>
                </tr>
            </thead>
    </c:if>
    
    <tr class="${loopStatus.index % 2 == 0 ? 'portlet-section-body results-row' : 'portlet-section-alternate results-row alt'}"
        id="<portlet:namespace/>users-${configuration.id}"
    >
        <td><strong><liferay-ui:message key="parameters-${configuration.name}"/></strong></td>
        <td><liferay-ui:message key="parameters-${configuration.name}-description"/></td>
        <td>
            <div id="<portlet:namespace/>displayValue${configuration.id}">${configuration.value}</div>
            <div id="<portlet:namespace/>form${configuration.id}" style="display:none;">
            <form onSubmit="saveConfig(this.<portlet:namespace/>configId${configuration.id}.value, this.<portlet:namespace/>configValue${configuration.id}.value);return false;">
                <input type="hidden" name="<portlet:namespace/>configId${configuration.id}"
                    id="<portlet:namespace/>configId${configuration.id}" value="${configuration.id}">
                <input name="<portlet:namespace/>configValue${configuration.id}"
                    id="<portlet:namespace/>configValue${configuration.id}" value="${configuration.value}"
                    type="text"/>
                <input type="submit" value="Save">
                <input type="submit" value="Cancel" onclick="toggleForm(${configuration.id}); return false;">
            </form>
            </div>
        </td>
        <td>
            <a id="<portlet:namespace/>editAction${configuration.id}"
            href="#" onclick="toggleForm(${configuration.id});">
            <liferay-ui:message key="edit"/></a>
        </td>
    </tr>
</c:forEach>

<portlet:resourceURL var="editAJAXURL" id="editConfiguration" escapeXml="false" />
<script type="text/javascript">
    function saveConfig(settingId, settingValue) {
        var querystring = {'id': settingId, 'value': settingValue};
        jQuery.ajax({
            type: "POST",
            url: "<%=renderResponse.encodeURL(editAJAXURL.toString())%>",
            data: querystring,
            success: function(msg) {
                if (msg == "success") {
                  jQuery("#<portlet:namespace/>displayValue"+settingId).html(settingValue);
                  toggleForm(settingId);
                  jQuery("#<portlet:namespace />flash").html(
                    '<span class="portlet-msg-success"><liferay-ui:message key="configurations-updated"/></span>'
                  );
                } else {
                  jQuery("#<portlet:namespace />flash").html(
                    '<span class="portlet-msg-error"><ul>'+msg+'</ul></span>'
                  );
                }
            }
        });
    }

    function toggleForm(settingId) {
        jQuery("#<portlet:namespace/>displayValue"+settingId).toggle();
        jQuery("#<portlet:namespace/>form"+settingId).toggle();
        jQuery("#<portlet:namespace/>editAction"+settingId).toggle();
        if (jQuery("#<portlet:namespace/>form"+settingId).is(":visible")) {
            jQuery("#<portlet:namespace/>configValue"+settingId).focus();
        }
        return false;
    }
</script>
<%@ include file="/jsp/footer.jsp" %>