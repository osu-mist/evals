<%
PortletURL criteriaListURL = renderResponse.createRenderURL();
criteriaListURL.setWindowState(WindowState.NORMAL);
criteriaListURL.setParameter("action", "listCriteria");
%>


<div id="<portlet:namespace/>accordionMenuPassAdmin" class="accordion-menu">
    <div class="accordion-header" onclick="<portlet:namespace/>toggleContent('<portlet:namespace/>passAdmin');">
        <table>
            <tr>
                <td align='left'><h2 class="accordion-header-left"></h2></td>
                <td align='left' class="accordion-header-middle">
                    <span class="accordion-header-content" id="<portlet:namespace/>_header_1">
                        &nbsp;&nbsp;<img id="<portlet:namespace/>passAdminImageToggle" src="/cps/images/accordion/accordion_arrow_down.png"/>
                    </span>
                    <span class="accordion-header-content">PASS Administration</span>
                </td>
                <td align='right'><h2 class="accordion-header-right"></h2></td>
            </tr>
        </table>
    </div>
    <div class="accordion-content" id="<portlet:namespace/>passAdmin" style="display: block;">
        <ul class="pass-menu-list">
            <li><liferay-ui:icon
                    image="page"
                    message="Evaluation Criteria"
                    url="<%= criteriaListURL.toString() %>"
                    method="get"
                />
                <a href="<%= criteriaListURL.toString() %>">Evaluation Criteria</a>
            </li>
        </ul>
    </div>
</div>

<script type="text/javascript">
    function <portlet:namespace/>toggleContent(id){
            var imgId=id+'ImageToggle';

             if(document.getElementById(id).style.display=='block'){

                var path1 = new String('/cps/images/accordion/accordion_arrow_up.png');
                document.getElementById(imgId).src = path1;
                jQuery('#' + id).hide('slow');
            }else if(document.getElementById(id).style.display=='none'){
                var path2 = new String('/cps/images/accordion/accordion_arrow_down.png');
                document.getElementById(imgId).src = path2;
                jQuery('#' + id).show('slow');
            }
        }

</script>