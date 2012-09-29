<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%@ page import="org.scripps.combo.GameLog"%>
<%@ page import="org.scripps.combo.model.Player"%>
<%@ page import="org.scripps.combo.model.Game"%>
<%@ page import="java.util.List"%>
<%
  GameLog log = new GameLog();
  String dataset = "dream_breast_cancer"; //use dream_breast_cancer for round 1 data 
  List<Game> whs = Game.getTheFirstGamePerPlayerPerBoard(true, dataset);
  GameLog.high_score sb = log.getScoreBoard(whs);
  Player player = (Player) session.getAttribute("player");
  boolean show_player = false;
  if(player!=null&&(!player.getName().equals("anonymous_hero"))){
    show_player = true;
  }
  
%>
<!DOCTYPE html>
<html>
  <head>
    <meta http-equiv="Content-type" content="text/html; charset=utf-8">
    <title>Round 1 results</title>
    <link rel="stylesheet" href="assets/css/combo_bootstrap.css" type="text/css" media="screen">
    <link rel="stylesheet" href="assets/css/combo.css" type="text/css" media="screen">
    <link rel="stylesheet" href="assets/css/style.css" type="text/css" media="screen">
    <link href="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8/themes/base/jquery-ui.css" rel="stylesheet" type="text/css" />
    
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
        <a class="brand" href="/cure/">The Cure</a>
        <div class="nav-collapse">
          <ul class="nav">
            <li><a href="contact.jsp">Contact</a></li>
            <li><a href="boardroom.jsp">Boardroom</a></li>
            <li><a href="help.jsp">Help</a></li>
          </ul>
        </div><!--/.nav-collapse -->
      </div>
    </div>
  </div>

  <div class="container">
    <div class="hero-unit">
      <div class="row">
        <div  id="results">
          <div id="leaderboard">
    <h2>Scoreboard for first round</h3>
    <h3><a href="boardroom.jsp">Play current round!</a></h3>
    <h3>
      <span class="rank">Rank</span>
      <span class="player">Player</span>
      <span class="points">Points</span>
      <span class="avg">Average score</span>
      <span class="max">Max score</span>
    </h3>
    <ol>
    <%
      int max = 100;
      int r = 0;
      for(String name : sb.getPlayer_global_points().keySet()){
        r++;
        String displayName = name;
        if(name == null || name.length() == 0) {
          displayName = "anon";
        }
        if(name.length() > 14) {
          displayName = name.substring(0, 13);
        }
        if(r<=max||player.getName().equals(name)){
          if(show_player&&player.getName().equals(name)){
        %>
            <li class="currentPlayer">
        <% } else { %>
            <li>
        <% } %>
          <span class="rank"> <%=r%> </span>
          <span class="player"> <%=displayName%> </span>
          <span class="points"> <%=sb.getPlayer_global_points().get(name)%> </span>
          <span class="avg"> <%=sb.getPlayer_avg().get(name)%> </span>
          <span class="max"> <%=sb.getPlayer_max().get(name)%> </span>
        </li>
      <% }} %>
    </ol>
  </div>
        </div>
      </div>
    </div>
  </div>

  <jsp:include page="footer.jsp" />
  <jsp:include page="js/analytics.js" />

</body>
</html>

