<?php
set_time_limit(4*60);
include_once 'account.php';
$MAX_COUNT_SUCCESS=10;
function randomFloat($min = 0, $max = 1) {
	return $min + mt_rand() / mt_getrandmax() * ($max - $min);
}
function startsWith($haystack, $needle){
	return strpos($haystack, $needle) === 0;
}
function getMoney(&$doc){
	$xpath = new DOMXpath($doc);
	$elements = $xpath->query("//b[@id='customer_gold']");
	$money ="";
	if (!is_null($elements)) {
		foreach ($elements as $element) {
			$money = $element->nodeValue;
		}
	}
	return $money;
}
function initCommonCURL(&$ch){
	curl_setopt($ch, CURLOPT_USERAGENT, "Mozilla/5.0 (Windows; U; Windows NT 5.1; rv:1.7.3) Gecko/20041001 Firefox/0.10.1");
	curl_setopt($ch, CURLOPT_HEADER, FALSE);
	curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
	curl_setopt($ch, CURLOPT_COOKIEJAR, "cookie.txt");
	curl_setopt($ch, CURLOPT_COOKIEFILE, "cookie.txt");
	curl_setopt($ch, CURLOPT_FRESH_CONNECT, true);

}
function login(&$ch,$username,$password){
	curl_setopt($ch, CURLOPT_URL,"http://muachung.vn//ajax.php?act=customer&code=login");
	initCommonCURL($ch);
	$params =  array("email"=>$username,"pass"=>$password,"save"=>"off","rand"=>randomFloat());
	curl_setopt($ch,CURLOPT_POST,count($params));
	$fields_string="";
	foreach($params as $key=>$value) { $fields_string .= $key.'='.urlencode($value).'&'; }
	rtrim($fields_string,'&');
	curl_setopt( $ch, CURLOPT_POSTFIELDS, $fields_string );
	$output = curl_exec($ch);
	echo $output."\n";
	return startsWith($output, '{"err":0,"msg":"success"');
}
function loantin(&$ch,$url,&$startURLList,&$countSuccess,$MAX_COUNT_SUCCESS,$index,&$db){
	if($countSuccess>=$MAX_COUNT_SUCCESS){
		return true;
	}
	echo $url."\n";
	$id = $url;
	if(!checkVisited($db, $index, $id)){
		curl_setopt($ch, CURLOPT_URL,"http://muachung.vn/ajax.php?act=connect&mccode=check-loantin");
		initCommonCURL($ch);
		$params =  array("item_id"=>$id,"yahoo"=>"1","facebook"=>"0","content"=>"","rand"=>randomFloat());
		curl_setopt($ch,CURLOPT_POST,count($params));
		$fields_string="";
		foreach($params as $key=>$value) { $fields_string .= $key.'='.urlencode($value).'&'; }
		rtrim($fields_string,'&');
		curl_setopt( $ch, CURLOPT_POSTFIELDS, $fields_string );
		$output = curl_exec($ch);
		echo "id:".$id.", result:" .$output."\n";
		if('{"err":-1,"msg":"done"}'== trim($output)){
			visited($db, $index, $id);
		}
		if(startsWith(trim($output), '{"err":0,"msg":"done"')){
			visited($db, $index, $id);
			$countSuccess = $countSuccess +1;
			if($countSuccess>=$MAX_COUNT_SUCCESS){
				return true;
			}
		}
		if('{"err":-1,"msg":"not_login"}'== trim($output)){
			return true;
		}
	}
	return false;
}
function logout(&$ch){
	curl_setopt($ch, CURLOPT_URL,"http://muachung.vn/ajax.php?act=customer&code=logout");
	initCommonCURL($ch);
	$params =  array("rand"=>randomFloat());
	curl_setopt($ch,CURLOPT_POST,count($params));
	$fields_string="";
	foreach($params as $key=>$value) { $fields_string .= $key.'='.urlencode($value).'&'; }
	rtrim($fields_string,'&');
	curl_setopt( $ch, CURLOPT_POSTFIELDS, $fields_string );
	$output = curl_exec($ch);
	echo "logout result: ".$output."\n";
	return startsWith($output, '{"err":0,"msg":"success"');
}
function checkVisited(&$db,$userid,$loantinId){
	$result=$db->query("SELECT * FROM loantin where userid=".$userid." and loantinid=".$loantinId);
	$rrow =null;
	while($result->valid()) {
		//	fetch current row
		$row=$result->current();
		$rrow = $row;
		// move pointer to next row
		$result->next();
	}
	if(is_null($rrow)){
		return  false;
	}
	return true;
}
function visited(&$db,$userid,$loantinId){
	$sql = "BEGIN;
			INSERT INTO   loantin (userid,loantinid) VALUES(".$userid.",".$loantinId.");
			COMMIT;";
	$db->query($sql);
}
function loadURL(&$loantinIdList,$url,&$ch,$prefix){
	curl_setopt($ch, CURLOPT_URL,$url);
	curl_setopt($ch, CURLOPT_USERAGENT, "Mozilla/5.0 (Windows; U; Windows NT 5.1; rv:1.7.3) Gecko/20041001 Firefox/0.10.1");
	curl_setopt($ch, CURLOPT_HEADER, FALSE);
	curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
	curl_setopt($ch, CURLOPT_COOKIEJAR, $prefix ."_cookie.txt");
	curl_setopt($ch, CURLOPT_COOKIEFILE, $prefix ."_cookie.txt");
	curl_setopt($ch, CURLOPT_FRESH_CONNECT, true);
	$output = curl_exec($ch);
	$doc = new DOMDocument();
	if(@$doc->loadHTML($output)){
		$xpath = new DOMXpath($doc);
		$elements = $xpath->query("//a[@class='SellingDetail']/@href");
		if (!is_null($elements)) {
			foreach ($elements as $element) {
				$url = $element->nodeValue;
				$matches = array();
				preg_match('/\/p-\d+\//',$url, $matches, PREG_OFFSET_CAPTURE);
				if(count($matches)>0){
					$id = $matches[0][0];
					$id = substr($id,3);
					$id = substr($id, 0,strlen($id)-1);
					if(!in_array($id, $loantinIdList)){
						$loantinIdList[] =$id;
					}
				}
			}
		}
	}
	return $doc;
}
function initURL(&$startURLList,&$loantinIdList,&$city){
	while(count($city)>0){
		$prefix=array_shift($city);
		$ch = curl_init();
		$doc = loadURL($loantinIdList,"http://muachung.vn/danh-muc/c-999999997/deal-dang-ban.html",$ch,$prefix);
		$xpath = new DOMXpath($doc);
		$elements = $xpath->query("//a[@class='newButton mLeft5']/@href");
		if (!is_null($elements)) {
			foreach ($elements as $element) {
				$url = $element->nodeValue;
				echo "collection :" .$url."\n";
				loadURL($loantinIdList, $url,$ch,$prefix);

			}
		}
		curl_close($ch);
	}
	//loadDulich($loantinIdList);
}
function loadDulich(&$loantinIdList){
	//http://muachung.vn/san-pham-du-lich.html;
	$ch = curl_init();
	curl_setopt($ch, CURLOPT_URL,"http://muachung.vn/san-pham-du-lich.html");
	initCommonCURL($ch);
	$output = curl_exec($ch);
	$doc = new DOMDocument();
	if(@$doc->loadHTML($output)){
		$xpath = new DOMXpath($doc);
		$elements = $xpath->query("//div[@class='titleFirst' or @class='tit']/a/@href");
		if (!is_null($elements)) {
			foreach ($elements as $element) {
				$url = $element->nodeValue;
				$matches = array();
				preg_match('/\/p-\d+\//',$url, $matches, PREG_OFFSET_CAPTURE);
				if(count($matches)>0){
					$id = $matches[0][0];
					$id = substr($id,3);
					$id = substr($id, 0,strlen($id)-1);
					if(!in_array($id, $loantinIdList)){
						$loantinIdList[] =$id;
					}
				}
			}
		}
	}
	curl_close($ch);
}
/*--------------------------------------------------------------------------*/
/*--------------------------START PROGAM------------------------------------*/
/*--------------------------------------------------------------------------*/
$city = array(29,22,15,68,26,47,67,14);
$startURLList=array('http://muachung.vn/tp-ho-chi-minh','http://muachung.vn/ha-noi','http://muachung.vn/da-nang','http://muachung.vn/nha-trang',
'http://muachung.vn/hai-phong','http://muachung.vn/quang-ninh','http://muachung.vn/vung-tau','http://muachung.vn/can-tho');
$ch = curl_init();
$countSuccess = 0;
$old_countSuccess = 0;
$db=new SQLiteDatabase("muachung.sqlite");
$result=$db->query("SELECT * FROM nextrun");
$index =null;
while($result->valid()) {
	//	fetch current row
	$row=$result->current();
	$index = $row['next'];
	// move pointer to next row
	$result->next();
}
echo $ACCOUNT[$index]."\n";
if(!is_null($index)){
	$loantinIdList = array();
	initURL($startURLList, $loantinIdList,$city);
	$stop =  false;

	if(login($ch, $ACCOUNT[$index], $PASSWORD)){
		//initURL($startURLList, $loantinIdList,$city);
		$nextIndex = $index +1;
		if($nextIndex ==  count($ACCOUNT)){
			$nextIndex =0;
		}
		$sql = "BEGIN;
			UPDATE  nextrun set next=".$nextIndex.";
			COMMIT;";
		$db->query($sql);
		while(!$stop && count($loantinIdList)>0 ){
			$url =array_shift($loantinIdList);
			$stop = loantin($ch, $url, $startURLList,$countSuccess,$MAX_COUNT_SUCCESS,$index,$db);
		}
		logout($ch);
	}



}
?>