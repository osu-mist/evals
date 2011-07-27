<div id="<portlet:namespace/>accordionMenuPassHelpLinks" class="accordion-menu">
    <div class="accordion-header" onclick="<portlet:namespace/>toggleContent('<portlet:namespace/>passHelpLinks');">
        <table>
            <tr>
                <td align='left'><h2 class="accordion-header-left"></h2></td>
                <td align='left' class="accordion-header-middle">
                    <span class="accordion-header-content" id="<portlet:namespace/>_header_1">
                        &nbsp;&nbsp;<img id="<portlet:namespace/>passHelpLinksImageToggle" src="/cps/images/accordion/accordion_arrow_down.png"/>
                    </span>
                    <span class="accordion-header-content"><liferay-ui:message key="helpful-links"/></span>
                </td>
                <td align='right'><h2 class="accordion-header-right"></h2></td>
            </tr>
        </table>
    </div>
    <div class="accordion-content" id="<portlet:namespace/>passHelpLinks" style="display: block;">
        <ul class="pass-menu-list">
            <c:forEach var="helpLink" items="${helpLinks}" varStatus="loopStatus">
            <li>${helpLink}</li>
            </c:forEach>
        </ul>
    </div>
</div>