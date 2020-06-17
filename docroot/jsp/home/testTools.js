jQuery(document).ready(function() {
  console.log('eyo');

  jQuery("#showCreateEmployee").click(function(event) {
    $("#dialog").dialog();
  });

  jQuery("#createEmployee").click(function(event) {
    console.log($("#admin").is(":checked"));
    jQuery.ajax({
      type: "POST",
      url: "<%=renderResponse.encodeURL(createEmployeeAction.toString())%>",
      data: {
        firstName: $("#firstName").val(),
        lastName: $("#lastName").val(),
        email: $("#email").val(),
        onid: $("#onid").val(),
        appointmentType: $("#appointmentType").val(),
        admin: $("#admin").is(":checked"),
      },
      success: function(msg) {
        console.log(msg);
      },
      fail: function(msg) {
        console.log(msg);
      },
    });
  });

  jQuery("#addAppraisal").click(function(event) {
    var data = {};
    jQuery.ajax({
      type: "POST",
      url: "<%=renderResponse.encodeURL(createAppraisalAction.toString())%>",
      data: data,
      success: function(msg) {
        console.log("we have success");
        console.log(msg);

        /*jQuery('#evaluations').html(
          '<span class="portlet-msg-success"><liferay-ui:message key="Appraisal Created"/></span>'
        );*/
        $("#evaluations").load(location.href+" #evaluations>*","");

        // jQuery('.portlet-msg-success').remove();

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

  jQuery("#deleteAppraisal").click(function(event) {
    console.log("delete clicked");
    console.log(event);

  });
});
