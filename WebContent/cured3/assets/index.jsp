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
Player player = (Player) session.getAttribute("player");
  if (player == null) {
    /* response.sendRedirect("login.jsp"); */
  } else {
    player_id = player.getId();
    player_experience = 0;
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
<link rel="stylesheet" href="/cure/assets/css/style.css" type="text/css" media="screen">
<link href='./css/style.css' rel='stylesheet' type='text/css'>
<link rel="stylesheet" href="//ajax.googleapis.com/ajax/libs/jqueryui/1.10.4/themes/smoothness/jquery-ui.css" />
<script src="//ajax.googleapis.com/ajax/libs/jquery/2.1.0/jquery.min.js"></script>
<script src="//ajax.googleapis.com/ajax/libs/jqueryui/1.10.4/jquery-ui.min.js"></script>
<script src="./js/mygene_autocomplete_jqueryui.js" type="text/javascript"></script>
<script type="text/javascript" src="./js/underscore.js"></script>
<script type="text/javascript" src="./js/backbone.js"></script>
<script type="text/javascript" src="./js/backbone-relational.js"></script>
<script type="text/javascript" src="./js/marionette.backbone.min.js"></script>
<script type="text/javascript" src="./js/d3.v3.js" charset="utf-8"></script>
</head>
<body>
<div id="loading-wrapper">
	<div class="panel panel-default">
	<div class="panel-heading">
	<center>Drawing Tree ...</center>
	</div>
	<div class="panel-content">
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
                <li><a style="color:#FFF;" href="/cure/contact.jsp">Contact</a></li>
             </ul>
          </div>
        </div>
      </div>
	<div class="container-fluid CureContainer">
	<div class="alert" id="alertWrapper">
  		<button type="button" class="close">&times;</button>
  		<strong id="alertMsg"></strong>
	</div>
		<div class="row">
				<div id="HelpText">
					<button type="button" id="closeHelp">×</button>
					<h5>Help</h5>
					<ul>
						<li>To decide split criteria, type and choose a gene in the text box. As you start typing a drop down will appear and you can choose a gene from the options shown.</li>
						<li>To view information regarding the genes in the drop down, hover on each option and a window will be shown. You can also use the 'up' and 'down' arrow keys to navigate up and down this drop down.</li>
						<li>To add a node click on <button class="btn btn-small btn-link" type="button"><i class="glyphicon glyphicon-plus-sign"></i><span style="float: none;">Add</span></button> at the bottom of the leaf nodes. The same text box will appear at the bottom.</li>
						<li>To remove a particular gene from the tree, click on <i class="glyphicon glyphicon-remove"></i> and the node along with its children will be deleted.</li>
						<li>To view the information of a gene in the tree, simply click on the gene name in the node.</li>
						<li>To view numerical data of classification, click on the square charts displayed along with every node.</li>
						<li>To view a detailed chart regarding your score, click on <button class="btn btn-small btn-link"><i class="glyphicon glyphicon-remove"></i> Show Chart</button>. Hover over the chart for numerical data as well.</li>
					</ul>
					<h5>Terminology</h5>
						<img src="img/helpimage.png" width="500" />
				</div>
				<div id="PlayerTreeRegion"></div>
			<div id="score-panel">
			<div class=" panel panel-default">
				<div class="panel-heading">Control Panel <button class="btn btn-sm btn-default togglePanel pull-right">Toggle Panel <i class="glyphicon glyphicon-th-list"></i></button></div>
				<div class="panel-body">
  					<button class="btn btn-primary btn-block" id="save_tree">Save Tree</button>
				<hr>
				<div id="CommentRegion"></div>
					<div id="ScoreRegion"></div>
				</span>
				<h2 class="renderPink">Score Board</h2>
				<div id="scoreboard_outerWrapper">
					<table class="table">
						<tr>
							<th>Score</th>
							<th>Size</th>
							<th>Accuracy</th>
							<th>Novelty</th>
						</tr>
					</table>
					<div id='scoreboard_wrapper'></div>
				</div>
				</table>
  				</div>
  				</div>
			</div>
		</div>
	</div>
	<jsp:include page="/footer.jsp" />
	<script type="text/template" id="Empty-Layout-Template">
		<div class="aimWrapper">
		<h3>Aim</h3>
		<p>Build a decision tree that predicts breast cancer outcome using the expression values of genes (and soon clinical variables).</p>
		</div>
		<div id="AddRootNodeWrapper"></div>
	</script> 
	<script type="text/template" id="commentTemplate">
	<@ if(editView == 0) { @>
		<@ if(content == "") { @>
		<button class="btn btn-primary btn-block enter-comment">Enter Comment</button>
		<@ } else { @>
		<p><@= content @></p>
		<button class="btn btn-primary btn-block enter-comment">Change Comment</button>
		<@ } @>
	<@ } else if(editView == 1){@>
		<textarea class="commentContent"><@= content @></textarea>
		<button class="btn btn-primary btn-block save-comment">Save Comment</button>
	<@ } @>
	</script>
	<script type="text/template" id="AddRootNode">
	<div id="mygeneinfo_wrapper" class="addnode_wrapper">
		<label class="label label-default">Enter a Gene Symbol/Name</label>
  		<div id="mygene_addnode">
  			<input id="gene_query" style="width:250px" class="mygene_query_target">
			<span title="Switch To Clinical Features Symbols" class="showCf"><i class="glyphicon glyphicon-refresh"></i></span>
  		</div>
	</div>
	<div id="mygenecf_wrapper" class="addnode_wrapper">
		<label class="label label-default">Click on the textbox and choose a clinical feature</label>
  		<div id="mygene_addnode_cf">
  			<input id="cf_query" style="width:250px" class="cf_query_target">
			<span title="Switch To Gene Symbols" class="hideCf"><i class="glyphicon glyphicon-refresh"></i></span> 
  		</div>
	</div>
  	</script>
  	<script id="ClinicalFeatureSummary" type="text/template">
	<div class="speechWrapper">
	<div class="summary_header">
		<button type="button" class="close">&times;</button>
		<h3><@= args.long_name @></h3>
		<hr>
	</div>
	<div class="summary_content">
		<p><@ if(Cure.isJSON(args.description)){
				var json_string = JSON.parse(args.description);
				for(var temp in json_string){
					print("<h4>"+temp+"</h4>"+"<p>"+json_string[temp]+"</p>");
				}
			  } else {
					print(args.description);
				}
			@></p>
	</div>
	</div>
	</script>
  	<script id="GeneInfoSummary" type="text/template">
	<div class="speechWrapper">
		<div class="summary_header">
			<button type="button" class="close">&times;</button>
			<h4><@= args.symbol @></h4>
			<h4 class="full_name"><@= args.summary.name @></h4>
		</div>
		<div class="summary_content">
		<@= args.summary.summaryText @>
		<@ if(args.summary.goTerms.MF) { @>
			<h4>Molecular Functions</h4>
			<p>
<ul>
				<@ for(var mfunction in args.summary.goTerms.MF) {@>
					<@ if(args.summary.goTerms.MF[mfunction].term)
					{
						print ("<li><a target='_blank' href='http://amigo.geneontology.org/cgi-bin/amigo/term_details?term="+args.summary.goTerms.MF[mfunction].id+"'>"+args.summary.goTerms.MF[mfunction].term+"</a></li>");
					}
					else if(mfunction == "term")
					{
						print ("<li><a target='_blank' href='http://amigo.geneontology.org/cgi-bin/amigo/term_details?term="+args.summary.goTerms.MF.id+"'>"+args.summary.goTerms.MF.term+"</a></li>");
					} 
					 @>
				<@ }@>
</ul>
			</p>
		<@ } @>
		<@ if(args.summary.goTerms.CC) { @>
			<h4>Cellular Component</h4>
			<p>
<ul>
				<@ for(var component in args.summary.goTerms.CC) {@>
					<@ if(args.summary.goTerms.CC[component].term)
					{
						print ("<li><a target='_blank' href='http://amigo.geneontology.org/cgi-bin/amigo/term_details?term="+args.summary.goTerms.CC[component].id+"'>"+args.summary.goTerms.CC[component].term+"</a></li>");
					}
					else if(component == "term")
					{
						print ("<li><a target='_blank' href='http://amigo.geneontology.org/cgi-bin/amigo/term_details?term="+args.summary.goTerms.CC.id+"'>"+args.summary.goTerms.CC.term+"</a></li>");
					} 
					 @>
				<@ }@>
</ul>
			</p>
		<@ } @>
			<@ if(args.summary.goTerms.BP) { @>
				<h4>Biological Process</h4>
				<p>
					<ul>
					<@ for(var process in args.summary.goTerms.BP) {@>
						<@ if(args.summary.goTerms.BP[process].term)
						{
							print ("<li><a target='_blank' href='http://amigo.geneontology.org/cgi-bin/amigo/term_details?term="+args.summary.goTerms.BP[process].id+"'>"+args.summary.goTerms.BP[process].term+"</a></li>");
						}
						else if(process == "term")
						{
							print ("<li><a target='_blank' href='http://amigo.geneontology.org/cgi-bin/amigo/term_details?term="+args.summary.goTerms.BP.id+"'>"+args.summary.goTerms.BP.term+"</a></li>");
						} 
						 @>
					<@ }@>
					</ul>
				</p>
			<@ } @>
			<@ if(args.summary.generif) { @>
			<h4>Gene Rifs</h4>
			<ul>
				<@ for(var generif in args.summary.generif) {@>
					<li>
						<@ if(args.summary.generif[generif].text)
						{
							print ('<a target="blank" href="http://www.ncbi.nlm.nih.gov/pubmed/'+args.summary.generif[generif].pubmed+'">'+args.summary.generif[generif].text+'</a>');
						}
						else if(generif == "text")
						{
							print ('<a target="blank" href="http://www.ncbi.nlm.nih.gov/pubmed/'+args.summary.generif.pubmed+'">'+args.summary.generif.text+'</a>');
						} 
						 @>
					</li>
				<@ } @>
			</ul>
		<@ } @>
	</div>
</div>
  	</script>
  	<script id="JSONSplitNodeCftemplate" type="text/template">
	<div class="jsonview_data" id="jsonview_data<@= args.id @>">
				<div id="summary_header">
					<button type="button" class="close">&times;</button>
					<h2><@= args.name @></h2>
				</div>
				<div id="summary_content">
				<p><@ if(Cure.isJSON(args.summary.summaryText)){
				var json_string = JSON.parse(args.summary.summaryText);
				for(var temp in json_string){
					print("<h4>"+temp+"</h4>"+"<p>"+json_string[temp]+"</p>");
				}
			  } else {
					print(args.summary.summaryText);
				}
			@></p>
				</div>
	</div>
	</script>
  	<script id="JSONSplitValuetemplate" type="text/template">
	<div class="jsonview_data" id="jsonview_data<@= args.id @>">
				<div id="summary_header">
					<button type="button" class="close">&times;</button>
					<h2><@= args.name @></h2>
				</div>
				<div id="summary_content">
				<@= args.summary.summaryText @>
				</div>
	</div>
	</script>
	<script id="JSONtemplate" type="text/template">
		<div class="jsonview_data" id="jsonview_data<@= args.id @>">
				<div id="summary_header">
					<button type="button" class="close">&times;</button>
					<h2><@= args.name @></h2>
					<h4 class="full_name"><@= args.summary.name @></h4>
				</div>
				<div id="summary_content">
				<@= args.summary.summaryText @>
				<@ if(args.summary.goTerms.MF) { @>
			<h4>Molecular Functions</h4>
			<p>
<ul>
				<@ for(var mfunction in args.summary.goTerms.MF) {@>
					<@ if(args.summary.goTerms.MF[mfunction].term)
					{
						print ("<li><a target='_blank' href='http://amigo.geneontology.org/cgi-bin/amigo/term_details?term="+args.summary.goTerms.MF[mfunction].id+"'>"+args.summary.goTerms.MF[mfunction].term+"</a></li>");
					}
					else if(mfunction == "term")
					{
						print ("<li><a target='_blank' href='http://amigo.geneontology.org/cgi-bin/amigo/term_details?term="+args.summary.goTerms.MF.id+"'>"+args.summary.goTerms.MF.term+"</a></li>");
					} 
					 @>
				<@ }@>
</ul>
			</p>
		<@ } @>
		<@ if(args.summary.goTerms.CC) { @>
			<h4>Cellular Component</h4>
			<p>
<ul>
				<@ for(var component in args.summary.goTerms.CC) {@>
					<@ if(args.summary.goTerms.CC[component].term)
					{
						print ("<li><a target='_blank' href='http://amigo.geneontology.org/cgi-bin/amigo/term_details?term="+args.summary.goTerms.CC[component].id+"'>"+args.summary.goTerms.CC[component].term+"</a></li>");
					}
					else if(component == "term")
					{
						print ("<li><a target='_blank' href='http://amigo.geneontology.org/cgi-bin/amigo/term_details?term="+args.summary.goTerms.CC.id+"'>"+args.summary.goTerms.CC.term+"</a></li>");
					} 
					 @>
				<@ }@>
</ul>
			</p>
		<@ } @>
			<@ if(args.summary.goTerms.BP) { @>
				<h4>Biological Process</h4>
				<p>
					<ul>
					<@ for(var process in args.summary.goTerms.BP) {@>
						<@ if(args.summary.goTerms.BP[process].term)
						{
							print ("<li><a target='_blank' href='http://amigo.geneontology.org/cgi-bin/amigo/term_details?term="+args.summary.goTerms.BP[process].id+"'>"+args.summary.goTerms.BP[process].term+"</a></li>");
						}
						else if(process == "term")
						{
							print ("<li><a target='_blank' href='http://amigo.geneontology.org/cgi-bin/amigo/term_details?term="+args.summary.goTerms.BP.id+"'>"+args.summary.goTerms.BP.term+"</a></li>");
						} 
						 @>
					<@ }@>
					</ul>
				</p>
			<@ } @>
			<@ if(args.summary.generif) { @>
			<h4>Gene Rifs</h4>
			<ul>
				<@ for(var generif in args.summary.generif) {@>
					<li>
						<@ if(args.summary.generif[generif].text)
						{
							print ('<a target="blank" href="http://www.ncbi.nlm.nih.gov/pubmed/'+args.summary.generif[generif].pubmed+'">'+args.summary.generif[generif].text+'</a>');
						}
						else if(generif == "text")
						{
							print ('<a target="blank" href="http://www.ncbi.nlm.nih.gov/pubmed/'+args.summary.generif.pubmed+'">'+args.summary.generif.text+'</a>');
						} 
						 @>
					</li>
				<@ } @>
			</ul>
		<@ } @>
</div>
			</div>
  </script>
	<script id="nodeTemplate" type="text/template">  
		<svg class="leafNodeChart chart" id="chart<@= args.cid @>"></svg>
		<@ if(args.name == args.negNodeName)
		{
			print ('<span class="name attrvalue" style="background:red;">'+args.name.toUpperCase()+'</span>');
		}
		else if(args.name == args.posNodeName)
		{
			print ('<span class="name attrvalue" style="background:blue;">'+args.name.toUpperCase()+'</span>');
		} 
      	@>
    <input type="text" class="edit d3edit" value="<@- args.name @>">
    <button class="btn btn-small btn-link addchildren" type="button">
      <i class="glyphicon glyphicon-plus-sign"></i>
		<span style="float: none;">Add</span>
    </button>
	<div class="addgeneinfo" id="addgeneinfo<@= args.cid @>"></div>
  </script>
	<script id="splitValueTemplate" type="text/template">  
    <span class="name attrvalue">
      <@= args.name @>
    </span>
  </script>
	<script id="splitNodeTemplate" type="text/template">
	<svg class="splitNodeChart chart" id="chart<@= args.cid @>"></svg>
   <span class="name attrvalue">
      <@= args.name @>
    </span>
    <button class="btn delete" href="#">
      <i class=" glyphicon glyphicon-remove"></i>
    </button>
</script>
	<script id="ScoreTemplate" type="text/template">
<span id="scoreLabel">Score</span>
<h3 id="score"><@= score @></h3>
<button class="btn btn-sm btn-default closeSVG"><i class="glyphicon glyphicon-resize-small"></i>Hide Chart</button>
		<svg id="ScoreSVG"></svg>
<div id="ScoreDetailsWrapper"></div>
  	</script>
  	<script id="scoreDetailsTemplate" type="text/template">
  	<h4>Score</h4> 
  <h4><@ 
		var currentVal = args.score + (-1 * args.scoreDiff);
  		var endVal = args.score;
  		var increment = args.scoreDiff/Math.abs(args.scoreDiff);
  		console.log(currentVal+" "+endVal+" "+increment);
  		var counter = window.setInterval(function ()
          {
              if (currentVal == endVal)
              {
				console.log(increment);
  				if(increment>0){
  					print(endVal+"<i class='glyphicon glyphicon-arrow-down'></i> "+args.scoreDiff+" POINTS");
  				} else {
  					print(endVal+"<i class='glyphicon glyphicon-arrow-up'></i> "+args.scoreDiff+" POINTS");
  				}
				window.clearInterval(counter); 
              }
              else
              {
                currentVal = currentVal + increment;
                print('Score '+currentVal);
				console.log(currentVal);
              }
          }, 10);
  		 @>
  </h4>
  	</script>
	<script id="EmptyTemplate" type="text/template">
  	</script>
  	<script type="text/javascript">
    var cure_user_experience = "<%=player_experience%>",
        cure_user_id = "<%=player_id%>";
  </script>
  <script id="ScoreBoardTemplate" type="text/template">
			<td><span class='keyValue'><@= json_tree.score @></span></td>
			<td><span class='keyValue'><@= json_tree.size @></span></td>
			<td><span class='keyValue'><@ print(Math.round(json_tree.pct_correct*100)/100) @></span></td>
			<td><span class='keyValue'><@ print(Math.round(json_tree.novelty*100)/100) @></span></td>
  </script>
	<script type="text/javascript" src="./js/app.js" charset="utf-8">></script>
</body>
</html>
