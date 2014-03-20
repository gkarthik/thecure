<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%@ page import="org.scripps.combo.model.Player"%>
<%@ page import="org.scripps.combo.model.Board"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.Map"%>
<%@ page import="java.util.ArrayList"%>
<% 
int player_id = 0;
int player_experience = 0;
int private_flag = 1;
String player_name = "";
if(request.getParameter("playerid")==null){
  Player player = (Player) session.getAttribute("player");
  if (player == null) {
    	response.sendRedirect("/cure/login.jsp"); 
  } else {
    player_id = player.getId();
    player_experience = 0;
    player_name = player.getName();
  }
} else {
	int id = Integer.parseInt(request.getParameter("playerid"));
	Player player_ = (Player) session.getAttribute("player");
	Player player = (Player) player_.lookupPlayerById(id);
	  if (player == null) {
	    	response.sendRedirect("/cure/login.jsp"); 
	  } else {
	    player_id = player.getId();
	    player_experience = 0;
	    player_name = player.getName();
	  }
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<link rel="stylesheet" href="//netdna.bootstrapcdn.com/bootstrap/3.1.0/css/bootstrap.min.css">
<link href='/cure/assets/css/style.css' rel='stylesheet' type='text/css'>
<link href='./css/style.css' rel='stylesheet' type='text/css'>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Profile</title>
</head>
<body>
<div class="row">
	<div class="navbar navbar-fixed-top">
		<div class="navbar-inner">
			<div class="container">
				<a class="btn btn-navbar" data-toggle="collapse"
					data-target=".nav-collapse"> <span class="icon-bar"></span> <span
					class="icon-bar"></span> <span class="icon-bar"></span>
				</a>
				<ul class="nav navbar-nav">
					<li><a class="brand" href="/cure/">The Cure</a></li>
					<li><a href="/cure/cure2%2E0/index.jsp">Cure 2.0</a></li>
					<li><a style="color: #FFF;" href="/cure/contact.jsp">Contact</a></li>
					<li><a style="color: #FFF;" href="/cure/logout.jsp">logout</a></li>
				</ul>
			</div>
		</div>
	</div>
	</div>
	<div class="container-fluid" id="profile-container">
		
	</div>
	<jsp:include page="/footer.jsp" />
	<script type="text/template" id="main-layout-template">
		<div class="col-md-4">
		<div id="sidebar-fixed">
		<h3><%= player_name %></h3>
		<ul class="nav nav-pills nav-stacked">
		  <li class="active" id="user-treecollection-button"><a href="#">Tree Collection</a></li>
			  <li id="community-treecollection-button"><a href="#">Community</a></li>
		</ul>
<div class="input-group">
	<span class="input-group-addon"><i class="glyphicon glyphicon-search"></i></span>
	<input type="text" class="form-control" id="search_collection" placeholder="Search through users, genes and comments.">
</div>
	<span id="loading-wrapper">Searching... </span>
</div>
	</div>
	<div class="col-md-8 collection-wrapper" id="user-treecollection-wrapper">
	</div>
	<div class="col-md-8 collection-wrapper" id="community-treecollection-wrapper" style="display:none;">
	</div>
	<div class="col-md-8 collection-wrapper" id="search-treecollection-wrapper" style="display:none;">
	</div>
	</script>
	<script type="text/template" id="score-entry-template">
	<@	if(json_tree.score != "Score"){ @>
	<td><span class='keyValue'><@ if(private ==1){print("<i title='Private Tree' style='cursor: default;color:red;' class='glyphicon glyphicon-eye-close'></i>")} @> <@= rank @></span></td>
	<td><span class='keyValue'><@= player_name @></span></td>
	<td><span class='keyValue'><@= json_tree.score @></span></td>
	<td><span class='keyValue'><@= json_tree.size @></span></td>
	<td><span class='keyValue'><@ print(Math.round(json_tree.pct_correct*10)/10) @></span></td>
	<td><span class='keyValue'><@ print(Math.round(json_tree.novelty*10)/10) @></span></td>
	<td><center><@= comment @></center></td>
	<td><svg id="treePreview<@= cid @>"></svg></td>
	<td><@= created @></td>
	<td><center><a href="/cure/cure2%2E0/index.jsp?treeid=<@= id @>"><i class="glyphicon glyphicon-edit"></i></a></center></td>
	<@ } else { @>
	<th><span class='keyValue'><i class="glyphicon glyphicon-star"></i></span></th>
	<th><span class='keyValue'><@= player_name @></span></th>
	<th><span class='keyValue'><@= json_tree.score @></span></th>
	<th><span class='keyValue'><@= json_tree.size @></span></th>
	<th><span class='keyValue'><@= json_tree.pct_correct @></span></th>
	<th><span class='keyValue'><@= json_tree.novelty @></span></th>
	<th><center><@= comment @></center></th>
	<th><center>Preview</center></th>
	<th>Created</th>
	<th><center>View Tree</center></td>
	<@ } @>
	</script>
	<script type="text/javascript">
    var cure_user_experience = "<%=player_experience%>",
        cure_user_id = "<%=player_id%>",
        cure_user_name = "<%= player_name %>";
	</script>
	<script src="./js/underscore.js"></script>
	<script src="./js/jquery-1.10.1.js"></script>
	<script src="./js/backbone.js"></script>
	<script src="./js/marionette.backbone.min.js"></script>
	<script src="./js/d3.v3.js"></script>
	<script src="./js/script.js"></script>
</body>
</html>