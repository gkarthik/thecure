<!DOCTYPE html>
<!--[if lt IE 7]>      <html class="no-js lt-ie9 lt-ie8 lt-ie7"> <![endif]-->
<!--[if IE 7]>         <html class="no-js lt-ie9 lt-ie8"> <![endif]-->
<!--[if IE 8]>         <html class="no-js lt-ie9"> <![endif]-->
<!--[if gt IE 8]><!--> <html class="no-js"> <!--<![endif]-->
    <head>
        <meta charset="utf-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
        <title>The Cure: Play Games, Defeat Cancer</title>
        <meta name="description" content="">
        <meta name="viewport" content="width=device-width">

        <link rel="stylesheet" href="assets/css/landing-style.css">
    </head>
    <body>
        <!--[if lt IE 7]>
            <p class="chromeframe">You are using an outdated browser. <a href="http://browsehappy.com/">Upgrade your browser today</a> or <a href="http://www.google.com/chromeframe/?redirect=true">install Google Chrome Frame</a> to better experience this site.</p>
        <![endif]-->

        <!-- Add your site or application content here -->
        <div id="topBar"></div>
        <div id="main">
        <center>
          	<img src="images/branch/logo.png" />
          	</center>
          <div id="column1">
          <div id="sections" class="section">
            <h3 class="about">About</h3>
            <div id="about" style="display: none;">
              <p>Branch is a new Web-based tool for the interactive construction of decision trees from genomic datasets. Branch offers the ability to</p>
              <ol class="unordered-list"> 
	              <li>Construct decision trees by manually selecting features such as genes for a gene expression dataset.</li> 
	              <li>Collaboratively edit and built decision trees. </li>
	              <li>Create feature functions that aggregate content from multiple independent features into single decision nodes (e.g. pathways)</li> 
	              <li>Evaluate decision tree classifiers in terms of precision and recall.</li> 
              </ol>
              <p>The tool is optimized for genomic use cases through the inclusion of gene and pathway-based search functions. </p>
            </div>
            <h3 class="background">Background</h3>
            <div id="background" style="display: none;">
              <p>A crucial task in modern biology is the prediction of complex phenotypes, such as breast cancer prognosis, from genome-wide measurements.  Machine learning algorithms can sometimes infer predictive patterns, but there is rarely enough data to train and test them effectively and the patterns that they identify are often expressed in forms (e.g. support vector machines, neural networks, random forests composed of 10s of thousands of trees) that are highly difficult to understand. </p>
            </div>
            
            <h3 class="contact">Contact</h3>
            <div id="contact" style="display: none;">
            <p>Please feel free to get in touch with us via email, twitter, messenger pigeon etc.  See our details on the <a href="contact.jsp">contact page</a>.
            </div>
            <!--
            <h3 class="faq">FAQ</h3>
            <div id="faq" style="display: none;">
              <ol>
                <li><h4>Who can play?</h4>
                  <p>Anyone is welcome to play.  The more you know about biology and disease at the level of gene function, the better you are likely to do, but you can also learn as you go.  The game provides a lot of information about the genes as well as links off to related Web resources.  We hope that anyone who plays will learn something about gene function.</p>
                </li>
                <li><h4>How do you evaluate the quality of the data provided by game players?</h4>
                  <p>The predictors generated using The Cure data are evaluated for accuracy on independent test datasets - just like any other predictor inferred by experts or by statistics would be.
              By testing on real data, we can tell the good players apart from those that are guessing randomly.  Since each player action in the game is associated with their account, it is then very easy to filter out data that is not useful.  
              This approach, while it may seem strange for a scientific project, follows the &lsquo;publish then filter&rsquo; approach that has made the Web so successful.  We hope that it encourages many people to share their time and their intelligence with the project.</p>
              </li>
              </ol>
              </div>
			-->
          </div>
          </div>
          <div id="column2">

            <div id="chart"></div>
            <div id="action">
              <input class="playnow" type="submit" value="Play Now" />
            </div>
         <!--  <h4>Round 3 started! <br/>View results from <a href="round1.jsp"> Round 1</a>, <a href="round2.jsp"> Round 2 </a></h4> --> 
          <div id="twitterUserFeed">
              <a class="twitter-timeline" href="https://twitter.com/genegame" data-widget-id="245564915832721410">Tweets by @genegame</a>
              <script>!function(d,s,id){var js,fjs=d.getElementsByTagName(s)[0];if(!d.getElementById(id)){js=d.createElement(s);js.id=id;js.src="//platform.twitter.com/widgets.js";fjs.parentNode.insertBefore(js,fjs);}}(document,"script","twitter-wjs");</script>
            </div>

          </div>

        </div>
  <jsp:include page="footer.jsp" />
  <script src="js/libs/jquery-1.8.0.min.js"></script>
  <script src="js/libs/jquery.plugins.js"></script>
  <script src="js/libs/underscore-min.js"></script>
  <script src="js/libs/d3.v2.min.js"></script>
  <script src="js/cure.js"></script>
  <jsp:include page="js/analytics.js" />

  </body>
</html>
