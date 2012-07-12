<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>


<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-type" content="text/html; charset=utf-8">
<title>Login to COMBO</title>
<link rel="stylesheet" href="assets/css/combo_bootstrap.css" type="text/css" media="screen">
	<link href="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8/themes/base/jquery-ui.css" rel="stylesheet" type="text/css" />
	<script src="//ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js" type="text/javascript"></script>
	<script	src="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8/jquery-ui.min.js"></script>
<script>
function clearInput(){
	$('input[type=text]').focus(function() {
		$(this).val('');
	    });		
}


$(document).ready(function() {
	//start with new user area hidden
	$("#newuser").hide();
	//show on click
	$("#newuserlink").click(function(e) {
		e.preventDefault();
		$("#newuser").show();
		$("#olduser").hide();
	    });	
	});	
	</script>
</head>
<body>
	<div class="navbar navbar-fixed-top">
      <div class="navbar-inner">
        <div class="container">
          <a class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
          </a>
          <a class="brand" href="/combo/">COMBO</a>
      </div>
    </div>
	</div>
  <div class="container">
    <div class="hero-unit">
    <div class="row">
    <div class="span8">
		<div id="login">
			<div id="olduser">
			 <strong>Enter your credentials here</strong> 
			<form action="./checkuser.jsp">
				Username: <input id="usernameinput" type="text" name="username" /><br>
				Password: <input id="passwordinput" type="password" name="password" /><br>
				<input	type="submit" value="Submit" /> 
				
			</form>			
			or
			<form action="./checkuser.jsp">
				<input id="usernameinput" type="hidden" name="username" value="anonymous_hero" />
				<input id="passwordinput" type="hidden" name="password" value="123"/>
				<input	type="submit" value="Play as anonymous_hero" />
			</form>
			or
			 <div id="newuserlink"><a href="">New player? click here.</a></div>
			</div>
			 <div id="newuser">
			 <strong>Enter new account details</strong> 
			 <form id="newuser" action="./checkuser.jsp">
				Username: <input id="usernameinput" type="text" name="username" /><br>
				Password: <input id="passwordinput" type="password" name="password" /><br>
				Email address: <input id="emailinput" type="text" name="email" /><br>
				<input type="hidden" name="newuser" value="1"/>
				<input	type="submit" value="Submit" />
			</form>
			</div>
		</div>
		<br/>
	</div>
</div>
</div>
</div>

</body>
</html>