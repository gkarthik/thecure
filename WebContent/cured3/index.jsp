<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%@ page import="org.scripps.combo.model.Player"%>
<%@ page import="org.scripps.combo.model.Board"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.Map"%>
<%@ page import="java.util.ArrayList"%>
<%@ page import="java.util.*" %>
<%
int player_id = 0;
int player_experience = 0;
String player_name = "";
Player player = (Player) session.getAttribute("player");
  if (player == null) {
    	response.sendRedirect("/cure/login.jsp"); 
  } else {
    player_id = player.getId();
    player_experience = 0;
    player_name = player.getName();
  }
%>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>The Cure</title>
<!-- Latest compiled and minified CSS -->
<link rel="stylesheet" href="//netdna.bootstrapcdn.com/bootstrap/3.1.0/css/bootstrap.min.css">
<link href='./css/bootstrap-tour.min.css' rel='stylesheet' type='text/css'>
<link rel="stylesheet" href="/cure/assets/css/style.css" type="text/css" media="screen">
<link href='./css/style.css' rel='stylesheet' type='text/css'>
<link rel="stylesheet" href="./css/odometer-theme-train-station.css" />
<link rel="stylesheet" href="//ajax.googleapis.com/ajax/libs/jqueryui/1.10.4/themes/smoothness/jquery-ui.css" />
</head>
<body>
<div id="loading-wrapper">
	<div class="panel panel-default">
	<div class="panel-heading">
	<center>LOADING</center>
	</div>
	<div class="panel-content">
	<center>Drawing Nodes <span id="loadingCount"></span></center>
	<div class="progress progress-striped active">
	  <div class="progress-bar"  role="progressbar" aria-valuenow="45" aria-valuemin="0" aria-valuemax="100" style="width: 100%"></div>
	</div>
	<center>Loading might take a while.</center>
	</div>
	</div>
</div>
<div id="NodeDetailsWrapper" class="blurCloseElement">
	<div id="NodeDetailsContent"></div>
</div>
<div id="jsonSummary"></div>

<div class="navbar navbar-fixed-top">
        <div class="navbar-inner">
          <div class="container">
            <a class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">
              <span class="icon-bar"></span>
              <span class="icon-bar"></span>
              <span class="icon-bar"></span>
            </a>
             <ul class="nav navbar-nav">
               <li><a class="brand" href="/cure/">The Cure</a></li>
               <li><a href="/cure/profile/">Explore!</a></li>
               <li><a href="/cure/boardroom.jsp">Boardroom</a></li>
                <li><a style="color:#FFF;" href="/cure/contact.jsp">Contact</a></li>
            	<li><a style="color:#FFF;" href="/cure/logout.jsp">logout</a></li>
             </ul>
          </div>
        </div>
      </div>
	<div class="container-fluid CureContainer">
	<div class="alert alert-warning alert-dismissable" id="alertWrapper">
  		 <button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button>
  		<strong id="alertMsg"></strong>
	</div>
		<div class="row">
				<div id="HelpText" class="HelpButton">
					<button type="button" id="closeHelp">×</button>
					<h5>Help</h5>
						<img src="img/helpimage.png" width="500" />
					<h5>Controls and Directions</h5>
					<ul>
						<li>To choose a gene you can start typing the name in the text box. As you start typing a drop down will appear and you can choose a gene from the options shown.</li>
						<li>You can also choose a clinical feature by clicking on <img src="./img/doctor.png">. Click on the text box that appears to view a drop down of the available clinical features.</li>
						<li>To view information regarding the genes/clinical features in the drop down, hover on each option and a window will be shown. You can also use the 'up' and 'down' arrow keys to navigate up and down this drop down.</li>
						<li>To add a node click on <button class="btn btn-small btn-link" type="button"><i class="glyphicon glyphicon-plus-sign"></i><span style="float: none;">Add</span></button> at the bottom of the leaf nodes. The same text box will appear at the bottom.</li>
						<li>To remove a particular split node from the tree, click on <i class="glyphicon glyphicon-remove"></i> and the node along with its children will be deleted.</li>
						<li>To view the information of a gene/clinical feature in the tree, simply click on the gene/clinical feature name in the node.</li>
						<li>To view numerical data of classification, click on the square charts displayed along with every node.</li>
						<li><font color="blue">Y</font> represents a favorable outcome i.e., the cases are predicted to survive beyond ten years.</li>
						<li><font color="red">N</font> represents an unfavorable outcome i.e., the cases are not predicted to survive beyond ten years.</li>
						<li>On top of each node, you can see a colored square representing the user who added that node. A list of users and their place holders is displayed on the right to serve as a key.</li>
						<li>Once you build a tree, be sure to save it by toggling the Save Options panel on the right and clicking on "Save".</li>
						<li>You can also enter a comment by clicking on "Enter Comment" in the Save Options panel.</li>
						<li>To view a textual description of the tree, click on Toggle Panel in Tree Explanation on the right.</li>
						<li>You can toggle the score board panel on the right to see high scores among users. You can click on the high score to view the tree.</li>						
					</ul>
				</div>
				<div id="zoom-controls">
				<button class="zoomin"><i class="glyphicon glyphicon-zoom-in"></i></button>
				<button class="zoomout"><i class="glyphicon glyphicon-zoom-out"></i></button>
				</div>
				<div id="PlayerTreeRegion"></div>
				<div id="cure-panel-wrapper">
						
				</div>
	<jsp:include page="/footer.jsp" />
  	<script type="text/javascript">
    var cure_user_experience = "<%=player_experience%>",
        cure_user_id = "<%=player_id%>",
        cure_user_name = "<%= player_name %>",
        cure_tree_id = null;
    <% if(request.getParameter("treeid")!=null){ %> 
     	cure_tree_id = <%= request.getParameter("treeid") %>;
    <% } %>
  </script>
	<script type="text/javascript" data-main="config" src="lib/require.js" charset="utf-8"></script>
</body>
</html>
