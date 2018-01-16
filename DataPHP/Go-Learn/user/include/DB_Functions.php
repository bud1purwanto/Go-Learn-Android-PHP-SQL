<?php

class DB_Functions {

    private $conn;
    function __construct() {
        require_once 'DB_Connect.php';
        $db = new Db_Connect();
        $this->conn = $db->connect();
    }

    function __destruct() {
        
    }

    /**
     * Storing new user
     * returns user details
     */

    public function upload($nama_file)
    {
        $folder = "upload/";

        $file_path = $folder . basename($_FILES['uploaded_file']['name']);

        if (move_uploaded_file($_FILES['uploaded_file']['tmp_name'], $file_path)) {
            $stmt = $this->$conn->query("INSERT INTO file(file) VALUES(?)") or die(mysqli_error());
            $stmt->bind_param("", $nama_file);
            $result = $stmt->execute();
            $stmt->close();
            echo "success";
        } else {
            echo "fail";
        }
    }

    public function storeUser($name, $email, $phone, $gender, $address, $password) {
        $uuid = uniqid('', true);
        $hash = $this->hashSSHA($password);
        $encrypted_password = $hash["encrypted"]; 
        $salt = $hash["salt"]; 

        $stmt = $this->conn->prepare("INSERT INTO users(unique_id, name, email, phone, gender, address, encrypted_password, salt, created_at, status) VALUES(?, ?, ?, ?, ?, ?, ?, ?, NOW(), 'user')");
        $stmt->bind_param("ssssssss", $uuid, $name, $email, $phone, $gender, $address, $encrypted_password, $salt);
        $result = $stmt->execute();
        $stmt->close();


        if ($result) {
            $stmt = $this->conn->prepare("SELECT * FROM users WHERE email = ?");
            $stmt->bind_param("s", $email);
            $stmt->execute();
            $user = $stmt->get_result()->fetch_assoc();
            $stmt->close();

            return $user;
        } else {
            return false;
        }
    }

    /**
     * Get user by email and password
     */
    public function getUserByEmailAndPassword($email, $password) {

        $stmt = $this->conn->prepare("SELECT * FROM users WHERE email = ?");

        $stmt->bind_param("s", $email);

        if ($stmt->execute()) {
            $user = $stmt->get_result()->fetch_assoc();
            $stmt->close();

            // verifying user password
            $salt = $user['salt'];
            $encrypted_password = $user['encrypted_password'];
            $hash = $this->checkhashSSHA($salt, $password);
            // check for password equality
            if ($encrypted_password == $hash) {
                // user authentication details are correct
                return $user;
            }
        } else {
            return NULL;
        }
    }

    /**
     * Check user is existed or not
     */
    public function isUserExisted($email) {
        $stmt = $this->conn->prepare("SELECT email from users WHERE email = ?");

        $stmt->bind_param("s", $email);

        $stmt->execute();

        $stmt->store_result();

        if ($stmt->num_rows > 0) {
            $stmt->close();
            return true;
        } else {
            $stmt->close();
            return false;
        }
    }

    /**
     * Encrypting password
     * @param password
     * returns salt and encrypted password
     */
    public function hashSSHA($password) {

        $salt = sha1(rand());
        $salt = substr($salt, 0, 10);
        $encrypted = base64_encode(sha1($password . $salt, true) . $salt);
        $hash = array("salt" => $salt, "encrypted" => $encrypted);
        return $hash;
    }

    /**
     * Decrypting password
     * @param salt, password
     * returns hash string
     */
    public function checkhashSSHA($salt, $password) {

        $hash = base64_encode(sha1($password . $salt, true) . $salt);

        return $hash;
    }

}

?>
