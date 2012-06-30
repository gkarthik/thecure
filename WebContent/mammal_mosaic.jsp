<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%@ page import="org.scripps.combo.Config"%>
<%@ page import="org.scripps.combo.Player"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.ArrayList"%>
<%
	String username = "";
	Player player = (Player) session.getAttribute("player");
	if (player == null) {
		response.sendRedirect("/combo/login.jsp");
	} else {
		username = player.getName();
	}
	if (player != null) {
		int levels_passed = 0;
		List<Integer> mammal_scores = player.getLevel_tilescores().get(
				"mammals");
		if (mammal_scores == null) {
			mammal_scores = new ArrayList<Integer>(4);
			for (int i = 0; i < 4; i++) {
				mammal_scores.add(0);
			}
			player.getLevel_tilescores().put("mammals", mammal_scores);
		}else{
			for(int i=0; i<mammal_scores.size(); i++){
				if(mammal_scores.get(i)>0){
					levels_passed = i;
				}
			}
			levels_passed++;
		}
%>

<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-type" content="text/html; charset=utf-8">
<title>Mammal Mosaic</title>
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
</head>
<body>
	<div class="navbar navbar-fixed-top">
		<div class="navbar-inner"
			style="background-color: blue; background-image: -webkit-linear-gradient(top, blue, black);">
			<div class="container">
				<a class="btn btn-navbar" data-toggle="collapse"
					data-target=".nav-collapse"> <span class="icon-bar"></span> <span
					class="icon-bar"></span> <span class="icon-bar"></span> </a> <a
					class="brand">Mammal Mosaic</a>
				<div class="nav-collapse">
					<ul class="nav">
						<li><a
							href="https://groups.google.com/forum/#!forum/genegames"
							target="_blank">Contact</a></li>
						<li><a href="player.jsp?username=<%=username%>"><strong><%=username%></strong>
						</a></li>
						<li><a href="casino.jsp">other games</a></li>
					</ul>
				</div>
				<!--/.nav-collapse -->
			</div>
		</div>
	</div>

	<div class="container">
		<div class="hero-unit">
			<div class="row">
					<h2>Mammal Challenge Summary</h2>
					<p>The world contains millions of different kinds of animals.
						To make sense of them all, biologists divide them up into
						different groups. Our favorite is the mammals of course. But what
						makes a mammal different from a bird, reptile, or any other of the
						major classes?</p>
					<p>Click on the numbered tiles below to play. Defeat your opponent Barney <img width="25" src="images/barney.png"> to turn the tile over!
						To win, find the best combination of features to use to decide if an unknown creature is a mammal or not.</p>
					<br>
			</div>
			<div class="row">		
					<div id="shrew" class="span3">
						<table>
							<%
								int level = -1;
								int num_tile_rows = 2;
								int num_tile_cols = 2;
								for (int i = 0; i < num_tile_rows; i++) {
							%>
							<tr>
								<%
									for (int j = 0; j < num_tile_cols; j++) {
												level++;
												int score = 0;
												if (mammal_scores.size() > level) {
													score = mammal_scores.get(level);
												}
								%>
								<td><div id="level_<%=level %>">
					<% if(levels_passed == level){ %>
						<a href="zoocard1.jsp?level=<%=level %>" class="btn btn-large btn-primary "><div class="big_level_button"><%=level+1 %></div></a>
						<%}else if(levels_passed > level){ %>
						<a href="zoocard1.jsp?level=<%=level %>" class=""><img width="100" src="images/Elephant_Shrew_<%=level%>.jpg"></a>
						<%}else{%>
						<div class="btn btn-large btn-primary disabled"><img src="images/lock-6-64.png"></div>
						<% }%>				
					</div></td>								
								<%
									}
								%>
							</tr>
							<%
								}
							%>
						</table>
						<p>Level 1: Elephant Shrew</p>
					</div>
					
					<div id="opossum" class="span3">
						<table>
							<%
								for (int i = 0; i < num_tile_rows; i++) {
							%>
							<tr>
								<%
									for (int j = 0; j < num_tile_cols; j++) {
												level++;
												int score = 0;
												if (mammal_scores.size() > level) {
													score = mammal_scores.get(level);
												}
								%>
								<td><div id="level_<%=level %>">
					<% if(levels_passed == level){ %>
						<a href="zoocard1.jsp?level=<%=level %>" class="btn btn-large btn-primary "><div class="big_level_button"><%=level+1 %></div></a>
						<%}else if(levels_passed > level){ %>
						<a href="zoocard1.jsp?level=<%=level %>" class=""><img width="100" src="images/possum_<%=level-4%>.jpg"></a>
						<%}else{%>
						<div class="btn btn-large btn-primary disabled"><img src="images/lock-6-64.png"></div>
						<% }%>				
					</div></td>								
								<%
									}
								%>
							</tr>
							<%
								}
							%>
						</table>
						<p>Level 2: Opossum</p>
					</div>
					
			</div>
		</div>
	</div>

</body>
</html>
<%} %>