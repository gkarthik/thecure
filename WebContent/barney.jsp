<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%@ page import="org.scripps.combo.Config"%>
<%@ page import="org.scripps.combo.Player"%>	
<%@ page import="org.scripps.combo.GameLog"%>
<%
String username = "";
	Player player = (Player)session.getAttribute("player");
	//refresh.. ack ugly..
	player = Player.lookupPlayer(player.getName());
if (player == null) {
	response.sendRedirect("/combo/login.jsp");   
}else{
	username = player.getName();
}
	GameLog log = new GameLog();
	GameLog.high_score sb = log.getScoreBoard();
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
		<h2>Go head to head with Barney <img src="images/barney.png"></h2>
		<p>Watch out, he gets smarter as you go deeper.. How far can you go?</p>
		<br>
		<div id="barneygames">
			<table class="table-games">
			<%
			int level = -1; 
			int levels_passed = 0;
			if(player.getBarney_levels()!=null&&player.getBarney_levels().size()>0){
				levels_passed = player.getBarney_levels().size();
			}
			for(int i=0; i<Config.num_barney_rows; i++){
			%>
			   <tr>
			   		<%
				for(int j=0; j<Config.num_barney_cols; j++){
					level++;
					int stars = 0;
					if(levels_passed>level){
						stars = player.getBarney_levels().get(level);
					}
					%>
					<td><div id="level_<%=level %>">
					<%=level %> <% if(levels_passed >= level){ %>
						<a href="genecard2.jsp?level=<%=level %>" class="btn btn-large "><img src="images/64px-Pink_ribbon.png"></a>
						<%}else{%>
						<div class="btn btn-large btn-primary disabled"><img src="images/lock-6-64.png"></div>
						<% }%>
					<br>
					<div class="stars">
						<%
						int num_completed = 0;
						for(int s=0; s<Config.num_stars; s++){ 
							if(num_completed < stars){
						%>
							<div class="icon-star"></div>	
						<%} else{	
						%>
							<div class="icon-star-empty"></div>
						<%
							}
						num_completed++;
						} %>
					</div>				
				</div></td>
				<% } %>	
			   </tr>
			<%} %> 
			</table>	
		
		</div>	


    </div>
    
   
</div>
</div>
</div>

</body>
</html>