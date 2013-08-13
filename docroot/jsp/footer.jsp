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
    function <portlet:namespace/>confirmBox(buttonText) {
        var question_suffix = " " + buttonText + "?";
        return confirm("<liferay-ui:message key="appraisal-confirm-message" />" + question_suffix);
    }
    jQuery(document).ready(function() {
        // Handle submit buttons that need confirmation
        jQuery("input.evals-show-confirm").click(function(event) {
            var buttonText = jQuery(this).val();
            // for submit goals and approve goals, returns true after removing empty goals
            if (buttonText == 'Submit Goals' || buttonText == 'Approve Goals') {
                if (window.readyToSubmit) {
                    return <portlet:namespace/>confirmBox(buttonText);
                }
                return false;
            }
            // if a previous js action aborted the click, don't show the confirm box
            if (event.isDefaultPrevented != undefined && event.isDefaultPrevented == true) {
                return false;
            }

            return <portlet:namespace/>confirmBox(buttonText);
        });

        // Handle the links in action menu bar that need confirmation
        jQuery("span.evals-show-confirm a").click(function() {
            var buttonText = "";
            if (jQuery(this).children()[0] != undefined) {
                buttonText = jQuery(this).children("img").attr("alt");
            } else {
                buttonText = jQuery(this).html();
            }

            var response = <portlet:namespace/>confirmBox(buttonText);
            if (response) {
                Liferay.Util.forcePost(this);
                return false;
            }
            return false;
        });
        jQuery("span.evals-show-confirm a").attr("onClick","");
    });
</script>