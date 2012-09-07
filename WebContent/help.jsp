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
          <div id="game result">
            <h1>Instructions</h1>
            <p>Basic game play: maximize your score by choosing the best combination of genes.</p>
            <p>Click the name of a gene to add it to your hand. Your score will be calculated each time your hand is changed.  Careful though!  Once you add a card to your hand it will stay there for the whole round - no takebacks! </p>
            <p>After your turn, your opponent Barney will select a card just like you did and then it will be your turn again.  After you both have 5 cards, the round is over and the player with the highest score wins the round.</p>
            <p>Click on the <img src="images/info-icon.png"> buttons to reveal information about a gene.</p>
          <h2>Scoring</h2>
            <p>Your score is determined by using the genes that you select to train machine learning algorithms to classify real biological samples. The better the genes reflect the phenotype, the better you will score in the game.  This all happens behind the scenes in real time!</p>
            <p>For the geeks, we are using the <a href="http://www.cs.waikato.ac.nz/ml/weka/">WEKA</a> implementation of Quinlan's C4.5 decision tree learning algorithm and performing a 10 fold cross-validation to produce each game score.</p>
          <h2 id="data">The Data</h2>
       <p>The data used to run this game is the same as is provided by the SAGE7 challenge to all participants.  (Note that we are not affiliated with SAGE7 beyond acting as participants. )  
       For detailed information, check out their <a href="https://sagebionetworks.jira.com/wiki/display/BCC/Breast+Cancer+Challenge%3A+Detailed+Description">technical information</a>.
       When you select genes in the game, predictive models are constructed using information from both gene expression and copy number variation.  Clinical information is
       not included at this time.  Each board contains a different set of genes.</p>
       <p>The training dataset comes from the METABRIC cohort of 2,000 breast cancer samples and includes detailed clinical annotations, 10 median year survival time, gene expression, and copy number data for 1’000 samples.
Gene expression levels have been selected based on probe match quality and signal intensity compared to the overall median, then ranked based on their variance across the  samples. We selected the probes with the highest variance corresponding to 1500 unique genes.
Similarly, we ranked copy number data based on the sum of squares across the  samples, and selected the top 1000 unique genes.
Finally, we merged CNV, gene expression and survival data in a single table with 2500 unique genes. This table is used to generate the hundred boards in The Cure.</p>
          </div>
        </div>
      </div>
    </div>
  </div>
  <jsp:include page="footer.jsp" />
  <script src="//ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js" type="text/javascript"></script>
  <script	src="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8/jquery-ui.min.js"></script>
  <jsp:include page="js/analytics.js" />
</body>
</html>
