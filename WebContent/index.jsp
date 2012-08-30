<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%@ page import="org.scripps.combo.GameLog"%>
<% 
GameLog log = new GameLog();
GameLog.high_score sb = log.getScoreBoard();
%>
<!DOCTYPE html>
<html>
<head>

<meta name="apple-mobile-web-app-capable" content="yes" />
<meta name="apple-mobile-web-app-status-bar-style" content="blue" />
<meta http-equiv="Content-type" content="text/html; charset=utf-8">
<title>Welcome to The Cure.  A game to help defeat breast cancer.</title>

<link rel="apple-touch-startup-image" href="images/barney.png">

<link
	href="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8/themes/base/jquery-ui.css"
	rel="stylesheet" type="text/css" />
<link rel="stylesheet" href="assets/css/combo_bootstrap.css" type="text/css" media="screen">

<script src="js/libs/jquery-1.8.0.min.js"></script>
<script src="js/libs/jquery-ui-1.8.0.min.js"></script>



<script type="text/javascript">
/* $(document).ready(function() {
  var agent =  navigator.userAgent;
  if((agent.indexOf("Safari") == -1)&&(agent.indexOf("Chrome") == -1)&&(agent.indexOf("AppleWebKit") == -1)){
  	alert("Sorry, this only works on Chrome and Safari right now... \nLooks like you are using \n"+agent);
  }
  setTimeout(function() { 
	  window.scrollTo(0, 1); 
	  }, 100);
}); */

</script>

<jsp:include page="js/analytics.js" />
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
          <a class="brand" href="./">The Cure</a>
          <div class="nav-collapse">
            <ul class="nav">
              <li><a href="about.jsp" target="_blank">About</a></li>
              <li><a href="contact.jsp" target="_blank">Contact</a></li>
              <li><a href="login.jsp">Login and play</a></li>
            </ul>
          </div><!--/.nav-collapse -->
        </div>
      </div>
    </div>

  <div class="container">
    <div class="hero-unit">
    <div class="row">
    <div class="span10">
		<div id="welcome" style="text-align:center;">
			<h1 class="offset1">Welcome to TheCure!</h1>
			<p>Come on in and <a href="login.jsp">play</a></p> 
			<form action="./SocialServer">
				Your name <input id="by" type="text" name="by" /><br>
				Invited person's email <input id="invited" type="text" name="invited" /><br>
				<input id="command" type="hidden" name="command" value="invite"/>
				<input	type="submit" value="Submit" /> 		
			</form>	
			
		</div>
	</div>
	</div>
	<div class="row">
		<div class="offset3 span1">
			<img align="middle" src="images/barney.png">
		</div>
	<div class="offset2 span1" style="text-align:center;">
			<jsp:include page="scoreboard_table.jsp" />
		</div>
	</div>
	<div class ="row">
	<div class="offset1">
	<!--  <img src="http://upload.wikimedia.org/wikipedia/commons/thumb/1/1e/CAT-D10N-pic001.jpg/640px-CAT-D10N-pic001.jpg"> -->
	</div>
	
	</div>
</div>
</div>

</body>
</html>