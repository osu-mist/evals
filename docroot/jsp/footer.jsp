</div> <!-- closes div.osu-cws -->

<script type="text/javascript">
    function <portlet:namespace/>toggleContent(id){
            var imgId=id+'ImageToggle';

             if(document.getElementById(id).style.display=='block'){

                var path1 = new String('/cps/images/accordion/accordion_arrow_up.png');
                document.getElementById(imgId).src = path1;
                jQuery('#' + id).hide('slow');
            }else if(document.getElementById(id).style.display=='none'){
                var path2 = new String('/cps/images/accordion/accordion_arrow_down.png');
                document.getElementById(imgId).src = path2;
                jQuery('#' + id).show('slow');
            }
        }
</script>