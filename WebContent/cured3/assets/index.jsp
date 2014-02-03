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
<html>
<head>
<meta charset="utf-8">
<title>The Cure</title>
<link href='./css/bootstrap.min.css' rel='stylesheet' type='text/css'>
<link rel="stylesheet" href="/cure/assets/css/style.css" type="text/css" media="screen">
<link href='./css/style.css' rel='stylesheet' type='text/css'>
<link rel="stylesheet"
	href="http://code.jquery.com/ui/1.10.3/themes/ui-lightness/jquery-ui.min.css"
	type="text/css" media="all" />
<script type="text/javascript" src="./js/jquery-1.10.1.js"></script>
<script src="http://code.jquery.com/ui/1.10.3/jquery-ui.min.js"
	type="text/javascript"></script>
<script src="./js/mygene_autocomplete_jqueryui.js"
	type="text/javascript"></script>
<script type="text/javascript" src="./js/underscore.js"></script>
<script type="text/javascript" src="./js/backbone.js"></script>
<script type="text/javascript" src="./js/backbone-relational.js"></script>
<script type="text/javascript" src="./js/marionette.backbone.min.js"></script>
<script type="text/javascript" src="./js/d3.v3.js" charset="utf-8"></script>
</head>
<body>
<div id="NodeDetailsWrapper">
	<div id="NodeDetailsContent"></div>
</div>

<div class="navbar navbar-fixed-top">
        <div class="navbar-inner">
          <div class="container">
            <a class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">
              <span class="icon-bar"></span>
              <span class="icon-bar"></span>
              <span class="icon-bar"></span>
            </a>
            <a class="brand" href="/cure/">The Cure</a>
            <div class="nav-collapse">
              <ul class="nav">
                <li><a href="/cure/contact.jsp">Contact</a></li>
              </ul>
            </div>
          </div>
        </div>
      </div>
	<div class="container CureContainer">
	<div class="alert" id="alertWrapper">
  		<button type="button" class="close">&times;</button>
  		<strong id="alertMsg"></strong>
	</div>
		<div class="row">
					<div class="span9">
				<span class="row">
				<div id="HelpText">
					<button type="button" id="closeHelp">×</button>
					<h5>Help</h5>
					<ul>
						<li>To decide split criteria, type and choose a gene in the text box. As you start typing a drop down will appear and you can choose a gene from the options shown.</li>
						<li>To view information regarding the genes in the drop down, hover on each option and a window will be shown. You can also use the 'up' and 'down' arrow keys to navigate up and down this drop down.</li>
						<li>To add a node click on <button class="btn btn-small btn-link" type="button"><i class="icon-plus-sign"></i><span style="float: none;">Add Node </span></button> at the bottom of the leaf nodes. The same text box will appear at the bottom.</li>
						<li>To remove a particular gene from the tree, click on <i class="icon-remove"></i> and the node along with its children will be deleted.</li>
						<li>To view the information of a gene in the tree, simply click on the gene name in the node.</li>
						<li>To view numerical data of classification, click on the square charts displayed along with every node.</li>
						<li>To view a detailed chart regarding your score, click on <button class="btn btn-small btn-link"><i class="icon-fullscreen"></i> Show Chart</button>. Hover over the chart for numerical data as well.</li>
					</ul>
					<h5>Terminology</h5>
						<img src="img/helpimage.png" width="500" />
				</div>
				<div id="PlayerTreeRegion"></div>
				</span>
			</div>
			<div class="span3">
			
				<span class="row">
				<button class="btn btn-primary" id="save_tree">Save Tree</button>
				<hr>
				<div id="CommentRegion"></div>
					<div id="ScoreRegion"></div>
				</span>
				<h2>Gene Summary</h2>
				<table class="table table-hover" id='json_structure'></table>
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
		<button class="btn btn-primary enter-comment">Enter Comment</button>
		<@ } else { @>
		<p><@= content @></p>
		<button class="btn btn-primary enter-comment">Change Comment</button>
		<@ } @>
	<@ } else if(editView == 1){@>
		<textarea class="commentContent"><@= content @></textarea>
		<button class="btn btn-link save-comment">Save</button>
	<@ } @>
	</script>
	<script type="text/template" id="AddRootNode">
		<label class="label label-info">Key in a Gene Symbol/Name to Start</label>
  		<div id="mygene_addnode">
  			<input id="gene_query" style="width:250px" class="mygene_query_target">
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
	<script id="JSONtemplate" type="text/template">
      	<td><span class="jsonview_name"><@= args.name @></span></td>
      	<td><span><button class="btn btn-link showattr" >Info</button></span></td>
	  	<@ if(args.kind == "split_node") { @>
      		<td><span><button class="btn btn-link showjson" >Gene Summary</button></span></td>
      		<td>
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
		</td>
	  <@  } @>
  </script>
	<script id="Attrtemplate" type="text/template">
    <td colspan="3">
<button class="btn btn-link editdone" >Close Info</button>
    <ul class="unstyled">
      <li>
            <span class="attredit">
              <label class="label label-info">Name</label><br /><label class="attrvalue"><@= args.name @></label>
              <input class="input-medium edit" id="name" type="text" value="<@= args.name @>" />
            </span>
      </li>
      <@ for(var option in args.options) {@>
        <li>
            <span class="attredit">
              <label class="label label-info"><@= option @></label><br /><label class="attrvalue"> <@= args.options[option] @></label>
              <input class="input-medium edit modeloption" id="<@= option @>" type="text" value="<@= args.options[option] @>" />
            </span>
        </li>
      <@ } @>
    </ul>
    </td>
  </script>
	<script id="nodeTemplate" type="text/template">  
		<svg class="leafNodeChart chart" id="chart<@= args.cid @>"></svg>
		<@ if(args.name == "relapse")
		{
			print ('<span class="name attrvalue" style="background:red;">'+args.name.toUpperCase()+'</span>');
		}
		else if(args.name == "no relapse")
		{
			print ('<span class="name attrvalue" style="background:blue;">'+args.name.toUpperCase()+'</span>');
		} 
      	@>
    <input type="text" class="edit d3edit" value="<@- args.name @>">
    <button class="btn btn-small btn-link addchildren" type="button">
      <i class="icon-plus-sign"></i>
		<span style="float: none;">Add Node </span>
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
    <button class="btn btn-small btn-link delete" href="#">
      <i class="icon-remove"></i>
    </button>
</script>
	<script id="ScoreTemplate" type="text/template">
<span id="scoreLabel">Score</span>
<h3 id="score"><@= score @></h3>
<button class="btn btn-small btn-link closeSVG"><i class="icon-fullscreen"></i>Hide Chart</button>
		<svg id="ScoreSVG"></svg>
  	</script>
	<script id="EmptyTemplate" type="text/template">
  	</script>
  	<script type="text/javascript">
    var cure_user_experience = "<%=player_experience%>",
        cure_user_id = "<%=player_id%>";
  </script>
	<script type="text/javascript" src="./js/app.js" charset="utf-8">></script>
</body>
</html>
