jQuery(document).ready(function() {
  console.log('eyo');

  try{
  jQuery("#submitTest").submit(function(event) {
    console.log('submit');
  });
  } catch (err) {
    console.log(err);
  }
});
