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
              <p>The Cure is a serious, biology-based card game. Assemble the best hands and you can win - just like poker. The challenge with The Cure is that we don't know the rules yet! Until you play, we can't tell you if you have the equivalent of a royal flush or a pair of 2s. By playing the game, you will be both teaching and learning the rules of nature.</p>
            </div>
            <h3 class="background">Background</h3>
            <div id="background" style="display: none;">
              <p>Using the latest technology we can now assess the activity level of more than 20,000 known human genes and identify literally millions of variations in every human genome. The challenge now is to make use of all of this data. For example, if a woman has been diagnosed with breast cancer we would like to be able to predict whether the cancer will metastasize and how quickly because we can use this information to make decisions about treatment - e.g. whether or not she should receive chemotherapy. Given a collection of samples from tumors that did and did not end up spreading, we take measurements using all the technology at our disposal and then attempt to identify consistent patterns. If a particular gene is much more active than normal in all the samples that eventually spread, this might be a good signal to look for in future patients.</p>
            </div>
            <h3 class="challenge">Challenge</h3>
            <div id="challenge" style="display: none;">
              <p>In seeking these patterns we are faced with two key problems, reproducibility and a combinatorial explosion. First, it often happens that patterns observed in one dataset do not appear with the same strength in new datasets. This is partly caused by natural biological variability and partly by the relative immaturity of the measuring technology that is being applied. Aside from reproducibility, the patterns themselves may be very complex. It may take a unique combination of 25 different variables to produce an excellent predictive pattern. The trouble is that it is impossible to test all such potential combinations. If we assume 20,000 human genes, there are on the order of 10<sup>82</sup> possible 25 gene combinations! (People estimate that the <a href="http://en.wikipedia.org/wiki/Observable_universe">total number of atoms in the universe</a> is close to 10<sup>80</sup>.)</p>
            </div>
            <h3 class="idea">The idea</h3>
            <div id="idea" style="display: none;">
              <p>One advantage when it comes to addressing these problems is our increasing understanding of biology. Our knowledge of genes, biological mechanisms and disease is growing exponentially and much of this knowledge is available on the Web. When we start looking for predictive patterns in a dataset such as the breast cancer example, we can use this knowledge to guide our search. TheCure was created as a fun way to solicit help in guiding the search for stable patterns that can be used to make biologically and medically important predictions.  When people play TheCure they use their knowledge (or their ability to search the Web or their social networks) to make informed decisions about the best combinations of variables (e.g. genes) to use to build predictive patterns. These combos are the &lsquo;hands&rsquo; in TheCure card game. Every time a game is played, the hands are evaluated and stored. Eventually predictors will be developed using advanced machine learning algorithms that are informed by the hands played in the game.</p>
            </div>
            <h3 class="mission">Current mission: <br /> <span class="pink">Griffith MetaStudy</span></h3>
            <div id="mission" style="display: none;">
              <p>The Cure team is currently working on creating the world&#8217;s best genomics-driven predictor of breast cancer prognosis.  To meet this ambitious objective, we are currently recruiting players with knowledge (or an interest in learning about) cancer biology.  <a href="login.jsp">Join us now!</a></p>
            </div>
            <h3 class="contact">Contact</h3>
            <div id="contact" style="display: none;">
            <p>Please feel free to get in touch with us via email, twitter, messenger pigeon etc.  See our details on the <a href="contact.jsp">contact page</a>.
            </div>
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
