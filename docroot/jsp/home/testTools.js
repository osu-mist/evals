jQuery(document).ready(function() {
  console.log('eyo');

  jQuery("#testForm").submit(function(event) {
    console.log('submit');

    var data = {};
    jQuery.ajax({
      type: "POST",
      url: "<%=renderResponse.encodeURL(saveDraftAJAXURL.toString())%>",
      data: data,
      success: function(msg) {
        if (msg == "success") {
          console.log("we have success");

          jQuery('.portlet-msg-success').remove();

          jQuery("#<portlet:namespace />flash").html(
            '<span class="portlet-msg-success"><liferay-ui:message key="Appraisal Created"/></span>'
          );
        } else {
          console.log(msg);
        }

        jQuery('.portlet-msg-success').fadeIn('slow');
        setTimeout(function() {jQuery('.portlet-msg-success').fadeOut('slow')}, 30000);
      }
    });
  });
});
