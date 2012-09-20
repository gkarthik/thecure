<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%@ page import="org.scripps.combo.GameLog"%>
<%@ page import="org.scripps.combo.model.Player"%>
<%@ page import="org.scripps.combo.model.Hand"%>
<%@ page import="java.util.List"%>
<%
  GameLog log = new GameLog();
  List<Hand> whs = Hand.getTheFirstHandPerPlayerPerBoard(true);
  GameLog.high_score sb = log.getScoreBoard(whs);
  Player player = (Player) session.getAttribute("player");
  boolean show_player = false;
  if(player!=null&&(!player.getName().equals("anonymous_hero"))){
    show_player = true;
  }
%>

  <div id="leaderboard">
    <h2>Leader Board</h3>
    <h3>
      <span class="rank">Rank</span>
      <span class="player">Player</span>
      <span class="points">Points</span>
    </h3>
    <ol>
      <%
      int max = 10;
      int r = 0;
      for(String name : sb.getPlayer_global_points().keySet()){
      r++;
      String displayName = name;
      if(name == null || name.length() == 0) {
        displayName = "anon";
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
      </li>
      <% }} %>
    </ol>
  </div>
