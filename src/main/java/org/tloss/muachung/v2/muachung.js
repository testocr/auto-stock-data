function checkLoginSucess(data) {
	var obj = eval("(" + data + ')');
	return obj.err == 0 && obj.msg == 'success';
}
function checkLoantinSucess(data) {
	var obj = eval("(" + data + ')');
	return obj.err == 0 && obj.msg == 'done';
}
