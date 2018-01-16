<?php

require_once 'include/DB_Functions.php';
$db = new DB_Functions();
$response = array("error" => FALSE);

if (isset($_POST['name']) && isset($_POST['email']) && isset($_POST['phone']) && isset($_POST['gender']) && isset($_POST['address']) && isset($_POST['password']) && isset($_POST['pelajaran'])) {
    $name = $_POST['name'];
    $email = $_POST['email'];
    $phone = $_POST['phone'];
    $gender = $_POST['gender'];
    $address = $_POST['address'];
    $password = $_POST['password'];
    $pelajaran = $_POST['pelajaran'];

    if ($db->isPengajarExisted($email)) {
        $response["error"] = TRUE;
        $response["error_msg"] = "Teacher already existed with " . $email;
        echo json_encode($response);
    } else {
        $user = $db->storePengajar($name, $email, $phone, $gender, $address, $password, $pelajaran);
        if ($user) {
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
            echo json_encode($response);
        } else {
            $response["error"] = TRUE;
            $response["error_msg"] = "Unknown error occurred in registration!";
            echo json_encode($response);
        }
    }
} else {
    $response["error"] = TRUE;
    $response["error_msg"] = "Required parameters (name, email or password) is missing!";
    echo json_encode($response);
}
?>

