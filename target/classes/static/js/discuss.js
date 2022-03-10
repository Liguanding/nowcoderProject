function like(btn,entityType,entityId,entityUserId,postId){
    $.ajax({
        url:CONTEXT_PATH + "/like",
        type:"post",
        dataType:"json",
        data:{
            "entityType":entityType,
            "entityId":entityId,
            "entityUserId":entityUserId,
            "postId":postId
        },
        success:function (data){
            if(data.code == 0){
                $(btn).children("i").text(data.likeCount);
                $(btn).children("b").text(data.likeStatus==1?'已赞':"赞");
            }else {
                alert(data.msg);
            }
        }
    });
}
