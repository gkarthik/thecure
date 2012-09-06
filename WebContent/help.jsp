<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>

<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-type" content="text/html; charset=utf-8">
<title>Defeat Barney!</title>
<link rel="stylesheet" href="assets/css/combo_bootstrap.css" type="text/css" media="screen">
<link rel="stylesheet" href="assets/css/combo.css" type="text/css" media="screen">

	<link href="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8/themes/base/jquery-ui.css" rel="stylesheet" type="text/css" />
	<script src="//ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js" type="text/javascript"></script>
	<script	src="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8/jquery-ui.min.js"></script>
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
		<p></p>
		<br>
		<div id="game result">
			<h1>Instructions</h1>
		<p>Basic game play: maximize your score by choosing the best combination of genes</p>
		<p>Click the name of a gene to add it to your hand. Your score will be calculated each time your hand is changed.</p>  
		<p>Careful though!. Once you add a card to your hand it will stay there for the whole round - no takebacks! </p>
		<p>After your turn, your opponent Barney will select a card just like you did and then it will be your turn again.</p>
		<p>After you both have 5 cards, the round is over and the player with the highest score wins the round.</p>
		<p>Click on the <img src="images/info-icon.png"> buttons to reveal information about a gene.</p>
		<h2>Scoring</h2>
		<p>Your score is determined by using the genes that you select to train machine learning algorithms to classify real biological samples. 
		The better the genes reflect the phenotype, the better you will score in the game.  This all happens under the hood in real time!  
		</p>
		<p>
		For the geeks,
		we are using Quinlan's C4.5 decision tree learning algorithm and performing a 10 fold cross-validation to produce the score.
		</p>

			
		</div>	


    </div>
    
   
</div>
</div>
</div>

</body>
</html>