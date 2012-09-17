<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%@ page import="org.scripps.combo.weka.Weka"%>
<%@ page import="org.scripps.combo.GameLog"%>
<%@ page import="org.scripps.combo.Player"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.ArrayList"%>

<% 
String full_request = request.getRequestURI()+"?"+request.getQueryString();
String mosaic_url = request.getParameter("mosaic_url"); //"cranio_coronal_mosaic.jsp";
String dataset = request.getParameter("dataset"); // "coronal_case_control";
String title = request.getParameter("title"); // "Craniostenososis - coronal verse control";
String nrows = request.getParameter("nrows"); // "5";
String ncols = request.getParameter("ncols"); // "5";
String max_hand = request.getParameter("max_hand"); // "5";
String showgeneinfo = "1";
if(request.getParameter("geneinfo")!=null){
	showgeneinfo = request.getParameter("geneinfo");
}

String username = (String)session.getAttribute("username");
Player p = (Player)session.getAttribute("player");
String player_id = ""+p.getId();
String level = request.getParameter("level");
int ilevel = 0;
if(level!=null){
	ilevel = Integer.parseInt(level);
}
int display_level = ilevel+1;
if(username==null){
	username = "anonymous_hero";
}
GameLog glog = new GameLog();
Integer multiplier = glog.getPheno_multiplier().get(dataset);

%>

<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="A game">
    <meta name="author" content="Ben">

    <link rel="stylesheet" href="assets/css/barney.css" type="text/css"	media="screen">
    <link rel="stylesheet" href="assets/css/combo.css" type="text/css"	media="screen">
    <link rel="stylesheet" href="assets/css/combo_bootstrap.css" type="text/css" media="screen">
    <link rel="stylesheet" href="assets/css/style.css" type="text/css" media="screen">

    <style>
    body {
      padding-top: 60px;  
      /* 60px to make the container go all the way to the bottom of the topbar */
    }
    </style>

<title>The Cure: <%=title %></title>
<%
	String ran = request.getParameter("level");
	if (ran == null) {		
		ran = ""+(int)Math.rint(Math.random()*1000);
	}
