<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%@ page import="org.scripps.combo.Config"%>
<%@ page import="org.scripps.combo.Player"%>	
<%@ page import="org.scripps.combo.GameLog"%>
<%
String username = "";
	Player player = (Player)session.getAttribute("player");
if (player == null) {
	response.sendRedirect("/combo/login.jsp");   
}else{
	username = player.getName();
	//refresh.. ack ugly..
	player = Player.lookupPlayer(username);
}
if(player != null) {
	GameLog log = new GameLog();
	GameLog.high_score sb = log.getScoreBoard();
%>

<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-type" content="text/html; charset=utf-8">
<title>Mammal Mosaic</title>
<link rel="stylesheet" href="assets/css/combo_bootstrap.css" type="text/css" media="screen">
<link rel="stylesheet" href="assets/css/combo.css" type="text/css" media="screen">

	<link href="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8/themes/base/jquery-ui.css" rel="stylesheet" type="text/css" />
	<script src="//ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js" type="text/javascript"></script>
	<script	src="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8/jquery-ui.min.js"></script>
</head>
<body>
	<div class="navbar navbar-fixed-top">
      <div class="navbar-inner" 
  style="background-color: blue; background-image: -webkit-linear-gradient(top, blue, black);">
        <div class="container">
          <a class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
          </a>
          <a class="brand">Mammal Mosaic</a>
          <div class="nav-collapse">
            <ul class="nav">
              <li><a href="https://groups.google.com/forum/#!forum/genegames" target="_blank">Contact</a></li>
              <li><a href="player.jsp?username=<%=username%>"><strong><%=username%></strong></a></li>
              <li><a href="casino.jsp">other games</a></li>
            </ul>
          </div><!--/.nav-collapse -->
        </div>
      </div>
    </div>

  <div class="container">
    <div class="hero-unit">
    <div class="row">
    <div id="games">
		<h2>What makes this little fellow <a target="blank" title="Elephant Shrew" href="http://en.wikipedia.org/wiki/Elephant_shrew">
		<img width="100" src="images/Elephant_Shrew.jpg"></a> a mammal?</h2>
		<p>Is it his brown hair? His little legs? His air-breathing nose?</p>
		<h2>Click a tile to start playing, learning and teaching.</h2>
		<br>
		<div id="mosaic">
			<table >
			<%
			int level = -1; 
			int levels_passed = 0;
			int num_tile_rows = 2; int num_tile_cols = 2;
			if(player.getBarney_levels()!=null&&player.getBarney_levels().size()>0){
				levels_passed = player.getBarney_levels().size();
			}
			for(int i=0; i<num_tile_rows; i++){
			%>
			   <tr>
			   		<%
				for(int j=0; j<num_tile_cols; j++){
					level++;
					int stars = 0;
					if(levels_passed>level){
						stars = player.getBarney_levels().get(level);
					}
					%>
					<td><div id="level_<%=level %>">
					<%=level %> <% if(levels_passed >= level){ %>
						<a href="zoocard1.jsp?level=<%=level %>" class="btn btn-large ">
						<img width="100" src="images/dolphin.jpg"></a>
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
			<%} %>