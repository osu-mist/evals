jQuery(document).ready(function() {
  console.log('eyo');

  jQuery("#addAppraisal").click(function(event) {
    console.log('onClick');

    var data = {};
    jQuery.ajax({
      type: "POST",
      url: "<%=renderResponse.encodeURL(saveDraftAJAXURL.toString())%>",
      data: data,
      success: function(msg) {
        console.log("we have success");
        console.log(msg);

        $('#evaluations').load('/jsp/home/testTools.jsp', function() {
          console.log("loading testTools");
        }).fadeIn("slow");

        jQuery('.portlet-msg-success').remove();

        jQuery("#<portlet:namespace />flash").html(
          '<span class="portlet-msg-success"><liferay-ui:message key="Appraisal Created"/></span>'
        );

        jQuery('.portlet-msg-success').fadeIn('slow');
        setTimeout(function() {jQuery('.portlet-msg-success').fadeOut('slow')}, 30000);
      },
      fail: function(msg) {
        console.log("fail");
        console.log(msg);
      }
    });
  });
});
