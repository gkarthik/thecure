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
		//refresh.. ack ugly..
		//player = Player.lookupPlayer(username);
	}
	if (player != null) {
		List<Integer> mammal_scores = player.getLevel_tilescores().get(
				"mammals");
		if (mammal_scores == null) {
			mammal_scores = new ArrayList<Integer>(4);
			for (int i = 0; i < 4; i++) {
				mammal_scores.add(0);
			}
			player.getLevel_tilescores().put("mammals", mammal_scores);
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
					<p>Click on the tiles below to play rounds of card games based on different anatomical
						and behavioral features associated with mammalhood. Defeat your opponent Barney <img width="25" src="images/barney.png"> to turn the tile over!
						To win, find the best combination of features to use to decide if an unknown creature is a mammal or not.</p>
					<br>
			</div>
			<div class="row">		
					<div id="mosaic" class="span4">
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
								<td><div id="level_<%=level%>">
										<%
											String img_loc = "images/Elephant_Shrew_" + level
																+ ".jpg";
														if (score == 0) {
															img_loc = "images/lock-6-64.png";
														}
										%>
										<a href="zoocard1.jsp?level=<%=level%>"
											class="btn btn-large "> <img width="100"
											src="<%=img_loc%>"> </a> <br>
										<div class="stars">
											<%=score%>
										</div>
									</div></td>
								<%
									}
								%>
							</tr>
							<%
								}
							%>
						</table>

					</div>
				<div id="mystery_animal">
					<img src="images/Notoryctes_typhlops.jpg">
				</div>

				

			</div>
		</div>
	</div>

</body>
</html>
<%} %>