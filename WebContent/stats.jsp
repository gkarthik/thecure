<!DOCTYPE html>
<!--[if lt IE 7]>      <html class="no-js lt-ie9 lt-ie8 lt-ie7"> <![endif]-->
<!--[if IE 7]>         <html class="no-js lt-ie9 lt-ie8"> <![endif]-->
<!--[if IE 8]>         <html class="no-js lt-ie9"> <![endif]-->
<!--[if gt IE 8]><!--> <html class="no-js"> <!--<![endif]-->
    <head>
        <meta charset="utf-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
        <title>Stats</title>
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

      <div class="container stats">
        <div class="hero-unit">
          <div class="row">
            <h2>Games won per day</h2>
            <div id="games_won" class="stats_graph"></div>
          </div>

          <div class="row">
            <h2>Leader Board</h2>
            <div id="leaderPie"></div>
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
