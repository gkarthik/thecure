<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%@ page import="org.scripps.combo.Config"%>
<%@ page import="org.scripps.combo.Player"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.ArrayList"%>
<%
//params for game board
String game_params = "&mosaic_url=cranio_coronal_mosaic.jsp&dataset=coronal_case_control&title=Craniostenostosis&nrows=5&ncols=5&max_hand=5";
int level = -1;
int num_tile_rows = 10;
int num_tile_cols = 10;

boolean all_levels_open = true;
String username = "";
Player player = (Player) session.getAttribute("player");
if (player == null) {
	response.sendRedirect("/combo/login.jsp");
} else {
	username = player.getName();
}
if (player != null) {
	int levels_passed = 0;
	List<Integer> zoo_scores = player.getLevel_tilescores().get("coronal_case_control");
	if (zoo_scores == null) {
		zoo_scores = new ArrayList<Integer>(num_tile_rows*num_tile_cols);
		for (int i = 0; i < num_tile_rows*num_tile_cols; i++) {
			zoo_scores.add(0);
		}
		player.getLevel_tilescores().put("coronal_case_control", zoo_scores);
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
		for(int i=zoo_scores.size(); i<num_tile_rows*num_tile_cols; i++){
			zoo_scores.add(i,0);
		}
	}
%>

<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-type" content="text/html; charset=utf-8">
<title>Craniosynostosis (coronal verse control)</title>
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
					class="brand">Craniosynostosis</a>
				<div class="nav-collapse">
					<ul class="nav">
              <li><a href="contact.jsp">Contact</a></li>
						<li><a href="games.jsp">other games</a></li>
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
					<h2>Craniosynostosis</h2>
					<p>Use gene expression levels in the skull to divide samples between controls and those with coronal craniosynostosis.  Craniosynostosis 
					is the pathologic fusion of calvarial (skull) bones that is associated with abnormal skull growth and increased intracranial pressure.
					<p>As always, you must defeat your nemesis Barney <img width="25" src="images/barney.png"> to turn a tile over.
						To win each round, find the best combination of genes to use to classify a new sample.</p>
					<br>
			</div>
			<div class="row">		
					<div id="keeper" class="span7">
						<table>
							<%
							String tile_index = "";
								for (int i = 0; i < num_tile_rows; i++) {
							%>
							<tr>
								<%
									for (int j = 0; j < num_tile_cols; j++) {
												level++;
												tile_index = i+"_"+j;
												int score = 0;
												if(zoo_scores.get(level)!=null&&zoo_scores.get(level)>0){
													score = zoo_scores.get(level);
												}
								%>
								<td width="50" ><div id="level_<%=level %>">
					<% if(zoo_scores.get(level)==null||zoo_scores.get(level)<1){ %>
						<a href="boardgame.jsp?level=<%=level %><%=game_params %>" class="btn btn-primary "><div class="small_level_button" style="height:20px; line-height:20px; font-weight:normal; width:30px;"><%=level %></div></a>
						<%}else { %>
						<img src="images/cube/cube_bots_<%=tile_index%>.png">
						<%}%>				
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