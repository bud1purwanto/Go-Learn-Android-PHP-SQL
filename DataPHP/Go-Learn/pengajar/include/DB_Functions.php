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


    public function storePengajar($name, $email, $phone, $gender, $address, $password, $pelajaran) {
        $uuid = uniqid('', true);
        $hash = $this->hashSSHA($password);
        $encrypted_password = $hash["encrypted"]; 
        $salt = $hash["salt"]; 

        $stmt = $this->conn->prepare("INSERT INTO pengajar (unique_id, name, email, phone, gender, address, encrypted_password, salt, created_at, pelajaran, status) VALUES(?, ?, ?, ?, ?, ?, ?, ?, NOW(), ?, 'pengajar')");
        $stmt->bind_param("sssssssss", $uuid, $name, $email, $phone, $gender, $address, $encrypted_password, $salt, $pelajaran);
        $result = $stmt->execute();
        $stmt->close();


        if ($result) {
            $stmt = $this->conn->prepare("SELECT * FROM pengajar WHERE email = ?");
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
    public function getPengajarByEmailAndPassword($email, $password) {

        $stmt = $this->conn->prepare("SELECT * FROM pengajar WHERE email = ?");

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
    public function isPengajarExisted($email) {
        $stmt = $this->conn->prepare("SELECT email from pengajar WHERE email = ?");

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
