<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%@ page import="org.scripps.combo.Config"%>
<%@ page import="org.scripps.combo.Player"%>	
<%@ page import="org.scripps.combo.GameLog"%>
<%
String win = request.getParameter("win");
String level = request.getParameter("level");
String username = "";
	Player player = (Player)session.getAttribute("player");
	//refresh.. ack ugly..
if (player == null) {
	response.sendRedirect("/combo/login.jsp");   
}else{
	player = Player.lookupPlayer(player.getName());
	username = player.getName();
}
%>

<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-type" content="text/html; charset=utf-8">
<title>Defeat Barney!</title>
<link rel="stylesheet" href="assets/css/combo_bootstrap.css" type="text/css" media="screen">
<link rel="stylesheet" href="assets/css/combo.css" type="text/css" media="screen">

	<link href="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8/themes/base/jquery-ui.css" rel="stylesheet" type="text/css" />
	<script src="//ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js" type="text/javascript"></script>
	<script	src="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8/jquery-ui.min.js"></script>
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
              <li><a href="http://www.genegames.org" target="_blank">Other bio games</a></li>
              <li><a href="player.jsp?username=<%=username%>"><strong><%=username%></strong></a></li>
              <li><a href="index.jsp">logout</a></li>
            </ul>
          </div><!--/.nav-collapse -->
        </div>
      </div>
    </div>

  <div class="container">
    <div class="hero-unit">
    <div class="row">
    <div  id="games">
		<h2>Game review <img src="images/barney.png"></h2>
		<p></p>
		<br>
		<div id="game result">
			<% 		if(win!=null&&win.equals("1")&&level!=null){ %>
				Congratulations you defeated Barney level <%=level %>!
	
	<% }else{
			%>
				Too bad, Barney level <%=level %> beat you
			<%} %>
				<p><a href="barney.jsp">next</a></p>
			
		</div>	


    </div>
    
   
</div>
</div>
</div>

</body>
</html>