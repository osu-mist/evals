</div> <!-- closes div.osu-cws -->

<div id="accessible-errors" aria-live="assertive" aria-role="alert"></div>

<script src="https://ajax.googleapis.com/ajax/libs/jquery/2.1.1/jquery.min.js"></script>
<script type="text/javascript">
    function <portlet:namespace/>toggleContent(id){
        var imgPath = '/o/evals-portlet/images/accordion/accordion_arrow_up.png';
        if(jQuery('#'+id).is(":visible")){
            var imgPath = new String('/o/evals-portlet/images/accordion/accordion_arrow_down.png');
        }

        document.getElementById(id+'ImageToggle').src = imgPath;
        jQuery('#' + id).toggle('slow');
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
        // There's no way to figure out via js what button was clicked. Add an attribute to track
        jQuery("#<portlet:namespace />fm input[type=submit]").click(function() {
            jQuery("input[type=submit]", jQuery(this).parents("form")).removeAttr("clicked");
            jQuery(this).attr("clicked", "true");
        });

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
        jQuery("span.evals-show-confirm a").click(function(event) {
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

            event.preventDefault();
            event.stopImmediatePropagation();
            return false;
        });
        jQuery("span.evals-show-confirm a").attr("onClick","");
    });

    /**
     * Format value into currency with two decimals and the $ sign.
     *
     * @param value
     * @returns {string}
     */
    function formatCurrency(value) {
        var fmtValue = value.toString().replace(/(\d)(?=(\d{3})+(?!\d))/g, "$1,");
        return '$' + fmtValue;
    }
</script>
