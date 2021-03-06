<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
  <!-- I'm a comment to test pushing -->
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <title>About TheCure</title>
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
          <a class="brand" href="/cure/">TheCure</a>
          <div class="nav-collapse">
            <ul class="nav">
              <li><a href="contact.jsp">Contact</a></li>
              <li><a href="login.jsp">Play</a></li>
            </ul>
          </div>
        </div>
      </div>
    </div>

    <div class="container">
      <div class="hero-unit">
        <div class="row">
          <div class="span10">
            <div id="content" class="about">
              <h1>Synopsis</h1>
              <p>TheCure is a card game (and a hard game!).  Assemble the best hands and you can win - just like poker.  The challenge with TheCure is that we don't know the rules yet!  Until you play, the game can't tell you if you have the equivalent of a royal flush or a pair of 2s.  By playing the game, you will be both teaching and learning the rules of nature.</p>
              <h2>Background</h2>
              <p>Using the latest technology it is now possible to measure enormous numbers of biological variables.  We can now assess the activity level of more than 20,000 known human genes and identify literally millions of variations in every human genome.  The challenge now is to make use of all of this data.  For example, if a woman has been diagnosed with breast cancer we would like to be able to predict whether the cancer will spread and how quickly because we can use this information to make decisions about treatment - e.g. whether or not she should receive chemotherapy.  Given a collection of samples from tumors that did and did not end up spreading, we take measurements using all the technology at our disposal and then attempt to identify consistent patterns.  If a particular gene is much more active then normal in all the samples that eventually spread, this might be a good signal to look for in future patients.</p>
              <h2>Challenge</h2>
              <p>In seeking these patterns we are faced with two key problems, reproducibility and a combinatorial explosion.  First, it often happens that patterns observed in one dataset do not appear with the same strength in new datasets.  This is partly caused by natural biological variability and partly by the relative immaturity of the measuring technology that is being applied.  Aside from reproducibility, the patterns themselves may be very complex.  It may take a unique combination of 25 different variables to produce an excellent predictive pattern.  The trouble is that it is impossible to test all such potential combinations.  If we assume 20,000 human genes, there are on the order of 10<sup>82</sup> possible 25 gene combinations!  (People estimate that the <a href="http://en.wikipedia.org/wiki/Observable_universe">total number of atoms in the universe</a> is close to 10<sup>80</sup>.)</p>
              <h2>Opportunity</h2>
              <p>One advantage when it comes to addressing these problems is our increasing understanding of biology.  Our knowledge of genes, biological mechanisms and disease is growing exponentially and much of this knowledge is available on the Web.  When we start looking for predictive patterns in a dataset such as the breast cancer example, we can use this knowledge to guide our search.  TheCure was created as a fun way to solicit help in guiding the search for stable patterns that can be used to make biologically and medically important predictions.</p>
              <p>When people play TheCure they use their knowledge (or their ability to search the Web or their social networks) to make informed decisions about the best combinations of variables (e.g. genes) to use to build predictive patterns.  These combos are the 'hands' in TheCure card game.  Every time a game is played, the hands are evaluated and stored.  Eventually predictors will be developed using advanced machine learning algorithms that are informed by the hands played in the game.</p>
              <strong><center><a href="login.jsp">Play TheCure</a> and share in discovery!</center></strong>
              <h3>Barney<a href="http://commons.wikimedia.org/wiki/File:YHTBTR-character-noback.png"><img src="images/barney.png"/></a></h3>
              <p>The opponent in combo came from a <a href="http://commons.wikimedia.org/wiki/File:YHTBTR-character-noback.png">Wikipedia Commons image</a> from the game "<a href="http://en.wikipedia.org/wiki/You_Have_To_Burn_The_Rope">You have to Burn the Rope</a>".  Thanks for sharing!</p>
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
