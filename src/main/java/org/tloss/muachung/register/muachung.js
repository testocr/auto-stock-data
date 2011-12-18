function checkcreateEmailSuccess(data) {
	var obj = eval("(" + data + ')');
	return obj.cpanelresult.event.result == 1;
}
function checkcreateAccountSuccess(data) {
	var obj = eval("(" + data + ')');
	return obj.err == 0;
}