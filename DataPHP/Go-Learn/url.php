<?php 
$email = $_GET['email'];
$URL = 'intent:#Intent;action=com.androidsrc.launchfrombrowser;category=android.intent.category.DEFAULT;category=android.intent.category.BROWSABLE;S.msg_from_browser='.$email.';end';
header('Location: '.$URL);
?>

