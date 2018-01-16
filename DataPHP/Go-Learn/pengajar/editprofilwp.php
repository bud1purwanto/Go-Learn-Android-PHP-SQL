<?php 
	if($_SERVER['REQUEST_METHOD']=='POST'){
		$email = $_POST['email'];
		$name = $_POST['name'];
		$phone = $_POST['phone'];
		$address = $_POST['address'];
		$gender = $_POST['gender'];
		$description = $_POST['description'];
		
		require_once('koneksi.php');

		$sql = "UPDATE pengajar SET name = '$name', phone = '$phone', address = '$address', gender = '$gender', updated_at = NOW(), description = '$description' WHERE email = '$email';";
		
		if(mysqli_query($con,$sql)){
			echo 'Profil sudah diupdate';
		}else{
			echo 'Ukuran gambar terlalu besar';
		}
		
		mysqli_close($con);
	}
?>