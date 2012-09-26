<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>

<!DOCTYPE html>
<html>
  <head>
    <meta http-equiv="Content-type" content="text/html; charset=utf-8">
    <title>Login to play The Cure</title>
    <meta name="viewport" content="width=device-width">
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

            <div id="olduser" style="display: none;">
              <strong>Enter your credentials here</strong>

              <form action="./checkuser.jsp">
                <label for="username">Username:</label>
                <input id="usernameinput" type="text" name="username" placeholder="Username" /><br>
                
                <label for="password">Password:</label>
                <input id="passwordinput" type="password" name="password" placeholder="Password" /><br>
                
                <input id="loginSubmit" type="submit" value="Submit" />
              </form>
              <div id="newuserlink">
                <a href="login.jsp?bad=n">New player? click here.</a>
              </div>
              <div id="iforgot">
                <a href="forgot.jsp">Forgot</a>
              </div>
            </div>

            <div id="newuser" style="display: none;">
              <h2>Sign Up</h2>
              <p>You must have an account to play so that we can reward you properly.</p>
              <p class="sub">Don’t worry, we won’t spam you.</p>

              <form id="newuser" action="./checkuser.jsp">
                <label for="username">Username *:</label>
                <input id="usernameinput" type="text" name="username" placeholder="Desired Username" />

                <label for="password">Password:</label>
                <input id="passwordinput" type="password" name="password" placeholder="Password" />

                <label for="email">Email:</label>
                <input id="refEmail" class="email" type="email" name="email" placeholder="email" />

                <p id="emailAlert" style="display: none;" ></p>

                <label for="degree">Most recently awarded academic degree:</label>
                <select name="degree" width="10">
                  <option value="ns" selected>Please choose one</option>
                  <option value="none">None</option>
                  <option value="bachelors">Bachelors</option>
                  <option value="masters">Masters</option>
                  <option value="phd">Ph.D.</option>
                  <option value="md">M.D.</option>
                  <option value="other">other</option>
                </select>

                <label for="degree">Do you consider yourself knowledgeable about cancer biology?:</label>
                <select name="cancer">
                  <option value="ns" selected>Please choose one</option>
                  <option value="no">No</option>
                  <option value="yes">Yes</option>
                </select>

                <label for="username">Do you consider yourself a biologist?:</label>
                <select name="biologist">
                  <option value="ns" selected>Please choose one</option>
                  <option value="no">No</option>
                  <option value="yes">Yes</option>
                </select> <input type="hidden" name="newuser" value="1" /> <br />
                <input type="submit" value="Submit" id="loginSubmit" />
              </form>
              <div id="message">
                <p class="sub">* We suggest an anonymous handle like ‘shamu76’, as this will be associated with your game play and will be visible to other players in the game.</p>
              </div>
            </div>

          </div>

          </div>
				</div>
			</div>
		</div>
  </div>

  <div id="alertmsg" style="display: none;"></div>

  <jsp:include page="footer.jsp" />
  <script src="js/libs/jquery-1.8.0.min.js"></script>
  <script src="js/libs/jquery.plugins.js"></script>
  <script src="js/libs/underscore-min.js"></script>
  <script src="js/libs/d3.v2.min.js"></script>
  <script src="js/cure.js"></script>
  <jsp:include page="js/analytics.js" />
</body>
</html>