%>


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
        <a class="brand">The Cure</a>
        <div class="nav-collapse">
          <ul class="nav">
            <li><a href="help.jsp" target="blank">Help!</a>
            <li><a href="contact.jsp">Contact</a></li>
            <li><a href="logout.jsp">logout</a></li>
          </ul>
        </div>
      </div>
    </div>
  </div>

  <div>
    <% if(nrows.equals("1")){ %>
      <div style="height: 200px; left: 150px; position: absolute; top: 265px; width: 200px;">
        <h2>Pick a feature</h2>
      </div>
      <div id="board" style="height: 200px; left: 150px; position: absolute; top: 300px; width: 200px;"></div>
    <% }else if(nrows.equals("2")){ %>
      <div style="height: 200px; left: 130px; position: absolute; top: 215px; width: 200px;">
        <h2>Pick a feature</h2>
      </div>
      <div id="board" style="height: 300px; left: 130px; position: absolute; top: 250px; width: 300px;"></div>
    <% } else if(nrows.equals("3")){ %>
      <div style="height: 200px; left: 130px; position: absolute; top: 215px; width: 200px;">
        <h2>Pick a feature</h2>
      </div>
      <div id="board" style="height: 300px; left: 100px; position: absolute; top: 250px; width: 300px;"></div>
    <% }else{ %>
      <div id="board" style="height: 500px; left: 30px; position: absolute; top: 180px; width: 500px;"></div>
    <% } %>
  </div>

  <div id="game_score_box_1" style="text-align: center; left: 460px; position: absolute; top: 600px; width: 350px; z-index: 2;">
    <img style="left:0px; top:0px; position:absolute;" id="clayton1" width="100px" src="images/200px-Clayton.png"/>
    <h4>Your score</h4>
    <h1 id="game_score_1" style="text-align: center;">0</h1>
  </div>

  <div id="player1_title_area" style="left: 30px; position: absolute; top: 560px;">
    <h3>Your hand</h3>
  </div>

  <div id="player1" style="height: 500px; left: 30px; position: absolute; top: 535px;">
    <div id="hand_info_box_1" style="position: relative; top: 45px; width: 500px;">
      <div id="player_box_1" style="position: relative; top: 15px; width: 400px;">
        <table border='0'>
          <tr id="player1_hand" align='center' style='height: 75px;'></tr>
        </table>
      </div>
    </div>
  </div>

  <div id="game_score_box_2" style="text-align: center; left: 460px; position: absolute; top: 75px; width: 350px; z-index: 2;">
    <img style="left:0px; top:0px; position:absolute;" id="barney5" src="images/barney.png"/>
    <div>
      <strong style="text-align: center; font-size:20px;">Barney's score</strong><br/><br/>
      <strong id="game_score_2" style="text-align: center; font-size:30px;">0</strong>
    </div>
  </div>

  <div id="game_meta_info" style="text-align: center; left: 750px; position: absolute; top: 55px; z-index: 2; width:200px">
    <% if(dataset.equals("mammal")){
      String fs = "features that distinguish";
      if(max_hand.equals("1")){
        fs = "feature that distinguishes";
      }
    %>
    <strong>Pick <b><%=max_hand%></b> <%=fs %> mammals from other creatures.  Think of things that separate mammals from fish, insects, amphibians, reptiles...</strong>
    <% }else if(dataset.equals("dream_breast_cancer")){ %>
    <strong>Pick <b><%=max_hand%></b> genes that track breast cancer survival.  Look for genes that you think will have prognostic RNA expression or copy number variation.</strong>
    <% } %>
  </div>

  <div id="player2_title_area" style="left: 30px; position: absolute; top: 50px;">
    <h3>Barney's hand</h3>
  </div>

  <div id="player2" style="left: 30px; position: absolute; top: 25px;">
    <div id="hand_info_box_2" style="position: relative; top: 45px; width: 500px;">
      <div id="player_box_2" style="position: relative; top: 15px; width: 400px;">
        <table border='0'>
          <tr id="player2_hand" align='center' style='height: 75px; '></tr>
        </table>
      </div>
    </div>
  </div>

  <div id="infobox" style="height: 375px; left: 460px; position: absolute; top: 170px; width: 500px; overflow: scroll; padding:10;">
    <div id="infobox_header"><strong>Click on a <img src="images/info-icon.png"> for clues </strong></div>
    <div id="tabs">
      <ul>
        <% if(!(dataset.equals("mammal")||dataset.equals("zoo"))){ %>
          <li><a href="#gene_description">Gene</a></li>
          <li><a href="#ontology">Ontology</a></li>
          <li><a href="#rifs">Rifs</a></li>
        <% } %>
        <li><a href="#p1_current_tree">Yours</a></li>
        <li><a href="#p2_current_tree">Barney's</a></li>
      </ul>
      <%if(!(dataset.equals("mammal")||dataset.equals("zoo"))){ %>
      <div id="gene_description">
        <p>Gene description</p>
      </div>
      <div id="ontology">
        <p>Gene Ontology terms</p>
      </div>
      <div id="rifs">
        <p>Gene References into Function</p>
      </div>
      <% } %>
      <div id="p1_current_tree" style="left: 5px; position: absolute; top: 50px; width: 400px;">
        <p>Your decision tree will be displayed here.</p>
      </div>
      <div id="p2_current_tree" style="left: 5px; position: absolute; top: 50px; width: 400px;">
        <p>Barney's decision tree will be displayed here.</p>
      </div>
    </div>
  </div>

  <div id="endgame" style="height: 410px; left: 30px; position: absolute; top: 175px; width: 400px; background-color: #F2F2F2; z-index:3; overflow: scroll;">
    <div style="margin-top:10px; margin-left:10px;">
      <h1>Round Over</h1>
      <div id="winner"  ></div><br>
      <input class="save_hand" id="holdem_button" type="submit" value="Try another board" />
    </div>
  </div>

  <jsp:include page="footer.jsp" />
  <script src="js/libs/jquery-1.8.0.min.js"></script>
  <script src="js/libs/jquery.sparkline.min.js"></script>
  <script src="js/libs/underscore-min.js"></script>
  <script src="js/libs/d3.v2.min.js"></script>
  <script src="js/tree.js"></script>
  <script src="js/cure.js"></script>
  <jsp:include page="js/analytics.js" />

</body>
</html>


