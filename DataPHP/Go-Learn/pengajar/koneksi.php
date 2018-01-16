<?php

 define('HOST','localhost');
 define('USER','root');
 define('PASS','mgma');
 define('DB','go_learn');

 $con = mysqli_connect(HOST,USER,PASS,DB) or die('Unable to Connect');
 ?>