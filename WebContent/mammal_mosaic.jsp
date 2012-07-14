<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%@ page import="org.scripps.combo.Config"%>
<%@ page import="org.scripps.combo.Player"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.ArrayList"%>

<%
String game_params = "&mosaic_url=mammal_mosaic.jsp&dataset=mammal&title=Mammal Challenge&geneinfo=0";

String username = "";
Player player = (Player) session.getAttribute("player");
if (player == null) {
	response.sendRedirect("/combo/login.jsp");
} else {
	username = player.getName();
}
if (player != null) {
	int levels_passed = 0;
	List<Integer> zoo_scores = player.getLevel_tilescores().get("mammal");
	if (zoo_scores == null) {
		zoo_scores = new ArrayList<Integer>(4);
		for (int i = 0; i < 4; i++) {
			zoo_scores.add(0);
		}
		player.getLevel_tilescores().put("mammal", zoo_scores);
	}else{
		boolean passed_one = false;
		for(int i=0; i<zoo_scores.size(); i++){
			if(zoo_scores.get(i)>0){
				levels_passed = i;
				passed_one = true;
			}
		}
		if(passed_one){
			levels_passed++;
		}
	}
String board_size = "&nrows=1&ncols=2&max_hand=1";
if(levels_passed>3){
	board_size = "&nrows=2&ncols=2&max_hand=2";
}
game_params+=board_size;
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
						<li><a href="games.jsp">other games</a></li>
						<!--  <li><a href="player.jsp?username=<%=username%>"><strong><%=username%></strong></a></li> -->						
						<li><a href="logout.jsp">logout</a></li>

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
												if (zoo_scores.size() > level) {
													score = zoo_scores.get(level);
												}
								%>
								<td><div id="level_<%=level %>">
					<% if(levels_passed == level){ %>
						<a href="boardgame.jsp?level=<%=level %><%=game_params %>" class="btn btn-large btn-primary "><div class="big_level_button"><%=level+1 %></div></a>
						<%}else if(levels_passed > level){ %>
						<img width="100" src="images/Elephant_Shrew_<%=level%>.jpg">
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
												if (zoo_scores.size() > level) {
													score = zoo_scores.get(level);
												}
								%>
								<td><div id="level_<%=level %>">
					<% if(levels_passed == level){ %>
						<a href="boardgame.jsp?level=<%=level %><%=game_params %>" class="btn btn-large btn-primary "><div class="big_level_button"><%=level+1 %></div></a>
						<%}else if(levels_passed > level){ %>
						<img width="100" src="images/possum_<%=level-4%>.jpg">
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
					<div id="back" class="span3">
						<p><a href="games.jsp">Back to game selector</a></p>
						<jsp:include page="scoreboard_table.jsp" />
					</div>
					
			</div>
		</div>
	</div>

</body>
</html>
<%} %>