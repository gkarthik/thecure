<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>

<!DOCTYPE html>
<html>
  <head>
    <meta http-equiv="Content-type" content="text/html; charset=utf-8">
    <title>Help</title>
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
          <li><a href="cure2%2E0/index.jsp">Cure 2.0</a></li>
            <li><a href="contact.jsp">Contact</a></li>
            <li><a href="boardroom.jsp">Boardroom</a></li>
          </ul>
        </div><!--/.nav-collapse -->
      </div>
    </div>
  </div>

  <div class="container">
    <div class="hero-unit">
      <div class="row">
        <div  id="games">
          <h2>Help defeat <img src="images/barney.png">!</h2>
          <div id="game result">

            <h3 class="instructions">Instructions</h3>
              <div id="instructions">
                <p>Once you pass training, you can select any of the open games in the <a href="boardroom.jsp">board room</a>.  These are indicated with a number like <img src="images/number.png">.  The pink line on the bottom of the icon shows how many players have finished that board.  We hope to have all of the boards explored (and defeated!) by many players.  Once you complete a board, it will be displayed as a <img src="images/star.png"> and you will need to choose a different open board to play.  When a certain number of players have defeated a particular board, that board will be closed and displayed as a <img src = "images/circle.png"> </p>
                <p><strong>Basic game play</strong>: maximize your score by choosing the best combination of genes.</p>
                <p>Click the name of a gene to add it to your hand. Your score will be calculated each time your hand is changed.  Careful though!  Once you add a card to your hand it will stay there for the whole round - no takebacks!</p>
                <p>After your turn, your opponent Barney will select a card just like you did and then it will be your turn again.  After you both have 5 cards, the round is over and the player with the highest score wins the round.</p>
                <p>Mouse over the <img src="images/info-icon.png"> card corners to reveal information about a gene.  Use the search box to search the gene annotations.  Matching genes will be highlighted.</p>
              </div>
             <h3 class="cheating">Cheating</h3>
              <div id="cheating" style="display: none";>
                <p>We have identified a few ways to use browser tricks to maximize your score.  While we work to make this impossible, please don't cheat..  We are trying to cure cancer here!  Play, have fun, experiment - but don't cheat.  (We can tell who you are..)</p>
              </div>
            <h3 class="scoring">Scoring</h3>
              <div id="scoring" style="display: none";>
                <p>Your score is determined by using the genes that you select to train machine learning algorithms to classify real biological samples. The better the genes reflect the phenotype, the better you will score in the game.  This all happens behind the scenes in real time!</p>
                <p>For the geeks, we are using the <a href="http://www.cs.waikato.ac.nz/ml/weka/">WEKA</a> implementation of Quinlan's C4.5 decision tree learning algorithm and performing a 10 fold cross-validation to produce each game score.</p>
              </div>
          </div>
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
