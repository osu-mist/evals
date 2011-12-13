</div> <!-- closes div.osu-cws -->

<script type="text/javascript">
    function <portlet:namespace/>toggleContent(id){
        var imgId=id+'ImageToggle';

            if(document.getElementById(id).style.display=='block'){
                var path1 = new String('/cps/images/accordion/accordion_arrow_down.png');
                document.getElementById(imgId).src = path1;
                jQuery('#' + id).hide('slow');
            } else if(document.getElementById(id).style.display=='none'){
                var path2 = new String('/cps/images/accordion/accordion_arrow_up.png');
                document.getElementById(imgId).src = path2;
                jQuery('#' + id).show('slow');
            }
        }

    /**
     * Handles displaying a confirmation pop-up box to the end user. Displays the
     * name of the pressed button along with the confirmation message.
     * @param button The button pressed by the end user
     */
    function <portlet:namespace/>confirmBox(button) {
        var question_suffix = " " + jQuery(button).val() + "?";
        return confirm("<liferay-ui:message key="appraisal-confirm-message" />" + question_suffix);
    }
    jQuery(document).ready(function() {
        // Handle submit buttons that need confirmation
        jQuery("input.evals-show-confirm").click(function() {
            return <portlet:namespace/>confirmBox(this);
        });
        // Handle buttons in action menu bar that need confirmation
        jQuery("span.evals-show-confirm a").click(function() {
            var response = <portlet:namespace/>confirmBox(this);
            if (response) {
                Liferay.Util.forcePost(this);
                return false;
            }
            return false;
        });
        jQuery("span.evals-show-confirm a").attr("onClick","");
    });
</script>