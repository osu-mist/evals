<div id="<portlet:namespace/>accordionMenuSearch" class="accordion-menu">
    <div class="accordion-header" onclick="<portlet:namespace/>toggleContent('<portlet:namespace/>Search');">
        <table>
            <tr>
                <td align='left'><h2 class="accordion-header-left"></h2></td>
                <td align='left' class="accordion-header-middle">
                    <span class="accordion-header-content" id="<portlet:namespace/>_header_1">
                        &nbsp;&nbsp;<img id="<portlet:namespace/>SearchImageToggle" src="/cps/images/accordion/accordion_arrow_down.png"/>
                    </span>
                    <span class="accordion-header-content"><liferay-ui:message key="search" /></span>
                </td>
                <td align='right'><h2 class="accordion-header-right"></h2></td>
            </tr>
        </table>
    </div>
    <div class="accordion-content pass-search" id="<portlet:namespace/>Search" style="display: block;">
        <form action="<portlet:actionURL windowState="<%= WindowState.MAXIMIZED.toString() %>">
            <portlet:param name="action" value="searchAppraisals" />
            </portlet:actionURL>" method="post">

            <label for="<portlet:namespace/>osuid"><liferay-ui:message key="search-employee-osuid"/></label>
            <input type="text" id="<portlet:namespace/>osuid" name="<portlet:namespace/>osuid" />
            <input type="submit" value="<liferay-ui:message key="search"/>"/>
        </form>
    </div>
</div>