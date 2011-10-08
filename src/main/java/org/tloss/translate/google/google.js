function getTranslateString(data){
	var result = new java.lang.StringBuffer();
	var obj = eval("(" + data + ')');
	for(x in obj[0]){
		result.append(obj[0][x][0]);
	}
	return result.toString();
}