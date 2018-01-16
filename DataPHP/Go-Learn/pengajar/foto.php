<?php
if($_SERVER['REQUEST_METHOD']=='GET'){
	$email = $_GET['email'];
	$sql = "select * from pengajar where email = '$email'";
	require_once('koneksi.php');
	 
	$r = mysqli_query($con,$sql);
	$result = mysqli_fetch_array($r);
	 
	header('content-type: image/jpeg');
	echo base64_decode($result['foto']);
	mysqli_close($con);
	 
 }else{
 	echo "Error";
 }