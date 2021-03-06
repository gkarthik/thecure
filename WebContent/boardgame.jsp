<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%@ page import="org.scripps.combo.weka.Weka"%>
<%@ page import="org.scripps.combo.GameLog"%>
<%@ page import="org.scripps.combo.model.Player"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.ArrayList"%>
<%
  int player_id = 0;
  int player_experience = 0;
  Player player = (Player) session.getAttribute("player");
    if (player == null) {
      response.sendRedirect("login.jsp");
    } else {
      player_id = player.getId();
      player_experience = 1;
    }
    if (player != null) {
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

    <title>The Cure: ...</title>
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
            <li><a href="cure2.0/index.jsp">Advanced Cure</a></li>
            <li><a href="logout.jsp">logout</a></li>
          </ul>
        </div>
      </div>
    </div>
  </div>

  <div id="boardgame">
    <div id="game_meta_info"></div>

    <div id="p2_area">
      <div id="p2_hand"></div>
      <div id="p2_scorebox">
        <img id="barney5" class="avatar" src="images/barney.png" />
        <h2>Barney's score: <span id="p2_score">0</span></h2>
      </div>
    </div>

    <div id="game_area">
      <div id="board"></div>

      <div id="help_area">
      <div id="tabs">
          <ul>
            <li class="ontology highlight">Ontology</li>
            <li class="rifs">Rifs</li>
            <li class="p1_current_tree">Yours</li>
            <li class="p2_current_tree">Barney's</li>
          </ul>
          <input id="search" placeholder="Search..." />
        </div>

        <div id="infoboxes">
          <div class="infobox" id="ontology">
            <p>Gene Ontology terms</p>
          </div>
          <div class="infobox" id="rifs" style="display: none;">
            <p>Gene References into Function</p>
          </div>
          <div class="infobox" id="p1_current_tree" style="display: none;">
            <p>Your decision tree will be displayed here.</p>
          </div>
          <div class="infobox" id="p2_current_tree" style="display: none;">
            <p>Barney's decision tree will be displayed here.</p>
          </div>
        </div>
      </div>

      <div id="endgame" style="display: none;"></div>
      <div id="modal" style="display: none;"></div>
    </div>

    <div id="p1_area">
      <div id="p1_hand"></div>
      <div id="p1_scorebox">
        <img id="clayton1" class="avatar" src="images/200px-Clayton.png"/>
        <h2>Your score: <span id="p1_score">0</span></h2>
      </div>
    </div>

  </div>

  <jsp:include page="footer.jsp" />
  <script>
    var cure_user_experience = "<%=player_experience%>",
    cure_user_id = "<%=player_id%>";
  </script>
  <script src="js/libs/jquery-1.8.0.min.js"></script>
  <script src="js/libs/jquery.plugins.js"></script>
  <script src="js/libs/underscore-min.js"></script>
  <script src="js/libs/d3.v2.min.js"></script>
  <script src="js/cure.js"></script>
  <jsp:include page="js/analytics.js" />

</body>
</html>
<%} %>
