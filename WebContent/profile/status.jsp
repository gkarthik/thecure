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
	<script type="text/template" id="badge-entry-template">
		<td>Level <@= level_id @></td>
		<td><@= description @></td>
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
	<script src="./js/status.js"></script>
</body>
</html>