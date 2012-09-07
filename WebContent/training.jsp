<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%@ page import="org.scripps.combo.Config"%>
<%@ page import="org.scripps.combo.Player"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.Map"%>
<%@ page import="java.util.ArrayList"%>

<%
	String game_params = "&mosaic_url=training.jsp&dataset=mammal&title=Training: Mammal Challenge&geneinfo=0";

	String username = "";
	Player player = (Player) session.getAttribute("player");
	if (player == null) {
		response.sendRedirect("login.jsp");
	} else {
		username = player.getName();
	}
	if (player != null) {
		int levels_passed = 0;
		Map<Integer, Integer> player_board_scores = player
				.getPhenotype_board_scores().get("mammal");
		List<Integer> zoo_scores = new ArrayList<Integer>();
		if (player_board_scores != null
				&& player_board_scores.values() != null) {
			zoo_scores = new ArrayList<Integer>(
					player_board_scores.values());
		}

		boolean passed_one = false;
		for (int i = 0; i < zoo_scores.size(); i++) {
			if (zoo_scores.get(i) > 0) {
				levels_passed = i;
				passed_one = true;
			}
		}
		if (passed_one) {
			levels_passed++;
		}
		if(levels_passed==4){
			response.sendRedirect("boardroom.jsp");
		}
%>


<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-type" content="text/html; charset=utf-8">
<title>Training Room</title>

<link rel="stylesheet" href="assets/css/combo.css" type="text/css"	media="screen">
<link rel="stylesheet" href="assets/css/combo_bootstrap.css" type="text/css" media="screen">
<link rel="stylesheet" href="assets/css/style.css" type="text/css" media="screen">

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
      <div class="navbar-inner">
        <div class="container">
          <a class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
          </a>
          <a class="brand" href="./">The Cure : training</a>
          <div class="nav-collapse">
            <ul class="nav">
              <li><a href="contact.jsp">Contact</a></li>
			  <li><a href="logout.jsp">logout</a></li>
            </ul>
          </div><!--/.nav-collapse -->
        </div>
      </div>
    </div>

	<div class="container">
		<div class="hero-unit">
			<div class="row">
				<h2>Training Summary</h2>
				<p> Before you get started with The Cure, please take a moment to play through a few simple examples.  
				These will help to get you familiar with the game interface and give you some points to get you started.</p>
				
				<p>In this training stage, your objective is to identify characteristics (e.g. number of legs, breathes air, etc.) that can be used to classify any animal into 
				one of two groups: mammals and not mammals.  To win, you must pick the right characteristic before your opponent Barney <img width="25" src="images/barney.png">!
				
				<p>
					Click on the numbered tiles below to play. Defeat Barney at each level to advance to the Breast Cancer challenge.
				</p>
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
									String gps = game_params;
									String board_size = "&nrows=1&ncols=2&max_hand=1";
									if (i > 0) {
										board_size = "&nrows=2&ncols=2&max_hand=2";
									}
									gps = game_params + board_size;
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
							<td><div id="level_<%=level%>">
									<%
										if (levels_passed == level) {
									%>
									<a href="boardgame.jsp?level=<%=level%><%=gps%>"
										class="btn btn-large btn-primary "><div
											class="big_level_button"><%=level + 1%></div>
									</a>
									<%
										} else if (levels_passed > level) {
									%>
									<a href="boardgame.jsp?level=<%=level%><%=gps%>"><img
										width="100" src="images/Elephant_Shrew_<%=level%>.jpg">
									</a>
									<%
										} else {
									%>
									<div class="btn btn-large btn-primary disabled">
										<img src="images/lock-6-64.png">
									</div>
									<%
										}
									%>
								</div>
							</td>
							<%
								}
							%>
						</tr>
						<%
							}
						%>
					</table>
					<p>Training levels 1-4</p>
				</div>

				<div id="back" class="span3">
					<jsp:include page="scoreboard_table.jsp" />
				</div>

			</div>
		</div>
	</div>

</body>
</html>
<%} %>