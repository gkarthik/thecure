var CURE = CURE || {};

CURE.username = "x0xMaximus";
CURE.phenotype = "dream_breast_cancer";

CURE.load = function() {
  var page = window.location.href.split("/cure/")[1];
  if ( page.indexOf('?') > 0 ) {
    page = page.substring(0, page.indexOf('?'));
  }
  CURE.page = page.replace(".jsp","");
  
  console.log(CURE.page);

  switch(CURE.page)
  {
    case "about":

      break;
    case "contact":

      break;
    case "forgot":
      CURE.forgot.init();
      break;
    case "help":
      CURE.utilities.collapseInfo();
      break;
    case "login":
      CURE.login.init();
      break;
    case "boardroom":
      CURE.boardroom.init();
      break;
    case "stats":
      CURE.stats.init();
      break;
    default:
      //aka the landing page
      CURE.landing.init();
  }
}

CURE.forgot = {
  init : function() {
    $("input#refEmail").blur(function() {
      var email = $("input#refEmail").val();
      CURE.utilities.validateEmail(email)
    });

    $(".emailsub").click(function() {
      var email = $("input#refEmail");
      if( CURE.utilities.validateEmail( email.val() ) ) {
        if( CURE.forgot.submitEmail( email.val() ) ) {
          email.val("");
        };
      }
    });
  },
  submitEmail : function(email) {

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
}

CURE.stats = {
  init : function() {
    var url = "/cure/SocialServer?command=boardroom";
    $.getJSON(url, function(data) {
      drawGraph(data, "chart1");
    });
  }
}

CURE.boardgame = {

}

CURE.login = {
  init : function() {
    var bad = CURE.utilities.getParameterByName("bad");

    //start with new user area hidden
    if( bad == "nametaken" ) {
      alert("Sorry, that user name has been taken. Please try another one.");
      $("#newuser").show();
      $("#olduser").hide();
    } else if( bad=="pw" ) {
      alert("Sorry, that username/password combination is invalid.  Please try again.  ");
      $("#olduser").show();
    } else if( bad=='n' ) {
      $("#newuser").show();
      $("#olduser").hide();
    } else {
      $("#olduser").show();
      //show new user on click
      $("#newuserlink").click(function(e) {
        e.preventDefault();
        $("#newuser").show();
        $("#olduser").hide();
      });
    }
  },
  clearInput : function() {
    $('input[type=text]').focus(function() {
      $(this).val('');
    });
  }
}

CURE.boardroom = {
  init: function() {
    var url = "/cure/SocialServer?command=boardroom&username="+CURE.username+"&phenotype="+CURE.phenotype;
    $.getJSON(url, function(data) {
      CURE.boardroom.drawGrid("#boards", data, 45);
    });
  },

  drawGrid: function(targetEl, data, box_size) {
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

      var scaleAttempt = attempt(v.attempts);
          if (scaleAttempt > 100) { scaleAttempt = 100; }
      var isEnabled,
          content,
          font_size = text_size;
     ( v.enabled == true ) ? isEnabled = "enabled" : isEnabled = "disabled";

     if ( v.enabled == false ) {
        content = "•";
        font_size = font_size*3;
        top_pos = (hw*.2);
      }
      if ( v.trophy == true) {
        content = "★";
        font_size = font_size*0.4;
        top_pos = Math.floor(hw*.2);
      }
      if ( v.enabled == false && v.trophy == true) {
        content = "★";
        top_pos = Math.floor(hw*.2);
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

}

CURE.landing = {
  init : function() {
    CURE.utilities.collapseInfo();

    var url = "/cure/SocialServer?command=gamelogs";
      $.getJSON(url, function(data) {
        //drawGraph(data, "#chart");
        var listItems = CURE.utilities.listOfLeaderBoard(data, 6);
        var olEl = $("div#leaderboard ol").append(listItems);
     });

    $("input.playnow").click(function(e) {
      //link to the game, need to do anything else?
      var gameUrl = "login.jsp";
      window.location.replace(gameUrl);
      // window.open(gameUrl);
      return false;
    })

      // $("input#refEmail").blur(function() {
      //     var email = $("input#refEmail").val();
      //     validateEmail(email)
      // })
      // $(".emailsub").click(function() {
      //   var email = $("input#refEmail");
      //   var name = $("input#refName");
      //   if( CURE.utilities.validateEmail( email.val() ) ) {
      //     if( submitEmail( email.val(), name.val() ) ) {
      //       email.val("");
      //       name.val("");
      //     };
      //   }
      // })

  },
  submitEmail : function(email, name) {
    // /cure/SocialServer?command=invite&by=jessesmith&invited=sam@smith.org
    $.get("/cure/SocialServer", { command: "invite", by: name, invited: email } )
    .success(function(d) {
      $("#emailAlert").html("Thank You!").fadeIn();
        return true;
      })
    .error(function(d) {
      $("#emailAlert").html("Sorry, error occured :(").fadeIn();
        return false;
    });
    return false;
  }
}

CURE.utilities = {
  listOfLeaderBoard : function(data, count) {
   var res = "",
       leaders = _.sortBy(data.leaderboard, function(obj) { return -obj.score; });
    _( _.first(leaders, count) ).each(function(v, i) {
      res += '<li><span class="username">'+v.username+'</span> <span class="score">'+v.score+'</span></li>';
    })
    return res;
  },
  collapseInfo : function() {
    $.each( $("h3") , function(i,v) {
      $(v).toggle(function() {
        var selEl = $(this).attr('class').split(' ')[0];
        $("#"+selEl).slideDown();
      }, function() {
        var selEl = $(this).attr('class').split(' ')[0];
        $("#"+selEl).slideUp();
      })
    });
  },
  drawLineGraph : function(data, targetEl){
    var format = d3.time.format.utc('%Y-%m-%e');
    var height = 200;
    var width = 200;
    var timeScale = d3.time.scale.utc().range([0, width]);
    var firstDomain = format.parse('2012-8-1');
    var secondDomain = format.parse('2012-8-30');
    timeScale.domain([firstDomain, secondDomain])

    var y = d3.scale.linear()
      .domain([0, 1])
      .range([height,0]);

    // axes
    var xAxis = d3.svg.axis().scale(timeScale);
    var yAxis = d3.svg.axis().scale(y).orient('left');

    var graphSvg = d3.select(targetEl).append("svg:svg");

    graphSvg.append('g')
      .attr('class', 'x_axis')
      .attr('transform', 'translate(25,'+(height+5)+')')
      .call(xAxis.ticks(7).tickFormat(d3.time.format('%d')));

    graphSvg.append('g')
      .attr('class', 'y_axis')
      .attr('transform', 'translate(25,5)')
      .call(yAxis);

    graphSvg.append('g')
      .attr('class', 'guide_lines_x')
      .attr('transform', 'translate(25,'+ (height+5) +')')
      .call(xAxis.tickFormat('').tickSize(-height,0,0));

    graphSvg.append('g')
      .attr('class', 'guide_lines_y')
      .attr('transform', 'translate(25,5)')
      .call(yAxis.tickFormat('').tickSize(-width,0,0));

    var line = d3.svg.line()
      .x(function(d) { return timeScale( d.timestamp ) })
      .y(function(d) { return y( d.y ) });

    graphSvg.append('g')
      .attr('transform', 'translate('+(-width)+', 5)')
      .append('path')
      .attr('class', 'line')
      .attr('d', line( data.chart ));

    graphSvg.append('g')
      .attr('class', 'points')
      .attr('transform', 'translate('+(-width)+', 5)').selectAll('circle')
      .data( data.chart ).enter()
      .append('circle')
        .attr('r', 1.5)
        .attr('cx', function(d){ return timeScale( d.timestamp ); })
        .attr('cy', function(d){ return y( d.y ); })
  },
  validateEmail : function(email) {
    var re = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,6}$/;
    var emailEl = $("input#refEmail"),
        isValid = re.test(email);
    if (isValid) {
      emailEl.css({"boxShadow":"0px 0px 4px 0px #20282B"});
    } else {
      emailEl.css({"boxShadow":"0px 0px 8px 0px red"});
    }
    return isValid;
  },
  getParameterByName : function(name) {
    name = name.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]");
    var regexS = "[\\?&]" + name + "=([^&#]*)";
    var regex = new RegExp(regexS);
    var results = regex.exec(window.location.search);
    if(results == null)
      return "";
    else
      return decodeURIComponent(results[1].replace(/\+/g, " "));
  }
}

$(document).ready(function() {
  CURE.load();
});
