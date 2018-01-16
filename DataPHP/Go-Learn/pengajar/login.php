<?php

require_once 'include/DB_Functions.php';
$db = new DB_Functions();
$response = array("error" => FALSE);

if (isset($_POST['email']) && isset($_POST['password'])) {

    $email = $_POST['email'];
    $password = $_POST['password'];

    $user = $db->getPengajarByEmailAndPassword($email, $password);

    if ($user != false) {
        $response["error"] = FALSE;
        $response["uid"] = $user["unique_id"];
        $response["user"]["name"] = $user["name"];
        $response["user"]["email"] = $user["email"];
        $response["user"]["phone"] = $user["phone"];
        $response["user"]["gender"] = $user["gender"];
        $response["user"]["address"] = $user["address"];
        $response["user"]["created_at"] = $user["created_at"];
        $response["user"]["updated_at"] = $user["updated_at"];
        $response["user"]["pelajaran"] = $user["pelajaran"];
        $response["user"]["status"] = $user["status"];
        $response["user"]["foto"] = $user["foto"];
        $response["user"]["description"] = $user["description"];
        echo json_encode($response);
    } else {
        $response["error"] = TRUE;
        $response["error_msg"] = "Login gagal. Username atau password salah!";
        echo json_encode($response);
    }
} else {
    $response["error"] = TRUE;
    $response["error_msg"] = "Required parameters email or password is missing!";
    echo json_encode($response);
}
?>

