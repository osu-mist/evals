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
      },
      fail: function(msg) {
        console.log("fail");
        console.log(msg);
      },
      done: function(msg) {
        console.log("done");
        console.log(msg);
      }
    });
  });

  jQuery("#addAppraisal").click(function(event) {
    console.log('onClick');

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
      },
      fail: function(msg) {
        console.log("fail");
        console.log(msg);
      },
      done: function(msg) {
        console.log("done");
        console.log(msg);
      }
    });
  });
});
