<?php 

	include_once "koneksi.php";
	$nama = $_POST['keyword'];
	$pelajaran = $_POST['pelajaran'];
	$query = mysqli_query($con, "SELECT * FROM pengajar WHERE pelajaran = '$pelajaran' AND name LIKE '%".$nama."%'");

	$num_rows = mysqli_num_rows($query);

	if ($num_rows > 0){
		$json = '{"value":1, "results": [';

		while ($row = mysqli_fetch_array($query)){
			$char ='"';

			$json .= '{
				"id": "'.str_replace($char,'`',strip_tags($row['id'])).'", 
				"name": "'.str_replace($char,'`',strip_tags($row['name'])).'",
				"email": "'.str_replace($char,'`',strip_tags($row['email'])).'",
				"phone": "'.str_replace($char,'`',strip_tags($row['phone'])).'",
				"gender": "'.str_replace($char,'`',strip_tags($row['gender'])).'",
				"address": "'.str_replace($char,'`',strip_tags($row['address'])).'",
				"pelajaran": "'.str_replace($char,'`',strip_tags($row['pelajaran'])).'",
				"description": "'.str_replace($char,'`',strip_tags($row['description'])).'"
			},';
		}

		$json = substr($json,0,strlen($json)-1);
		
		$json .= ']}';

	} else {
		$json = '{"value":0, "message": "Data tidak ditemukan."}';
	}

	echo $json;

	mysqli_close($con);
?>