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
              <li><a href="login.jsp">Play!</a></li>
            </ul>
          </div><!--/.nav-collapse -->
        </div>
      </div>
    </div>

  <div class="container">
    <div class="hero-unit">
    <div class="row">
    <div class="span5">

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
</div>
</div>
</div>

</body>
</html>