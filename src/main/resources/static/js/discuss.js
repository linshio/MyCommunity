//声明三个方法
$(function (){
   $("#topBtn").click(setTop);
   $("#wonderfulBtn").click(setWonderful);
   $("#deleteBtn").click(setDelete);
});

function like(btn,entityType,entityId,entityUserId,postId){
    $.post(
        CONTEXT_PATH + "/like",
        {"entityType":entityType,"entityId":entityId,"entityUserId":entityUserId,"postId":postId},
        function (data){
            data = $.parseJSON(data);
            if (data.code == 200){
                $(btn).children("i").text(data.likeCount);
                $(btn).children("b").text(data.likeStatus==1?'已赞':'赞');
            }else {
                alert(data.message);
            }
        }
    )
}

//置顶
function setTop(){
    $.post(
        CONTEXT_PATH + "/discuss/top",
        {"id":$("#postId").val()},
        function (data){
            data = $.parseJSON(data);
            if (data.code == 200){
                $("#topBtn").attr("disabled","disabled");
            }else {
                alert(data.message);
            }
        }
    )
}
//加精
function setWonderful(){
    $.post(
        CONTEXT_PATH + "/discuss/wonderful",
        {"id":$("#postId").val()},
        function (data){
            data = $.parseJSON(data);
            if (data.code == 200){
                $("#wonderfulBtn").attr("disabled","disabled");
            }else {
                alert(data.message);
            }
        }
    )
}
//拉黑
function setDelete(){
    $.post(
        CONTEXT_PATH + "/discuss/delete",
        {"id":$("#postId").val()},
        function (data){
            data = $.parseJSON(data);
            if (data.code == 200){
                location.href = CONTEXT_PATH + "/index";
            }else {
                alert(data.message);
            }
        }
    )
}