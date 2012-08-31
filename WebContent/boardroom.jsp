<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%@ page import="org.scripps.combo.Config"%>
<%@ page import="org.scripps.combo.Player"%>
<%@ page import="org.scripps.combo.Board"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.ArrayList"%>
<%
//params for game board
String game_params = "&mosaic_url=boardroom.jsp&dataset=dream_breast_cancer&title=Breast Cancer Survival&nrows=5&ncols=5&max_hand=5";
int level = 0;
int num_tile_rows = 10;
int num_tile_cols = 10;

boolean all_levels_open = true;
	String username = "";
	Player player = (Player) session.getAttribute("player");
	if (player == null) {
		response.sendRedirect("login.jsp");
	} else {
		username = player.getName();
	}
	if (player != null) { 
		Board control = new Board();
		List<Board> boards = control.getBoardsByPhenotype("dream_breast_cancer");
		//System.out.println(boards.size());
%>

<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-type" content="text/html; charset=utf-8">
<title>Breast Cancer Metastasis</title>
<link rel="stylesheet" href="assets/css/combo_bootstrap.css"
	type="text/css" media="screen">
<link rel="stylesheet" href="assets/css/combo.css" type="text/css"
	media="screen">

<link
	href="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8/themes/base/jquery-ui.css"
	rel="stylesheet" type="text/css" />
	
	<script src="js/libs/jquery-1.8.0.min.js"></script>
<script src="js/libs/jquery-ui-1.8.0.min.js"></script>

<jsp:include page="js/analytics.js" />
</head>

<body>
	<div class="navbar navbar-fixed-top">
		<div class="navbar-inner"
			style="background-color: blue; background-image: -webkit-linear-gradient(top, blue, black);">
			<div class="container">
				<a class="btn btn-navbar" data-toggle="collapse"
					data-target=".nav-collapse"> <span class="icon-bar"></span> <span
					class="icon-bar"></span> <span class="icon-bar"></span> </a> <a
					class="brand">Breast Cancer 10 Year Survival</a>
				<div class="nav-collapse">
					<ul class="nav">
              <li><a href="contact.jsp">Contact</a></li>
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
					<h2>Breast Cancer 10 Year survival</h2>
					<p>The goal of this game is to use gene expression levels in breast cancer tumors to predict 10 year survival. 
					If a high quality signature can be identified it can be used to modify treatment accordingly.  </p>
					<p>You must defeat your nemesis Barney <img width="25" src="images/barney.png">. 
						To win each round, find the best combination of genes to use to classify a new sample.</p>
					<br>
			</div>
			<div class="row">		
					<div id="keeper" class="span8">
						<table>
							<%
							String tile_index = "";
								for (int i = 0; i < num_tile_rows; i++) {
							%>
							<tr>
								<%
									for (int j = 0; j < num_tile_cols; j++) {
										level++;
										Board board = boards.get(level);
										int d = (int)board.getBase_score()-50;
										String rating = "1";
										if(d>5&&d<10){
											rating = "2";
										}else if(d<5){
											rating = "3";
										}
										int b_id = board.getId();
								%>
					<td width="50" ><div id="level_<%=level %>">
						<a href="boardgame.jsp?level=<%=b_id %><%=game_params %>" class="btn btn-primary ">
						 <div class="small_level_button"><%=level %>(<%=rating%>) 
						 </div>
						</a>
		
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
					
			
					<div id="back" class="span2">
						<jsp:include page="scoreboard_table.jsp" />
					</div>

									
			</div>
		</div>
	</div>

</body>
</html>
<%} %>