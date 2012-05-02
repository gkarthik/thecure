<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import="org.scripps.combo.Player"%>	
<%@ page import="org.scripps.combo.GameLog"%>    
<%
String username = request.getParameter("username");
if(username==null){
	response.sendRedirect("/combo/");  
}
Player player = Player.lookupPlayer(username);
GameLog log = new GameLog();
GameLog.high_score sb = log.getScoreBoard();
%>    
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title><%=player.getName() %></title>
<link rel="stylesheet" href="assets/css/combo_bootstrap.css" type="text/css" media="screen">
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
              <li><a href="https://groups.google.com/forum/#!forum/genegames" target="_blank">Contact</a></li>
              <li><a href="http://www.genegames.org" target="_blank">Other bio games</a></li>
              <li><a href="casino.jsp">Play!</a></li>
              <li><a href="index.jsp">logout</a></li>
            </ul>
          </div><!--/.nav-collapse -->
        </div>
      </div>
    </div>
    
    <div class="container">
    <div class="hero-unit">
    <div class="row">
    
    <div class="span10">
    
    <div id="content">
    <h3><%=username %></h3>
<div id="scoreboard">
			<div class="span5">
			<table>
			<caption><b><u>Breast Cancer Challenge score board</u></b></caption>
				<thead>
					<tr>
						<th>Rank</th>
						<th>Player</th>
						<th>Rating</th>
						<th>Games Played</th>
						<th>Personal Best</th>
					</tr>
				</thead>
				<tbody>
					<%
					int r = 0;
					for(String name : sb.getPlayer_avg().keySet()){
						r++;
						String rowhighlight = "";
						if(name.equals(username)){
							rowhighlight = "style=\"background-color:#F5CC6C\";";
						}
						%>
						<tr align="center" <%=rowhighlight %>>
						<td><%=r%></td>
						<td><%=name %></td>
						<td><%=sb.getPlayer_avg().get(name) %></td>
						<td><%=sb.getPlayer_games().get(name) %></td>
						<td><%= sb.getPlayer_max().get(name)%></td>
					</tr>
					<% 
					}
					%>
				</tbody>
			</table>
		</div>
		</div>
    </div>
    </div>
    </div>
    </div>
    </div>
</body>
</html>