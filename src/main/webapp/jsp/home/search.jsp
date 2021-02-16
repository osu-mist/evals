<div id="<portlet:namespace/>accordionMenuSearch" class="accordion-menu">
    <div class="osu-accordion-header" onclick="<portlet:namespace/>toggleContent('<portlet:namespace/>Search');">
      <img id="<portlet:namespace/>SearchImageToggle" src="/o/evals-portlet/images/accordion/accordion_arrow_up.png"/>
      <liferay-ui:message key="search" />
    </div>
    <div class="accordion-content pass-search" id="<portlet:namespace/>Search" style="display: block;">
        <form action="<portlet:actionURL windowState="<%= WindowState.MAXIMIZED.toString() %>">
            <portlet:param name="action" value="search" />
            <portlet:param name="controller" value="AppraisalsAction" />
            </portlet:actionURL>" method="post">

            <label for="<portlet:namespace/>searchTerm"><liferay-ui:message key="search-employee-searchTerm"/></label>
            <input type="text" id="<portlet:namespace/>searchTerm" class="inline narrow" name="<portlet:namespace/>searchTerm" />
            <input type="submit" value="<liferay-ui:message key="search"/>"/>
        </form>
    </div>
</div>