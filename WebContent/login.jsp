<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>

<!DOCTYPE html>
<html>
  <head>
    <meta http-equiv="Content-type" content="text/html; charset=utf-8">
    <title>Login to COMBO</title>
    <link href="assets/css/combo_bootstrap.css" rel="stylesheet"  type="text/css" media="screen">
    <link	href="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8/themes/base/jquery-ui.css" rel="stylesheet" type="text/css" />
  </head>

<body>
  <div class="navbar navbar-fixed-top">
  <div class="navbar-inner">
    <div class="container">
    <a class="btn btn-navbar" data-toggle="collapse"
					data-target=".nav-collapse"> <span class="icon-bar"></span> </a> <a
        class="brand" href="/">The Cure</a>
      </div>
  </div>
  </div>

  <div class="container">
		<div class="hero-unit">
			<div class="row">
				<div class="span12">
					<div id="login">
						<div id="olduser">
							<strong>Enter your credentials here</strong>

              <form action="./checkuser.jsp">
                Username: <input id="usernameinput" type="text" name="username" /><br>
                Password: <input id="passwordinput" type="password" name="password" /><br>
                <input type="submit" value="Submit" />
              </form>
							or
							<div id="newuserlink">
								<a href="">New player? click here.</a>
							</div>
						</div>
						<div id="newuser">
							<div id="reg" class="span8">
								<p>
									<strong>You must have an account to play so that we
										can reward you properly. <br />Don’t worry, we won’t spam you.
									</strong>
								</p>
								<form id="newuser" action="./checkuser.jsp">
									* Username: <input id="usernameinput" type="text"
										name="username" /><br> Password: <input
										id="passwordinput" type="password" name="password" /><br>
									Email address: <input id="emailinput" type="text" name="email" /><br>
									Most recently awarded academic degree: <select name="degree" width="10">
										<option value="none">None</option>
										<option value="bachelors">Bachelors</option>
										<option value="masters">Masters</option>
										<option value="phd">Ph.D.</option>
										<option value="md">M.D.</option>
										<option value="other">other</option>
									</select><br/> 
									Do you consider yourself knowledgeable about cancer biology?: <select
										name="cancer">
										<option value="no" selected>No</option>
										<option value="yes">Yes</option>
									</select><br/>
									 Do you consider yourself a biologist?: <select
										name="biologist">
										<option value="no" selected>No</option>
										<option value="yes">Yes</option>
									</select> <input type="hidden" name="newuser" value="1" /> 
									<br/><input
										type="submit" value="Submit" />
								</form>
							</div>
							<div id="message" class="span2">
								<p>* We suggest an anonymous handle like ‘shamu76’, as this
									will be associated with your game play and will be visible to
									other players in the game.</p>
							</div>
						</div>
						<br />
					</div>
				</div>
			</div>
		</div>
  </div>

<% //-- Moved JS to end of body faster load times %>
<script src="js/libs/jquery-1.8.0.min.js"></script>
<script src="js/libs/jquery-ui-1.8.0.min.js"></script>
<script>
	function clearInput() {
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
<jsp:include page="js/analytics.js" />
</body>
</html>
