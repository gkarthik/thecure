<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%@ page import="org.scripps.combo.model.Player"%>
<%@ page import="org.scripps.combo.model.Board"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.Map"%>
<%@ page import="java.util.ArrayList"%>
<%
//params for game board
String game_params = "&mosaic_url=boardroom.jsp&dataset=dream_breast_cancer&title=Breast Cancer Survival&nrows=5&ncols=5&max_hand=5";
int level = 0;
int num_tile_rows = 10;
int num_tile_cols = 10;

boolean all_levels_open = true;
	String username = "";
	Player player = (Player) session.getAttribute("player");
	if (player == null) {
		response.sendRedirect("login.jsp");
	} else {
		username = player.getName();
	} 
	if (player != null) {
%>
<!DOCTYPE html>
<!--[if lt IE 7]>      <html class="no-js lt-ie9 lt-ie8 lt-ie7"> <![endif]-->
<!--[if IE 7]>         <html class="no-js lt-ie9 lt-ie8"> <![endif]-->
<!--[if IE 8]>         <html class="no-js lt-ie9"> <![endif]-->
<!--[if gt IE 8]><!--> <html class="no-js"> <!--<![endif]-->
    <head>
        <meta charset="utf-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
        <title></title>
        <meta name="description" content="">
        <meta name="viewport" content="width=device-width">
        <link rel="stylesheet" href="assets/css/board.css">
        <link rel="stylesheet" href="assets/css/combo_bootstrap.css" type="text/css" media="screen">
        <link rel="stylesheet" href="assets/css/combo.css" type="text/css" media="screen">
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
            <a class="brand" href="/cure/">The Cure</a>
            <div class="nav-collapse">
              <ul class="nav">
                <li><a href="contact.jsp">Contact</a></li>
                <li><a href="logout.jsp">logout</a></li>
              </ul>
            </div>
          </div>
        </div>
      </div>

      <div class="container boardroom">
        <div class="hero-unit">
          <div class="row">
            <h2>DREAM7 challenge: predict breast cancer survival</h2>
              <p>Your objective is to identify genes that can be used to classify tumor samples into one of two prognostic groups: 'poor' and 'good'.  'Good' suggests that the patient is likely to survive more than 10 years from the time of diagnosis. Poor suggests that, without major intervention, the patient is not likely to survive beyond 10 years. (We did mention that this was a serious game...) To win, you must pick the right genes before Barney <img width="25" src="images/barney.png">.

              <p>Click on the numbered tiles below to play. Take your time, ask your friends or search the internet for help if you get stuck. This is not going to be easy, give it your best shot! <a href="help.jsp#data"><span style="color: #B2365F;">More..</span></a></p>
              <br/>
          </div>
          <div class="row">
            <div id="boards" class="span7"></div>
            <div id="back" class="span3">
              <jsp:include page="scoreboard_table.jsp" />
            </div>
          </div>
        </div>
      </div>

  <jsp:include page="footer.jsp" />
  <script>
    var cure_dataset = "<%=dataset%>",
        cure_user_experience = "<%=player_experience%>",
        cure_user_id = "<%=player_id%>";
  </script>
  <script src="js/libs/jquery-1.8.0.min.js"></script>
  <script src="js/libs/underscore-min.js"></script>
  <script src="js/libs/d3.v2.min.js"></script>
  <script src="js/cure.js"></script>
  <jsp:include page="js/analytics.js" />

  </body>
</html>
<%} %>
