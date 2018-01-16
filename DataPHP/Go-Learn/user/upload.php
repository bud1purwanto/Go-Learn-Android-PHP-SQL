<?php

require_once 'include/DB_Functions.php';
$db = new DB_Functions();
$response = array("error" => FALSE);

if (isset($_POST['file'])) {
    $nama_file = $_FILES['uploaded_file']['name'];

    $user = $db->upload($nama_file);
    if ($user) {
        $response["error"] = FALSE;
        $response["user"]["nama_file"] = $user["nama_file"];  
        echo json_encode($response);
    } else {
        $response["error"] = TRUE;
        $response["error_msg"] = "File error!";
        echo json_encode($response);
    }
} else {
    $response["error"] = TRUE;
    $response["error_msg"] = "Required parameters (file) is missing!";
    echo json_encode($response);
}
?>

