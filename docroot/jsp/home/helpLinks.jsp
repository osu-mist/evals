<div id="<portlet:namespace/>accordionMenuPassHelpLinks" class="accordion-menu">
    <div class="osu-accordion-header" onclick="<portlet:namespace/>toggleContent('<portlet:namespace/>passHelpLinks');">
      <img id="<portlet:namespace/>passHelpLinksImageToggle" src="/evals/images/accordion/accordion_arrow_up.png"/>
      Helpful Links
    </div>
    <div class="accordion-content" id="<portlet:namespace/>passHelpLinks" style="display: block;">
        <ul class="pass-menu-list">
            <c:forEach var="helpLink" items="${helpLinks}" varStatus="loopStatus">
            <li>${helpLink}</li>
            </c:forEach>
        </ul>
    </div>

    <div>
        <input type="button" class="cancel" value="im a button"/>
    </div>
</div>
