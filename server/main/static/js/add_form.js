function checkType(){
    var fields = $("#id_project, #id_billed, #id_bill_image");
    var type = $('#id_type').val();
    var fields_parents = fields.parent().parent();
    if (type==0){
        fields_parents.show();
    }
    else{
        fields_parents.hide();
        fields.val(false);
    }
}
checkType();

$("#id_type").change(function(){
    checkType();
});
