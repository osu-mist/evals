jQuery(document).ready(function() {
  console.log('eyo');

  jQuery("#testForm").submit(function(event) {
    console.log('submit');

    jQuery.ajax({
      type: "GET",
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
