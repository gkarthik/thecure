<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%@ page import="org.scripps.combo.model.Player"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.Map"%>
<%@ page import="java.util.ArrayList"%>

<%
  String username = "";
  Player player = (Player) session.getAttribute("player");
  if (player == null) {
    response.sendRedirect("login.jsp");
  } else {
    username = player.getName();
  }
  if (player != null) {
    int levels_passed = 0;
    player.setBoardScoresWithDb();
    Map<Integer, Integer> player_board_scores = player.getDataset_board_scores().get("mammal");
    List<Integer> zoo_scores = new ArrayList<Integer>();
    if (player_board_scores != null && player_board_scores.values() != null) {
      zoo_scores = new ArrayList<Integer>(player_board_scores.values());
    }

    boolean passed_one = false;
    for (int i = 0; i < zoo_scores.size(); i++) {
      if (zoo_scores.get(i) > 0) {
        levels_passed = i;
        passed_one = true;
      }
    }
    if (passed_one) {
      levels_passed++;
    }
    if(levels_passed==4) {
      response.sendRedirect("boardroom.jsp");
    }
%>

<!DOCTYPE html>
<html>
  <head>
  <meta http-equiv="Content-type" content="text/html; charset=utf-8">
  <title>Training Room</title>

  <link rel="stylesheet" href="assets/css/combo.css" type="text/css"	media="screen">
  <link rel="stylesheet" href="assets/css/combo_bootstrap.css" type="text/css" media="screen">
  <link rel="stylesheet" href="assets/css/style.css" type="text/css" media="screen">

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
        <a class="brand" href="./">The Cure : training</a>
        <div class="nav-collapse">
          <ul class="nav">
          <li><a href="cured3/index.jsp">Advanced Cure</a></li>
            <li><a href="contact.jsp">Contact</a></li>
            <li><a href="logout.jsp">logout</a></li>
          </ul>
        </div>
      </div>
    </div>
  </div>

  <div class="container training">
    <div class="hero-unit">
      <div class="row">
        <div class="span8">
          <h2>Training Summary</h2>
          <p>Before you get started with The Cure, please take a moment to play through a few simple examples. These will help to get you familiar with the game interface and give you some points to get you started.</p>
          <p>In this training stage, your objective is to identify characteristics (e.g. number of legs, breathes air, etc.) that can be used to classify any animal into one of two groups: mammals and not mammals.  To win, you must pick the right characteristic before your opponent Barney <img width="25" src="images/barney.png">!</p>
          <p>Click on the numbered tiles below to play. Defeat Barney at each level to advance to the Breast Cancer challenge.</p>
          <br>
        </div>
      </div>

      <div class="row">
        <br/>
        <div id="shrew" class="span3 offset3">
          <table>
            <%
            int level = -1; int board_id = 200;
            int num_tile_rows = 2;
            int num_tile_cols = 2;

            for (int i = 0; i < num_tile_rows; i++) {
              String board_size = "&h=1";
              if (i > 0) {
                board_size = "&h=2";
              }
              String gps = "&t=Training: Mammal Challenge" + board_size;
            %>
            <tr>
              <%
              for (int j = 0; j < num_tile_cols; j++) {
                board_id++;
                level++;
                int score = 0;
                if (zoo_scores.size() > level) {
                  score = zoo_scores.get(level);
                }
              %>
              <td><div id="level_<%=level%>">
              <%
              if (levels_passed == level) {
              %>
              <a href="boardgame.jsp?b=<%=board_id%><%=gps%>" class="btn btn-large btn-primary "><div class="big_level_button"><%=level + 1%></div></a>
              <%
              } else if (levels_passed > level) {
              %>
              <p><span style="font-size: 100px; color: #B2365F;">&#9733;</span></p>
              <% } else { %>
                <div class="btn btn-large btn-primary disabled">
                  <img src="images/lock-6-64.png">
                </div>
                <% } %>
              </div>
            </td>
            <% } %>
          </tr>
          <% } %>
        </table>
        <p>Training levels 1-4</p>
      </div>
    </div>
  </div>
</div>

  <jsp:include page="footer.jsp" />
  <script src="js/libs/jquery-1.8.0.min.js"></script> 
  <script src="js/libs/underscore-min.js"></script>
  <script src="js/libs/d3.v2.min.js"></script>
  <script src="js/cure.js"></script>
  <jsp:include page="js/analytics.js" />
</body>
</html>
<%} %>
