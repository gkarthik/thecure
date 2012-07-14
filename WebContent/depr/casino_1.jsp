<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%@ page import="org.scripps.combo.Player"%>
<%@ page import="org.scripps.combo.GameLog"%>
<%
	Player player = (Player) session.getAttribute("player");
	if (player == null) {
		response.sendRedirect("/combo/login.jsp");
	} else {
		String username = player.getName();
		GameLog log = new GameLog();
		GameLog.high_score sb = log.getScoreBoard();
%>

<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-type" content="text/html; charset=utf-8">
<title>Welcome to COMBO, games of prediction and discovery</title>
<link rel="stylesheet" href="assets/css/combo_bootstrap.css"
	type="text/css" media="screen">
<link rel="stylesheet" href="assets/css/combo.css" type="text/css"
	media="screen">
<link
	href="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8/themes/base/jquery-ui.css"
	rel="stylesheet" type="text/css" />
<script src="//ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js"
	type="text/javascript"></script>
<script
	src="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8/jquery-ui.min.js"></script>

<script>
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
				<a class="btn btn-navbar" data-toggle="collapse"
					data-target=".nav-collapse"> <span class="icon-bar"></span> <span
					class="icon-bar"></span> <span class="icon-bar"></span> </a> <a
					class="brand" href="/combo/">COMBO</a>
				<div class="nav-collapse">
					<ul class="nav">
						<li><a href="about.jsp" target="_blank">About</a></li>
						<li><a
							href="https://groups.google.com/forum/#!forum/genegames"
							target="_blank">Contact</a></li>
						<li><a href="player.jsp?username=<%=username%>"><strong><%=username%></strong>
						</a></li>
						<li><a href="index.jsp">logout</a></li>
					</ul>
				</div>
				<!--/.nav-collapse -->
			</div>
		</div>
	</div>

	<div class="container">
		<div class="hero-unit">
			<div class="row">
				<div class="span11" id="games">
					<h2>Introductory games</h2>
					<br>
					<p>
						Intro level 1 <a href="mammal_mosaic.jsp"
							class="btn btn-large btn-primary"><strong> Mammal Mosaic</strong>
						</a> What separates mammals from all other animals?
					</p>
					<p>
						Intro level 2 <a href="zoocard2.jsp" class="btn btn-large btn-primary">
							<strong> Zookeeper </strong> </a> Divide the animal kingdom into 5
						classes.
					</p>
				</div>
				<hr>
			</div>
			<div class="row">
				<div class="span7" id="craniogames" class="rounded_gradient_box" style="background-image: -webkit-linear-gradient(top, white, #33CCFF);">
					<h2>Craniosynostosis (<a href="http://www.ncbi.nlm.nih.gov/pubmedhealth/PMH0002557/" target = "blank">more about</a>)</h2>
					<div>
					<a href="craniocard1.jsp?dataset_name=cranio_case_control" class="btn btn-large btn-primary"><strong>Craniosynostosis</strong> </a>
					</div>
					<div><br/></div>
					<div >
					<a href="craniocard1.jsp?dataset_name=coronal_case_control" class="btn btn-large btn-primary"><strong>Coronal Craniosynostosis</strong> </a>
					</div>
				</div>			
			</div>
								<div><br/></div>
			<div class="row">
				<div class="span7" id="breastcancergames">
					<div class="rounded_gradient_box">
					<h2>Breast Cancer Metastasis</h2>
					 <a href="genecard1.jsp" class="btn btn-large btn-primary"><img
							src="images/Pink_ribbon.png"><strong>Breast Cancer
								1<br>(Random Walk)</strong> </a>
						<div>
							<br> <br>
						</div>
						<a href="barney.jsp" class="btn btn-large btn-primary"><img
							src="images/Pink_ribbon.png"><strong>Breast Cancer
								2<br>(Defeat Barney!)</strong> </a>
						<div>
							<br> <br>
						</div>
						<a href="gocard1.jsp" class="btn btn-large btn-primary"><img
							src="images/Pink_ribbon.png"><strong>Breast Cancer
								3<br>(GO)</strong> </a>

					</div>
					<%-- 					<div class="span2">
						<div id="scoreboard">
							<table>
								<caption>
									<b><u>Breast Cancer Challenge score board</u>
									</b>
								</caption>
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
											for (String name : sb.getPlayer_avg().keySet()) {
												r++;
									%>
									<tr align="center">
										<td><%=r%></td>
										<td><%=name%></td>
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
					</div> --%>
				</div>
			</div>
		</div>
	</div>

</body>
</html>

<%}%>