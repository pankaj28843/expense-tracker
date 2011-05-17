function checkType(){
    var fields = $("#id_project, #id_billed, #id_bill_image");
    var type = $('#id_type').val();
    var fields_parents = fields.parent().parent();
    if (type=='Official'){
        fields_parents.show();
    }
    else{
        fields_parents.hide();
    }
}
checkType();

$("#id_type").change(function(){
    checkType();
});
