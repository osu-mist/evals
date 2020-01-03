jQuery(document).ready(function() {
  console.log('eyo');

  jQuery("#testForm").submit(function(event) {
    console.log('submit');

    var data = {};
    // data.controller = "AppraisalsAction";
    // data.action = "updateTest";
    jQuery.ajax({
      type: "POST",
      url: "<%=renderResponse.encodeURL(saveDraftAJAXURL.toString())%>",
      data: data,
      success: function(msg) {
        if (msg == "success") {
          console.log("we have success");
        } else {
          console.log(msg);
        }
      }
    });
  });
});
