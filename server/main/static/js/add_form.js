function checkBilled(){
    var field = $("#id_bill_image");
    var checkbox = $('#id_billed');
    var fieldParents = field.parent().parent();
    if (checkbox.prop("checked")==true){
        fieldParents.show();
    }
    else{
        fieldParents.hide();
        fieldParents.val(false);
    }
}
checkBilled();

$("#id_billed").change(function(){
    checkBilled();
});
