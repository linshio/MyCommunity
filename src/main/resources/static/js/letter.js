$(function(){
	$("#sendBtn").click(send_letter);
	$(".close").click(delete_msg);
});

function send_letter() {
	$("#sendModal").modal("hide");

	var toName = $("#recipient-name").val();
	var content = $("#message-text").val();

	$.post(
		CONTEXT_PATH+"/letter/send",
		{
			"toName":toName,
			"content":content
		},
		function (data){
			data = $.parseJSON(data);
			if (data.code==200){
				$("#hintBody").text("发送成功");
			}else {
				$("#hintBody").text(data.message);
			}

			$("#hintModal").modal("show");
			setTimeout(function(){
				$("#hintModal").modal("hide");
				//刷新当前的页面
				location.reload();
			}, 2000);
		}
	)


}

function delete_msg() {
	$.post(
		CONTEXT_PATH+"/letter/delete",
		{
			"id":$("#letterId").val()
		},
		function (data){
			data = $.parseJSON(data);
			if (data.code==200){
				alert(data.message)
				location.reload();
			}else {
				alert(data.message)
			}
		}
	)
}