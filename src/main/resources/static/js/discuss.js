$(function(){
    $("#topBtn").click(setTop);
    $("#wonderfulBtn").click(setWonderful);
    $("#deleteBtn").click(setDelete);
});

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

// 置顶
function setTop() {

    $.ajax({
        url:CONTEXT_PATH + "/discuss/top",
        type:"post",
        dataType:"json",
        data:{
            "id":$("#postId").val()
        },
        success:function (data){
            if(data.code == 0){
                $("#topBtn").attr("disabled", "disabled");
            }else {
                alert(data.msg);
            }
        }
    });
}

// 加精
function setWonderful() {
    $.ajax({
        url:CONTEXT_PATH + "/discuss/wonderful",
        type:"post",
        dataType:"json",
        data:{
            "id":$("#postId").val()
        },
        success:function (data){
            if(data.code == 0){
                $("#wonderfulBtn").attr("disabled", "disabled");
            }else {
                alert(data.msg);
            }
        }
    });
}

// 删除
function setDelete() {

    $.ajax({
        url:CONTEXT_PATH + "/discuss/delete",
        type:"post",
        dataType:"json",
        data:{
            "id":$("#postId").val()
        },
        success:function (data){
            if(data.code == 0){
                location.href = CONTEXT_PATH + "/index";
            }else {
                alert(data.msg);
            }
        }
    });
}

/*// 置顶
function setTop() {
    $.post(
        CONTEXT_PATH + "/discuss/top",
        {"id":$("#postId").val()},
        function(data) {
            data = $.parseJSON(data);
            if(data.code == 0) {
                $("#topBtn").attr("disabled", "disabled");
            } else {
                alert(data.msg);
            }
        }
    );
}

// 加精
function setWonderful() {
    $.post(
        CONTEXT_PATH + "/discuss/wonderful",
        {"id":$("#postId").val()},
        function(data) {
            data = $.parseJSON(data);
            if(data.code == 0) {
                $("#wonderfulBtn").attr("disabled", "disabled");
            } else {
                alert(data.msg);
            }
        }
    );
}

// 删除
function setDelete() {
    $.post(
        CONTEXT_PATH + "/discuss/delete",
        {"id":$("#postId").val()},
        function(data) {
            data = $.parseJSON(data);
            if(data.code == 0) {
                location.href = CONTEXT_PATH + "/index";
            } else {
                alert(data.msg);
            }
        }
    );
}*/
