<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>

<!DOCTYPE html>
<html>
  <head>
    <meta http-equiv="Content-type" content="text/html; charset=utf-8">
    <title>Forgot password</title>
    <link rel="stylesheet" href="assets/css/combo_bootstrap.css" type="text/css" media="screen">
    <link rel="stylesheet" href="assets/css/style.css">
  </head>
  
  <body>
    <div class="navbar navbar-fixed-top">
      <div class="navbar-inner">
        <div class="container">
          <a class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">
            <span class="icon-bar"></span> </a> 
          <a class="brand" href="/cure/">Branch</a>				
          <div class="nav-collapse">
            <ul class="nav">
              <li><a href="contact.jsp">Contact</a></li>
            </ul>
          </div>
        </div>
      </div>
    </div>
    
    <div class="container">
      <div class="hero-unit">
        <div class="row">
          <div class="span6 offset2">
            <div id="login">
              <div id="email" class="forgot">
                
                <label for="mail">Enter your email address:</label> <br />
                <input id="refEmail" class="email" type="email" name="mail" placeholder="email" /><br />
                
                <p id="emailAlert" style="display: none;" ></p><br />
                <input class="emailsub" type="submit" value="Submit" id="loginSubmit" />
                <a href="login.jsp"><span class="pink">Back to login</span></a>
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
