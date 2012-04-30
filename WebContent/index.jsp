<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%@ page import="org.scripps.combo.GameLog"%>
<%
	String username = request.getParameter("username");
	if (username != null) {
		session.setAttribute("username", username);
	} else {
		session.setAttribute("username", null);
	}

	GameLog log = new GameLog();
	GameLog.high_score sb = log.getScoreBoard();
%>

<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-type" content="text/html; charset=utf-8">
<title>Welcome to COMBO, games of prediction and discovery</title>
<link rel="stylesheet" href="assets/css/combo_bootstrap.css" type="text/css" media="screen">
	<link href="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8/themes/base/jquery-ui.css" rel="stylesheet" type="text/css" />
	<script src="//ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js" type="text/javascript"></script>
	<script	src="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8/jquery-ui.min.js"></script>
<script>
function clearInput(){
	$('input[type=text]').focus(function() {
		$(this).val('');
	    });		
}

$(document).ready(function() {
	clearInput();
	});	
	</script>
</head>
<body>
	<div id="content">
		<%
			if (username == null) {
		%>
		<div id="login">
			<form target="">
				Enter a username (for the high score list) to start: <input id="usernameinput" type="text" name="username" value="anonymous_hero" />
				<input	type="submit" value="Submit" />
			</form>
		</div>
		<%
			} else {
		%>
		<div id="header">
			Welcome
			<%=username%>! <a href="index.jsp">logout</a>
		</div>
		<div id="games">
			Play:
			<ul>
				<li>Breast Cancer prognosis game
					<ol>
						<li>Version 1 <a href="genecard1.jsp">Play!</a></li>
					</ol></li>
			</ul>
		</div>
		<%
			}
		%>
		<br/>
		<div id="scoreboard">
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
						
						%>
						<tr align="center">
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

</body>
</html>