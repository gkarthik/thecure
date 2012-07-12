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
		List<Integer> zoo_scores = player.getLevel_tilescores().get("coronal_case_control");
		if (zoo_scores == null) {
			zoo_scores = new ArrayList<Integer>(4);
			for (int i = 0; i < 4; i++) {
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
						<li><a href="player.jsp?username=<%=username%>"><strong><%=username%></strong>
						</a></li>
						<li><a href="games.jsp">other games</a></li>
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
					<div id="keeper" class="span5">
						<table>
							<%
								int level = -1;
								int num_tile_rows = 4;
								int num_tile_cols = 4;
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
						<a href="cranio_coronal.jsp?level=<%=level %>" class="btn btn-large btn-primary "><div class="big_level_button"><%=level+1 %></div></a>
						<%}else if(levels_passed > level){ %>
						<a href="cranio_coronal.jsp?level=<%=level %>" class=""><img width="100" src="images/skull/skull_<%=level%>.png"></a>
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
					</div>
					
			
					<div id="back" class="span3">
						<p><a href="games.jsp">Back to game selector</a></p>
					</div>
					
			</div>
		</div>
	</div>

</body>
</html>
<%} %>