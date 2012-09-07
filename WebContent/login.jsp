<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>

<%
String bad = request.getParameter("bad");
%>

<!DOCTYPE html>
<html>
  <head>
    <meta http-equiv="Content-type" content="text/html; charset=utf-8">
    <title>Login to COMBO</title>
    <link href="assets/css/bootstrap.css" rel="stylesheet"  type="text/css" media="screen">
    <link	href="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8/themes/base/jquery-ui.css" rel="stylesheet" type="text/css" />
    <link href="assets/css/style.css" rel="stylesheet" type="text/css">
   </head>

<body>

  <div class="navbar navbar-fixed-top">
		<div class="navbar-inner">
			<div class="container">
				<a class="btn btn-navbar" data-toggle="collapse"
					data-target=".nav-collapse"> <span class="icon-bar"></span> </a> <a
					class="brand" href="/cure/">The Cure</a>
			</div>
		</div>
	</div>

  <div class="container">
		<div class="hero-unit">
			<div class="row">
				<div class="span6 offset2">
					<div id="login">
						<div id="olduser">
							<strong>Enter your credentials here</strong>

              <form action="./checkuser.jsp">
                <label for="username">Username:</label>
                <input id="usernameinput" type="text" name="username" placeholder="Username" /><br>
                
                <label for="password">Password:</label>
                <input id="passwordinput" type="password" name="password" placeholder="Password" /><br>
                
                <input id="loginSubmit" type="submit" value="Submit" />
              </form>
              <div id="newuserlink">
                <a href="">New player? click here.</a>
              </div>
              <div id="iforgot">
                <a href="forgot.jsp">Forgot</a>
              </div>
            </div>

            <div id="newuser">
							<div id="reg" class="span8">
								<p>
									<strong>You must have an account to play so that we
										can reward you properly. <br />Don’t worry, we won’t spam
										you. </strong>
								</p>
								<form id="newuser" action="./checkuser.jsp">
									* Username: <input id="usernameinput" type="text"
										name="username" /><br> Password: <input
										id="passwordinput" type="password" name="password" /><br>
									Email address: <input id="refEmail" class="email" type="email" name="email" placeholder="email" />
									<br>
								<p id="emailAlert" style="display: none;" ></p>
									Most recently awarded academic degree: <select name="degree"
										width="10">
										<option value="ns" selected>Please choose one</option>
										<option value="none">None</option>
										<option value="bachelors">Bachelors</option>
										<option value="masters">Masters</option>
										<option value="phd">Ph.D.</option>
										<option value="md">M.D.</option>
										<option value="other">other</option>
									</select><br /> Do you consider yourself knowledgeable about cancer
									biology?: <select name="cancer">
										<option value="ns" selected>Please choose one</option>
										<option value="no">No</option>
										<option value="yes">Yes</option>
									</select><br /> Do you consider yourself a biologist?: <select
										name="biologist">
										<option value="ns" selected>Please choose one</option>
										<option value="no">No</option>
										<option value="yes">Yes</option>
									</select> <input type="hidden" name="newuser" value="1" /> <br />
									<input type="submit" value="Submit" />
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
var bad = '<%=bad%>';
	function clearInput() {
		$('input[type=text]').focus(function() {
			$(this).val('');
		});
	}

	$(document).ready(function() {
		//start with new user area hidden
		$("#newuser").hide();
		$("#olduser").hide();
		console.log("bad is "+bad);
		if(bad=="nametaken"){
			alert("Sorry, that user name has been taken. Please try another one.");
			$("#newuser").show();
			$("#olduser").hide();
		}else if(bad=="pw"){
			alert("Sorry, that username/password combination is invalid.  Please try again.  ");
			$("#olduser").show();
		} else{		
			$("#olduser").show();
		//show new user on click
		$("#newuserlink").click(function(e) {
			e.preventDefault();
			$("#newuser").show();
			$("#olduser").hide();
		});
		}
	});
</script>
<jsp:include page="js/analytics.js" />
</body>
</html>
