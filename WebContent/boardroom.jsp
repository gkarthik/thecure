<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%@ page import="org.scripps.combo.Config"%>
<%@ page import="org.scripps.combo.Player"%>
<%@ page import="org.scripps.combo.Board"%>
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
        <jsp:include page="js/analytics.js" />
    </head>
    <body>
    <div class="navbar navbar-fixed-top">
		<div class="navbar-inner"
			style="background-color: blue; background-image: -webkit-linear-gradient(top, blue, black);">
			<div class="container">
				<a class="btn btn-navbar" data-toggle="collapse"
					data-target=".nav-collapse"> <span class="icon-bar"></span> <span
					class="icon-bar"></span> <span class="icon-bar"></span> </a> <a
					class="brand">Breast Cancer 10 Year Survival</a>
				<div class="nav-collapse">
					<ul class="nav">
						<li><a href="contact.jsp">Contact</a>
						</li>
						<li><a href="logout.jsp">logout</a>
						</li>
					</ul>
				</div>
				<!--/.nav-collapse -->
			</div>
		</div>
	</div>

	<div class="container">
		<div class="hero-unit">
			<div class="row">
				<h2>Breast Cancer 10 Year survival</h2>
				<p>The goal of this game is to use gene expression levels in
					breast cancer tumors to predict 10 year survival. If a high quality
					signature can be identified it can be used to modify treatment
					accordingly.</p>
				<p>
					You must defeat your nemesis Barney <img width="25"
						src="images/barney.png">. To win each round, find the best
					combination of genes to use to classify a new sample.
				</p>
				<br>
			</div>
			<div class="row">
				<div id="boards" class="span8"></div>
				<div id="back" class="span2">
					<jsp:include page="scoreboard_table.jsp" />
				</div>


			</div>
		</div>
	</div>




  <script src="js/libs/jquery-1.8.0.min.js"></script>
  <script src="js/libs/underscore-min.js"></script>
  <script src="js/libs/d3.v2.min.js"></script>
  <script type="text/javascript" charset="utf-8">

  function drawGrid(targetEl, data, box_size) {
    //double check the sort/ordering by difficulty
    data.boards = _.sortBy(data.boards, function(obj){ return -obj.base_score; })

      // Check to ensure the board is/can be a grid
      // this will clip the most difficult boards // thoughts?
      var base = Math.sqrt( data.boards.length );
      if ( Math.floor(base) !== base) {
        data.boards = _.first(data.boards, Math.pow( Math.floor(base), 2 ) );
      }

      var attempt = d3.scale.linear()
        .domain([0, 10])
        .range([0, 100]);

      var targetEl = $(targetEl),
          hw = box_size,
          text_size = Math.floor(hw*.435),
          margin = Math.floor(hw*.09),
          top_pos = Math.floor(hw*.09),
          pro_height = Math.floor(hw*.15);

      _.each(data.boards, function(v, i) {
        var scaleAttempt = attempt(v.attempts),
            isEnabled,
            content,
            font_size = text_size;
       ( v.enabled == true ) ? isEnabled = "enabled" : isEnabled = "disabled"; 

       if ( v.enabled == false ) {
          content = "•";
          font_size = font_size*3;
          top_pos = -(hw*.4);
        }
        if ( v.trophy == true) {
          content = "★";
          font_size = font_size*0.4;
          top_pos = Math.floor(hw*.09);
        }
        if ( v.enabled == false && v.trophy == true) {
          content = "★";
          top_pos = Math.floor(hw*.09);
        }

        if ( v.enabled == true && v.trophy == false && v.attempts < 10 ) {
          content = (v.position+1);
          top_pos = Math.floor(hw*.2);
        }
        var board = targetEl.append("\
            <div id='board_"+ v.board_id +"' class='board "+ isEnabled +"' style='height:"+ hw +"px; width:"+ hw +"px; font-size:"+ font_size +"px; margin:"+ margin +"px' > \
              <span class='symbol' style='top:"+ top_pos +"px'>"+ content +"</span>\
              <div class='score_slider' style='width:"+ hw +"px; height:"+ pro_height +"px;'>\
                <div class='score_value' style='width:"+ scaleAttempt +"%; height:"+ pro_height +"px;'></div>\
              </div>\
              </div>");
      })

      $.each( $("div.board.enabled"), function(i, v) {
          var board_id = $(this).attr('id').split('_')[1];
          $(this).click(function(e) {
            var url = "boardgame.jsp?level="+board_id+"&mosaic_url=boardroom.jsp&dataset=dream_breast_cancer&title=Breast Cancer Survival&nrows=5&ncols=5&max_hand=5";
            window.location.href = url;
          })
      })

  }

  $(document).ready(function() {
      var username = '<%=username%>';
      var phenotype = "dream_breast_cancer";
      var url = "/cure/SocialServer?command=boardroom&username="+username+"&phenotype="+phenotype;
        $.getJSON(url, function(data) {
          drawGrid("#boards", data, 35);
        });
  });
  </script>
  </body>
</html>
<%} %>
