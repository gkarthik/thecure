<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>

<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-type" content="text/html; charset=utf-8">
<title>Forgot password</title>
<link rel="stylesheet" href="assets/css/combo_bootstrap.css"
	type="text/css" media="screen">

<jsp:include page="js/analytics.js" />
</head>
<body>
	<div class="navbar navbar-fixed-top">
		<div class="navbar-inner">
			<div class="container">
				<a class="btn btn-navbar" data-toggle="collapse"
					data-target=".nav-collapse"> <span class="icon-bar"></span> </a> <a class="brand" href="/">The Cure</a>				
					<div class="nav-collapse">
					<ul class="nav">
						<li><a
							href="contact.jsp">Contact</a>
						</li>

					</ul>
				</div>
			
			</div>
		</div>
	</div>
	<div class="container">
		<div class="hero-unit">
			<div class="row">
				<div class="span12">
					<div id="login">
						<div id="olduser">
							<form action="/SocialServer?command=iforgot">
								Enter the email address associated with your account: <input type="text" name="mail" /><br>
								<input type="submit" value="Submit" />
							</form>							
						</div>						
					</div>
				</div>
			</div>
		</div>
	</div>
</body>
</html>