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
          <a class="brand" href="/cure/">The Cure</a>				
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

    <script src="js/libs/jquery-1.8.0.min.js"></script>
    <script src="js/libs/underscore-min.js"></script>
    <script>
	  $(document).ready(function() {	
	function validateEmail(email) {
        var re = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,6}$/;
        var emailEl = $("input#refEmail"),
            isValid = re.test(email);
        if (isValid) {
          emailEl.css({"boxShadow":"0px 0px 4px 0px #20282B"});
        } else {
          emailEl.css({"boxShadow":"0px 0px 8px 0px red"});
          $("#emailAlert").html("Please enter a valid email address").fadeIn();
        }
        return isValid;
      }

      function submitEmail(email) {

      $.get("/cure/SocialServer", { command: "iforgot", mail: email } )
        .success(function(d) {
          $("#emailAlert").html("Thank you, your password request has been sent to the provided email address.").fadeIn();
          return true;
        })
        .error(function(d) {
          $("#emailAlert").html("Sorry, error occured :(").fadeIn();
          return false;
        });
        return false;
      }

      $("input#refEmail").blur(function() {
        var email = $("input#refEmail").val();
        validateEmail(email)
      });

      $(".emailsub").click(function() {
        var email = $("input#refEmail");
        if( validateEmail( email.val() ) ) {
          if( submitEmail( email.val() ) ) {
            email.val("");
          };
        }
      });
	  });	
  </script>
<jsp:include page="js/analytics.js" />
</body>
</html>
