<div id="<portlet:namespace/>accordionMenuSearch" class="accordion-menu">
    <div class="osu-accordion-header" onclick="<portlet:namespace/>toggleContent('<portlet:namespace/>Search');">
      <img id="<portlet:namespace/>SearchImageToggle" src="/cps/images/accordion/accordion_arrow_down.png"/>
      <liferay-ui:message key="search" />
    </div>
    <div class="accordion-content pass-search" id="<portlet:namespace/>Search" style="display: block;">
        <form action="<portlet:actionURL windowState="<%= WindowState.MAXIMIZED.toString() %>">
            <portlet:param name="action" value="searchAppraisals" />
            </portlet:actionURL>" method="post">

            <label for="<portlet:namespace/>osuid"><liferay-ui:message key="search-employee-osuid"/></label>
            <input type="text" id="<portlet:namespace/>osuid" class="inline narrow" name="<portlet:namespace/>osuid" />
            <input type="submit" value="<liferay-ui:message key="search"/>"/>
        </form>
    </div>
</div>