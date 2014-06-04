<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%@ page import="org.scripps.combo.model.Player"%>
<%@ page import="org.scripps.combo.model.Board"%>
<%@ page import="org.scripps.combo.model.Badge"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.Map"%>
<%@ page import="java.util.ArrayList"%>
<%@ page import="java.util.*" %>
<%
int player_id = 0;
int player_experience = 0;
String player_name = "";
String badge_desc = null;
if(request.getParameter("badgeid")!=null){
	Badge _badge = new Badge();
	HashMap mp = _badge.getBadgebyId(Integer.parseInt(request.getParameter("badgeid")));
	badge_desc = mp.get("description").toString();
}
Player player = (Player) session.getAttribute("player");
  if (player == null) {
    	player_id = -1;
    	player_experience = 0;
    	player_name = "Guest";
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
<link href='./css/bootstrap-switch.css' rel='stylesheet' type='text/css'>
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

	<jsp:include page="/header.jsp" />
	<div class="container-fluid CureContainer">
	<div class="alert alert-warning alert-dismissable" id="alertWrapper">
  		 <button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button>
  		<strong id="alertMsg"></strong>
	</div>
	<div id="InfoWrapper" class="panel panel-default">
	<div class="panel-heading"><span>Data</span>
	<span><button class="close">&times;</button></span>
	</div>
	<div class="panel-body">
	Current Dataset: METABRIC <br /> <br />
	<p>The version of the METABRIC dataset that is currently loaded contains 12 clinical features and expression values for 17,920 genes across 1,226 samples.  527 (43%) of the samples have recorded survival times greater than 10 years while the remaining 699 (57%) have less than 10 year survival.  Survival time is measured from the point of diagnosis.</p>
<p>The 12 clinical features include: age, tumor size, # of lymph nodes positive, grade, histological type, ER_IHC_status, ER expression, PR expression, HER2_IHC_status, HER2_SNP6_state, HER2 expression, and Treatment.</p>   
<p>The complete METABRIC dataset contains clinical traits, gene expression values, CNV profiles, and SNP genotypes derived from breast tumors collected from participants of the METABRIC trial. Details about the METABRIC cohort have been published here: Curtis C, et.al. The genomic and transcriptomic architecture of 2,000 breast tumours reveals novel subgroups. Nature. 2012 Apr 18;486(7403):346-52.</p>
	</div>
	</div>
		<div class="row">
				<div id="HelpText" class="HelpButton">
					<button type="button" class="close">&times;</button>
					<h3 class="renderPink">Help</h3>
					<p>
					<button class="btn btn-sm btn-default" id="taketour">Take Tour</button>
						<img align="right" src="img/helpimage.png" width="500" />
					<dl class="dl-horizontal">
					  <dt>Add a Gene</dt>
					  <dd>To choose a gene you can start typing the name in the text box. As you start typing a drop down will appear and you can choose a gene from the options shown.</dd>
					  <dt>Add a Clinical Feature</dt>
					  <dd>You can choose a clinical feature by clicking on <img width="30" src="./img/doctor.png">. Click on the text box that appears to view a drop down of the available clinical features.</dd>
					  <dt>Gene and Clinical Feature Information</dt>
					  <dd>To view information regarding the genes/clinical features in the drop down, hover on each option and a window will be shown. You can also use the 'up' and 'down' arrow keys to navigate up and down this drop down.</dd>
					  <dt>Add a Gene/Clinical Feature to Leaf Nodes</dt>
					  <dd>To add a node click on <button class="btn btn-small btn-link" type="button"><i class="glyphicon glyphicon-plus-sign"></i><span style="float: none;">Add</span></button> at the bottom of the leaf nodes. The same text box will appear at the bottom.</dd>
					  <dt>Delete Node</dt>
					  <dd>To remove a particular split node from the tree, click on <i class="glyphicon glyphicon-remove"></i> and the node along with its children will be deleted.</dd>
					  <dt>Gene/Clinical Feature Information from Tree</dt>
					  <dd>To view the information of a gene/clinical feature in the tree, simply click on the gene/clinical feature name in the node.</dd>
					  <dt>Numerical data of Classification</dt>
					  <dd>To view numerical data of classification, click on the square charts displayed along with every node.</dd>
					  <dt>Outcomes</dt>
					  <dd><font color="blue"><b>Y</b></font> represents a favorable outcome i.e., the cases are predicted to survive beyond ten years.<font color="red"><b>N</b></font> represents an unfavorable outcome i.e., the cases are not predicted to survive beyond ten years.</dd>
					  <dt>Player Placeholder</dt>
					  <dd>On top of each node, you can see a colored square representing the user who added that node. A list of users and their place holders is displayed on the right to serve as a key.</dd>
					  <dt>Save Tree</dt>
					  <dd>Once you build a tree, be sure to save it by clicking on the Save Button on the right. You can add a comment by typing in the comment box that will appear once you click save. In case you want to protect your tree you can also choose an option to save your tree as private in which case no other player will be able to view that tree but it will not be part of the score baord.</dd>
					  <dt>Textual Description of Tree</dt>
					  <dd>To view a textual description of the tree, click on Tree Explanation on the right.</dd>
					  <dt>Profie</dt>
					  <dd>You can see your badge collection and your tree collection in your profile page. To view your profile click on My Profile on the right panel.</dd>
					</dl>						
					</ul>
					</p>
				</div>
				<div id="zoom-controls">
				
				</div>
				<div id="PlayerTreeRegion"></div>
				<div id="cure-panel-wrapper">
						
				</div>
				<div id="GenePoolRegion">
				</div>
				<div id="FeatureBuilderRegion">
				</div>
	<jsp:include page="/footer.jsp" />
  	<script type="text/javascript">
    var cure_user_experience = "<%=player_experience%>",
        cure_user_id = "<%=player_id%>",
        cure_user_name = "<%= player_name %>",
        cure_tree_id = null,
        badge_desc = <%= badge_desc %>,
        badge_id = <%= request.getParameter("badgeid") %>,
        base_url= document.location.href.split("cure2.0")[0],
        _csb = [[null, null]];
    <% if(request.getParameter("treeid")!=null){ %> 
     	cure_tree_id = <%= request.getParameter("treeid") %>;
    <% } %>
  </script>
  <% if(request.getParameter("ref")!=null){
		if(request.getParameter("ref").equals("yako")){ %>
	<script>_csb=[['token','R6TzQ7']];</script>
	<% }
		}%>
	<script type="text/javascript" data-main="config" src="lib/require.js" charset="utf-8"></script>
</body>
</html>
