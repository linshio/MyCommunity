$(function(){
	$("#publishBtn").click(publish);
});

function publish() {
	$("#publishModal").modal("hide");

	// 获取到标题和内容
	var title =  $("#recipient-name").val();
	var content =  $("#message-text").val();
	console.log("title==>"+title);
	console.log("content==>"+content);
	//发送异步的请求
	$.post(
		CONTEXT_PATH+"/discuss/add",
		{"title":title,"content":content},
		function (data){
			data = $.parseJSON(data);
			//在提示框中显示后端的消息
			$("#hintBody").text(data.message);
			//显示提示框
			$("#hintModal").modal("show");
			//两秒后自动隐藏提示框
			setTimeout(function(){
				$("#hintModal").modal("hide");
				//刷新页面
				if (data.code==200){
					window.location.reload();
				}
			}, 2000);
		}
	);
}