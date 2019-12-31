jQuery(document).ready(function() {
  console.log('eyo');

  jQuery("#<portlet:namespace />fm.appraisalDue, #<portlet:namespace />fm.appraisalOverdue, #<portlet:namespace />fm.releaseDue, #<portlet:namespace />fm.releaseOverdue").submit(function(){
    console.log('submit');
  });
});
