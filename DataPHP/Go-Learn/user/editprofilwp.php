<?php 
	if($_SERVER['REQUEST_METHOD']=='POST'){
		$email = $_POST['email'];
		$name = $_POST['name'];
		$phone = $_POST['phone'];
		$address = $_POST['address'];
		$gender = $_POST['gender'];
		
		require_once('koneksi.php');

		$sql = "UPDATE users SET name = '$name', phone = '$phone', address = '$address', gender = '$gender', updated_at = NOW() WHERE email = '$email';";
		
		if(mysqli_query($con,$sql)){
			echo 'Profil sudah diupdate';
		}else{
			echo 'Ukuran gambar terlalu besar';
		}
		
		mysqli_close($con);
	}
?>