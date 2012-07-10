<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%@ page import="org.scripps.combo.GameLog"%>

<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-type" content="text/html; charset=utf-8">
<title>Welcome to COMBO, games of prediction and discovery</title>
<link rel="stylesheet" href="assets/css/combo_bootstrap.css" type="text/css" media="screen">
	<link href="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8/themes/base/jquery-ui.css" rel="stylesheet" type="text/css" />
	<script src="//ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js" type="text/javascript"></script>
	<script	src="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8/jquery-ui.min.js"></script>

<script type="text/javascript">
$(document).ready(function() {
  var agent =  navigator.userAgent;
  if((agent.indexOf("Safari") == -1)&&(agent.indexOf("Chrome") == -1)){
  	alert("Sorry, this only works on Chrome and Safari right now... \nLooks like you are using \n"+agent);
  }
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
          <div class="nav-collapse">
            <ul class="nav">
              <li><a href="about.jsp" target="_blank">About</a></li>
              <li><a href="https://groups.google.com/forum/#!forum/genegames" target="_blank">Contact</a></li>
              <li><a href="http://www.genegames.org" target="_blank">Other gene games</a></li>
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
			<h1 class="offset1">Welcome to COMBO!</h1>
			<p>Come on in and <a href="login.jsp">play as yourself</a>. Or play <a href="checkuser.jsp?username=anonymous_hero&password=123">anonymously</a></p> 

			<p> 
			Read <a href="about.jsp" target="_blank">about</a> the COMBO concept, or sign up for our <a href="https://groups.google.com/forum/#!forum/genegames" target="_blank">mailing list</a>.
			</p>
		</div>
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