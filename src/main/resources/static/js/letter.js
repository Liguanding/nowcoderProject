$(function(){
	$("#sendBtn").click(send_letter);
	$(".close").click(delete_msg);
});

function send_letter() {
	$("#sendModal").modal("hide")

	var toName = $("#recipient-name").val();
	var content = $("#message-text").val();
	$.ajax({
		url: CONTEXT_PATH + "/letter/send",
		type: "post",
		dataType: "json",
		data: {
			toName:toName,
			content:content
		},
		success:function (data){
			if(data.code == 1){
				$("#hintBody").text(data.msg);
			}else if(data.code == 0){
				$("#hintBody").text("发送成功!");
			}

			$("#hintModal").modal("show");

			setTimeout(function(){
				$("#hintModal").modal("hide");
				location.reload();
			}, 2000);
		}
	})


	$("#hintModal").modal("show");
	setTimeout(function(){
		$("#hintModal").modal("hide");
	}, 2000);
}

function delete_msg() {
	// TODO 删除数据
	$(this).parents(".media").remove();
}