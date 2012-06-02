<?php
$db=new SQLiteDatabase("muachung.sqlite");
$db->query("BEGIN;
CREATE TABLE nextrun ( 
    next INTEGER 
); 
CREATE TABLE loantin ( 
    userid    INTEGER,
    loantinid INTEGER,
    PRIMARY KEY ( userid, loantinid ) 
);
COMMIT;");

$sql = "BEGIN;
	INSERT INTO nextrun (next) VALUES(0);
	COMMIT;";
$db->query($sql);

?>
