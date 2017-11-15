<?php
/*$filename = 'file.txt';
$word = "ฤใบรฃก\n";

$fh = fopen($filename, "a");
fwrite($fh, $word);
fclose($fh);


$find = "ฤใบร3";
$data = file_get_contents("file.txt");
echo strpos($data, $find) === FALSE ? 0 : 1;

*/
$filename = 'www888aaaa3333ppp.txt';
$action=$_POST['action'];


if($action=="reg"){

$nickName=$_POST['nickName'];
$username=$_POST['username'];
$password=$_POST['password'];
$passworda=$_POST['passworda'];




	if($nickName==""||$username==""||$password==""){
	header("Location:reg.php?t=error"); 
	exit;
	}
	
	if($passworda!=$password){
	header("Location:reg.php?t=error"); 
	exit;
	}
	

$shuju="|NC:$nickName|YHM:$username|MM:$password \n";

$txt = fopen($filename, "a");
fwrite($txt, $shuju);
fclose($txt);

session_start(); 
$_SESSION['name']=$nickName;
header("Location:pay.php");  



}else if($action=="login"){



$username=$_POST['j_username'];
$password=$_POST['password'];
//echo $password;

$find = "YHM:$username|MM:$password";
//echo $find;
$data = file_get_contents($filename);
//echo strpos($data, $find) === FALSE ? 0 : 1;

session_start(); 

if(strpos($data, $find)==true){
	$_SESSION['name']=$username;
	echo $_SESSION['name'];
	header("Location:pay.php");
	
}else{
	header("Location:index.php?t=error"); 

}


}


?>