<?php
include_once 'utils.php';
function initCommonCURL(&$ch){
	curl_setopt($ch, CURLOPT_USERAGENT, "Mozilla/5.0 (Windows; U; Windows NT 5.1; rv:1.7.3) Gecko/20041001 Firefox/0.10.1");
	curl_setopt($ch, CURLOPT_HEADER, FALSE);
	curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
	curl_setopt($ch, CURLOPT_COOKIEJAR, "cookie.txt");
	curl_setopt($ch, CURLOPT_COOKIEFILE, "cookie.txt");
	curl_setopt($ch, CURLOPT_FRESH_CONNECT, true);

}
function get_now_for_one_top(){
	return (time()-(24 * 60 * 60))*1000;
}
function login(&$ch,$username,$password){
	curl_setopt($ch, CURLOPT_URL,"http://www.1top.vn/TPHoChiMinh");
	initCommonCURL($ch);
	$output = curl_exec($ch);
	curl_setopt($ch, CURLOPT_URL,"http://www.1top.vn/ajax/login_reg/cdt_login_form?_=". get_now_for_one_top());
	curl_setopt($ch,CURLOPT_REFERER,'http://www.1top.vn/TPHoChiMinh');
	initCommonCURL($ch);
	$output = curl_exec($ch);
	$doc = new DOMDocument();
	$cdt_csrf_token = null;
	if(@$doc->loadHTML($output)){
		$xpath = new DOMXpath($doc);
		$elements = $xpath->query("//input[@name='cdt_csrf_token']/@value");
		if (!is_null($elements)) {
			foreach ($elements as $element) {
				$cdt_csrf_token = $element->nodeValue;
			}
		}
	}
	curl_setopt($ch, CURLOPT_URL,"http://www.1top.vn/ajax/login_reg/action_login_cdt");
	initCommonCURL($ch);
	curl_setopt($ch,CURLOPT_REFERER,'http://www.1top.vn/TPHoChiMinh');
	$params =  array("cdt_csrf_token"=>$cdt_csrf_token,"form_name"=>"login_cdt","user_name"=>$username,"password"=>$password);
	curl_setopt($ch,CURLOPT_POST,TRUE);
	$fields_string="";
	foreach($params as $key=>$value) { $fields_string .= $key.'='.urlencode($value).'&'; }
	$fields_string = rtrim($fields_string,'&');
	curl_setopt( $ch, CURLOPT_POSTFIELDS, $fields_string );
	$output = curl_exec($ch);
	curl_setopt($ch, CURLOPT_URL,"http://www.1top.vn/TPHoChiMinh");
	initCommonCURL($ch);
	curl_exec($ch);
	return $output == "{\"code\":1}";
}
/*--------------------------------------------------------------------------*/
/*--------------------------START PROGAM------------------------------------*/
/*--------------------------------------------------------------------------*/
$ch = curl_init();
echo(login($ch,"myname74119","z712211z74119"));
