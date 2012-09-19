var CURE = CURE || {};

CURE.username = "x0xMaximus";
CURE.user_id = "51";
CURE.dataset = "dream_breast_cancer";

CURE.load = function() {
  var page = window.location.href.split("/cure/")[1];
  if ( page.indexOf('?') > 0 ) {
    page = page.substring(0, page.indexOf('?'));
  }
  CURE.page = page.replace(".jsp","");

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
    case "boardgame":
      CURE.boardgame.init();
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
  
    var data = CURE.utilities.seedData();

//
//     var url = "/cure/SocialServer?command=stats";
//     $.getJSON(url, function(data) {
//       CURE.utilities.drawLineGraph(data, "chart1");
//     });
    CURE.utilities.drawLineGraph(data.chart1, "#chart1", 600);

  }
}



CURE.boardgame = {
  cards : [],
  opponent_sort : [],
  p1_score : 0,
  p2_score : 0,

  p1_hand : [],
  p2_hand : [],

  features : "",
  feature_names : "",
  barney_init : 0,
  board_state_clickable : true,
  replay : "<%=full_request%>",
  multiplier : "<%=multiplier%>",

  game_started : (new Date),

  init : function() {
    var game = CURE.boardgame,
        utils = CURE.utilities;
    game.nrows = utils.getParameterByName("nrows");
    game.ncols = utils.getParameterByName("ncols");
    game.level = utils.getParameterByName("level");
    game.showgeneinfo = utils.getParameterByName("geneinfo") || "1";
    game.max_hand = utils.getParameterByName("max_hand");

    //set up the board
    var args = { x : game.ncols, y : game.nrows }
    if ( game.level != null && CURE.dataset == "mammal" ) {
      args.dataset = "mammal";
      args.command = "getspecificboard";
      args.board = "mammal_"+level;
    } else {
      args.dataset = CURE.dataset;
      args.command = "getboard";
      args.ran = game.level;
    }
    //data will contain the array of cards used to build the board for this game
    $.getJSON("MetaServer", args, function(data) {
      game.cards = data;
      game.generateBoard();
      game.addCardSelectionHandlers();
      //set up handlers
      //-- Mouse hover events for the info box
      game.setupInfoToggle();

      //save hand

      //== MAX NOTES;
      //game_meta_info set this up


    });

 },
  generateBoard : function() {
    //-- Using the cards array, this draws the cards to the board div
    var game = CURE.boardgame,
        targetEl = $("#board");
    _(game.cards).each(function(v, i) {
      v.display_name = v.name || v.att_name || v.unique_id;
      var card = targetEl.append("\
        <div id='card_"+ v.unique_id +"' class='gamecard active'>\
        <span class='help_label'>i</span>\
        <span class='gene_label'>"+ v.display_name +"</span>\
        </div>");
    })
  },
  addCardSelectionHandlers : function() {
    //-- Adds click event listeners on the cards in the board,
    //when clicked, add to hand/save/etc...
    var game = CURE.boardgame;

    $("#board div.gamecard").click(function(e) {
      var clicked_card = $(this);
      if( game.board_state_clickable ) {
        var hand_size = 0;
        if( game.p1_hand ) { hand_size = game.p1_hand.length; }

        if(hand_size < game.max_hand) {
          var unique_id  = clicked_card.attr('id').split('card_')[1];
          var card_obj = _.find(game.cards, function(obj){ return obj.unique_id == unique_id; });
          var playedCard = game.returnCard(card_obj);

          $("#player1_hand").append(playedCard);
          game.p1_hand.push( card_obj );
          game.saveSelection( card_obj );
          game.evaluateHand(game.p1_hand, 1);

          //hide button from board
          clicked_card.removeClass("active").addClass("selected").unbind("click");
          game.board_state_clickable = false;
          game.addCardToBarney();
        } else {  alert("Sorry, you can only have 5 cards in your hand in this game."); }
      } else { alert("Wait your turn!"); }
    })
  },
  returnCard : function(obj) {
    var game = CURE.boardgame;
    var card = "\
                <div id='playedcard_"+ obj.unique_id +"' class='gamecard'>\
                <span class='help_label'>i</span>\
                <span class='gene_label'>"+ obj.display_name +"</span>\
                </div>";

    return card;
  },
  saveSelection : function(card) {
    var game = CURE.boardgame;
    var geneid = card.unique_id;
    var args = {
      dataset : CURE.dataset,
      command : "playedcard",
      board_id : game.level,
      player_name : CURE.username,
      player_id : CURE.user_id,
      geneid : card.unique_id
    }
    $.getJSON("MetaServer", args, function(data) { });
  },
  evaluateHand : function(cardsInHand, player) {
    var game = CURE.boardgame,
        utils = CURE.utilities;
    var args = {
      dataset : CURE.dataset,
      command : "getscore"
    }
    
    if( CURE.dataset == 'dream_breast_cancer') {
      var geneids = [];
      _(cardsInHand).each( function(v) { geneids.push( v.unique_id ); });
      args.geneids = geneids.join(",")
    } else {
      var features = [];
      _(cardsInHand).each( function(v) { features.push( v.att_index ); });
      args.features = features.join(",")
    }

    //goes to server, runs the default evaluation with a decision tree
    $.getJSON("MetaServer", args, function(data) {
      console.log(data);
      var treeheight = 250,
          treewidth = 420;
      if (data.max_depth > 2) { treeheight = 200 + 30*data.max_depth; }

      //draw the current tree
      $("#p"+ player +"_current_tree").empty();
      utils.drawTree(data, treewidth, treeheight, "#p"+ player +"_current_tree");
      //sometimes randomness in cross-validation gets you a different score even without producing
      //any tree at all.
      if (data.max_depth < 2) {
        game["p"+ player +"_score"] = 50;
      } else {
        game["p"+ player +"_score"] = data.evaluation.accuracy;
      }
      $("strong#game_score_"+player).html( game["p"+ player +"_score"] );

      if (player == "2") {
        game.board_state_clickable = true;
        if (  game.p2_hand.length != game.max_hand &&
              game.p1_score < game.p2_score &&
              game.p1_score > 0 ) {
          game.moveBarney("correct"); //incorrect win lose
        } else if ( game.p1_score > game.p2_score ) {
          game.moveClayton("correct"); //incorrect win lose
        }
      }

      //-- If it's the last hand-- save out & display the results
      if ( game.p2_hand.length == game.max_hand) {
        //game.saveHand();
        window.setTimeout( game.showTheResults(), 1500 );
      }
    });
  },
  showTheResults : function() {
    var game = CURE.boardgame,
        winnerEl = $("#endgame #winner");
    if (  game.p1_score < game.p2_score &&
          game.p1_score > 0 ) {
      winnerEl.text("Sorry, you lost this hand. ");
      //$tabs.tabs('select', 4);
      game.moveBarney("win"); //incorrect win lose
      winnerEl.append("<br><a href=\""+replay+"\">Play Level Again?</a>");

    } else if ( game.p1_score > game.p2_score &&
                CURE.dataset == 'mammal' &&
                game.level == 3 ) {

      winnerEl.html("<h1>Congratulations! You finished your training!</h1> <p>You have gained access to the challenge area.</p><h2><a href=\"boardroom.jsp\">Start the challenge!</a></h2>");
      $("#holdem_button").hide();
      //$tabs.tabs('select', 3);
      game.moveBarney("lose"); //incorrect win lose
      game.moveClayton("win");

    } else if ( game.p1_score > game.p2_score ) {

      winnerEl.html("<h1>You beat Barney!</h1>You earned "
        + game.p1_score + " * " + game.multiplier
        + "= </strong><span style=\"font-size:30px;\">"
        + ( game.p1_score * game.multiplier )
        + "</span> points!");
      //$tabs.tabs('select', 3);
      game.moveBarney("lose"); //incorrect win lose
      game.moveClayton("win");

    } else if (p1_score == p2_score) {
      winnerEl.text("You tied Barney! ");
      //$tabs.tabs('select', 3);
      game.moveBarney("win"); //incorrect win lose
      game.moveClayton("win");
      targetEl.append("<br><a href=\""+replay+"\">Play Level Again?</a>");
    }
    $("#board").hide();
    $("#endgame").show();
  },
  addCardToBarney : function() {
    var game = CURE.boardgame;
    var card_obj = game.getBarneysNextCard();

    game.p2_hand.push(card_obj);
    var playedCard = game.returnCard(card_obj);
    $("#player2_hand").append( playedCard );

    var selected_card = $("#boardgame #game_area #board div#card_"+card_obj.unique_id);
    selected_card.removeClass("active").addClass("selected").unbind("click");

    game.evaluateHand( game.p2_hand, "2");
  },
  getBarneysNextCard : function(){
    var game = CURE.boardgame;
    var index = Math.floor( Math.random() * game.cards.length )
    //var card_obj = _.find(game.cards, function(obj){ return obj.board_index == index; });
    var card_obj = game.cards[index];
    //-- Check to see if the randomly selected card is already in 
    //a player's hand
    var used_cards = _.union( _(game.p1_hand).pluck('unique_id'), _(game.p2_hand).pluck('unique_id') )
    
    if( _(used_cards).include( card_obj.unique_id ) ) {
      return game.getBarneysNextCard();
    } else {
      return card_obj;
    }
  },
  setupInfoToggle : function() {
    //-- Handles switching between tab views
   $("#tabs ul li").click(function(e) {
     $.each( $("div#help_area div#infoboxes div.infobox"), function(i, v) { $(v).hide() })
      var selEl = $(this).attr('class').split(' ')[0];
      $("#"+selEl).show();
    })
  },
  activeInfoPanel : function() {
    //-- Returns the ID of which info panel tab is currently being viewed
    var active_panel = "";
    $.each( $("div#help_area div#infoboxes div.infobox"), function(i, v) {
      if( $(v).is(':visible') ) { active_panel = $(v).attr('id'); }
    });
    return active_panel;
  },
  setupInfoHandler : function() {
    var game = CURE.boardgame;
    //-- Handles updating the tabs with the gene of interest
    $("#board div.gamecard span.help_label").click(function(e) {
      var clicked_card = $(this);
      var unique_id  = clicked_card.attr('id').split('card_')[1];
      game.showgene(unique_id);
    });
  },
  setupGameMetaInfo : function() {
    //   <% if(dataset.equals("mammal")){
    //   String fs = "features that distinguish";
    //   if(max_hand.equals("1")){
    //     fs = "feature that distinguishes";
    //   }
    // %>
    // <strong>Pick <b><%=max_hand%></b> <%=fs %> mammals from other creatures.  Think of things that separate mammals from fish, insects, amphibians, reptiles...</strong>
    // <% }else if(dataset.equals("dream_breast_cancer")){ %>
    // <strong>Pick <b><%=max_hand%></b> genes that track breast cancer survival.  Look for genes that you think will have prognostic RNA expression or copy number variation.</strong>
    // <% } %>
  },
  moveBarney : function(moveChoice) {
    $("#barney5").removeClass().hide().addClass(moveChoice).show();
  },
  moveClayton : function(moveChoice) {
    $("#clayton1").removeClass().hide().addClass(moveChoice).show();
  },

  saveHand : function() {
    par_score = p1_score - par;
    var win = "0";
    if(p1_score > p2_score) {
      win = "1";
    }

    var saveurl;
    if( CURE.dataset == 'dream_breast_cancer' ) {
      geneids = "";
      feature_names = "";
    console.log(game.cads);
      $.each(p1_hand, function(index, value) {
        geneids += value.unique_id + ",";
        feature_names +=  value.unique_id+":"+value.att_name + ":" + value.name + "|";
      });
      saveurl = 'MetaServer?dataset='+ CURE.dataset +'&command=savehand&geneids='
        + geneids + '&player_name=' + CURE.username + '&score='
        + par_score + '&cv_accuracy=' + p1_score + '&board_id=' + level
        + "&win=" + win+'&game=verse_barney&feature_names=' + feature_names;
    } else {
      features = "";
      feature_names = "";
      $.each(p1_hand, function(index, value) {
        features+=value.att_index+",";
        feature_names += value.unique_id+":"+value.att_name + ":"+value.name+"|";
      });
      saveurl = 'MetaServer?dataset='+ CURE.dataset +'&command=savehand&features='
        + features + '&player_name=' + CURE.username + '&score='
        + par_score + '&cv_accuracy=' + p1_score + '&board_id=' + level
        + "&win=" + win+'&game=verse_barney&feature_names=' + feature_names;
    }
    $.getJSON(saveurl, function(data) {
      //console.log("saved a hand");
    });
  },
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
    var args = {
      command : "boardroom",
      username : CURE.username,
      //this will become datatype when serverside is fixed
      phenotype : CURE.dataset
    }
    $.getJSON("/cure/SocialServer", args, function(data) {
      CURE.boardroom.drawGrid("#boards", data, 45);
    });
  },

  drawGrid: function(targetEl, data, box_size) {
    //double check the sort/ordering by difficulty
    //data.boards = _.sortBy(data.boards, function(obj){ return -obj.base_score; })

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
    // $('#twitter-widget-1').ready(function() {
    //   CURE.landing.getTwitterTimeline();
    // });
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
  getTwitterTimeline : function() {
    var cssLink = document.createElement("link");
    cssLink.href = "twitovr.css";  cssLink .rel = "stylesheet";
    cssLink .type = "text/css";
    $("twitter-widget-1").append(cssLink); 
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
  seedData : function() {
    var res = {};

    for(var i = 1; i < 6; i++) {
      var chart = {};
      chart.metadata = {
        "x_axis_label" : "foo",
        "y_axis_label" : "bar"
      }
      chart.data = d3.range(20).map(function(i) {
        return {timestamp: ( new Date(2012, 8, (6+i), 1, 1, 23, 23).getTime() ), y: (Math.sin(i / 3) + 1) / 2};
      })
      res["chart"+i] = chart;
    }
    
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
  drawLineGraph : function(data, targetEl, size){
    var format = d3.time.format.utc('%Y-%m-%d');
    var hw = size || 200;
    var small_padding = hw*0.025;
    var large_padding = hw*0.125;
    var timeScale = d3.time.scale.utc().range([0, hw]);
    var firstDomain = format.parse('2012-09-01');
    var secondDomain = format.parse('2012-10-15');
    timeScale.domain([firstDomain, secondDomain])

    var y = d3.scale.linear()
      .domain([0, 1])
      .range([hw,0]);

    // axes
    var xAxis = d3.svg.axis().scale(timeScale);
    var yAxis = d3.svg.axis().scale(y).orient('left');

    var graphSvg = d3.select(targetEl).append("svg:svg");

    graphSvg.append('g')
      .attr('class', 'x_axis')
      .attr('transform', 'translate('+ large_padding +','+(hw + small_padding)+')')
      .call(xAxis.tickFormat(d3.time.format('%b %d')));

    graphSvg.append('g')
      .attr('class', 'y_axis')
      .attr('transform', 'translate('+ large_padding +','+ small_padding +')')
      .call(yAxis);

    graphSvg.append('g')
      .attr('class', 'guide_lines_x')
      .attr('transform', 'translate('+ large_padding +','+ (hw+small_padding) +')')
      .call(xAxis.tickFormat('').tickSize(-hw,0,0));

    graphSvg.append('g')
      .attr('class', 'guide_lines_y')
      .attr('transform', 'translate('+ large_padding +', '+ small_padding +')')
      .call(yAxis.tickFormat('').tickSize(-hw,0,0));

    var line = d3.svg.line()
      .x(function(d) { return timeScale( d.timestamp ) })
      .y(function(d) { return y( d.y ) })
      .interpolate("basis");

    graphSvg.append('g')
      .attr('class', 'line')
      .attr('transform', 'translate('+ large_padding +', '+ small_padding +')')
      .append('path')
      .attr('class', 'line')
      .attr('d', line( data.data ));

    graphSvg.append('g')
      .attr('class', 'points')
      .attr('transform', 'translate('+ large_padding +', '+ small_padding +')').selectAll('circle')
      .data( data.data ).enter()
      .append('circle')
        .attr('r', 1.5)
        .attr('cx', function(d){ return timeScale( d.timestamp ); })
        .attr('cy', function(d){ return y( d.y ); })

    // Axis labels
    graphSvg.append("text")
      .attr("class", "x label")
      .attr("text-anchor", "center")
      .attr("x", (hw/2)+large_padding )
      .attr("y", hw + (large_padding - small_padding) )
      .text( data.metadata.x_axis_label );

    graphSvg.append("text")
      .attr("class", "y label")
      .attr("text-anchor", "center")
      .attr("dy", hw )
      .attr("x", "100")
      .attr("transform", "rotate(-90)")
      .text( data.metadata.y_axis_label );
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
  },
  getScore : function(cards) {
    //set the par - use all the features on the board
    var f = [];
    $.each( cards, function(i, v) { f.push(v.att_index); });
    var args = {
      dataset : CURE.dataset,
      command : "getscore",
      features : f.join(",")
    }
    $.getJSON("MetaServer", args, function(data) {
      return data.evaluation.accuracy;
    });
  },
///////////////////////////////
  kind : function(kind_text) {
    switch(kind_text)
    {
      case "split_node":
        return 0;
      case "split_value":
        return 1;
      case "leaf_node":
        return 2;
      default:
        return 3;
    }
    return 3;
  },
  drawTree : function(json, width, height, selector_string) {
    var utils = CURE.utilities;
    if(json.max_depth<2){
      $(selector_string).append("Could not build a useful tree with the selected features...");
      return;
    }
    var green = "#1FA13A",
        orange = "#D44413",
        depth = json.max_depth-1;
    var cluster = d3.layout.tree()
      .size([width-40, height-40]),
      diagonal = d3.svg.diagonal();

    var vis = d3.select(selector_string).append("svg")
      .attr("width", width)
      .attr("height", height)
      .append("g")
      .attr("transform", "translate(0,8)");

    //Deeeebugging
    var nodes = cluster.nodes(json.tree),
        links = cluster.links(nodes),
        color = d3.scale.linear().domain([0, depth]).range([orange, green]),
        //Breaking out node types, uglyyyy
        split_nodes = [], split_values = [], leaf_nodes = [];
    _.each(nodes, function(node) {
      if( utils.kind(node.kind) == 0) {
        split_nodes.push(node);
      } else if ( utils.kind(node.kind) == 1 ) {
        split_values.push(node);
      } else if ( utils.kind(node.kind) == 2 || utils.kind(node.kind) == 3 ) {
        leaf_nodes.push(node);
      }
    });

    //Draw the links first so they're behind the nodes
    var link = vis.selectAll("path.link")
    .data(links)
    .enter().append("path")
    .transition().delay(400).duration(200)
    .attr("class", "link")
    .attr("d", diagonal)
    .style("stroke", function(d) { return color(d.source.depth) } )
    .style("stroke-width", function(d) { return 1.3*(depth - d.source.depth+1) +"px" } );

    //Drawing the groups to dom
    var split_node = vis.selectAll("g.split_node")
      .data(split_nodes)
      .enter().append("g")
      .attr("class", "split_node")
      .attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; })
    var split_value = vis.selectAll("g.split_value")
      .data(split_values)
      .enter().append("g")
      .attr("class", "split_value")
      .attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; })
    var leaf_node = vis.selectAll("g.leaf_node")
      .data(leaf_nodes)
      .enter().append("g")
      .attr("class", "leaf_node")
      .attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; })

    //Adding to the groups for that node type
    split_node.append("rect")
      .transition().delay(100).duration(400)
      .attr("height", "18")
      .attr("width", function(d) { return $.trim(d.name).length*9  })
      .attr("x", function(d) { return -( $.trim(d.name).length*4.5 ) } )
      .attr("y", "-2")
    split_node.append("text")
      //move it down slightly
      .transition().delay(100).duration(400)
      .attr("dy", 12)
      .text(function(d) { return d.name.toUpperCase() });

    split_value.append("circle")
      .transition().delay(100).duration(400)
      .style("fill", function(d) { return color( d.depth ) })
      .attr("r", "8")
    split_value.append("text")
      .transition().delay(100).duration(400)
      //move it down slightly
      .attr("dy", 4)
      .attr("dx", 22)
      .text(function(d) { return d.name.toUpperCase() });

    //Left node text
    leaf_node.append("text")
      .transition().delay(100).duration(400)
      //move it down slightly
      .attr("dy", 12)
      .text(function(d) { return d.name.toUpperCase() });

    //Adding the bar graphs
    //  _.each(leaf_nodes, function(d) {
    //    $(selector_string).append("<div id='sprkln"+d.bin_size+""+d.errors+"' style='display:none;position:absolute;top:"+(d.y+30)+"px;left:"+(d.x +  $.trim(d.name).length*1.5   )+"px'></div>");
    //    $("#sprkln"+d.bin_size+""+d.errors).sparkline([d.bin_size,d.errors], {
    //      type: 'pie',
    //      width: '20',
    //      height: '20',
    //      sliceColors: ['#1FA13A','#D44413']
    //      }).fadeIn(1200);
    //    });
  },
  drawTreeNoLeaf : function(json, width, height, selector_string) {
    var utils = CURE.utilities;
    var green = "#1FA13A",
    orange = "#D44413",
    depth = json.max_depth-1;

    var cluster = d3.layout.tree()
      .size([width-40, height-40]),
      diagonal = d3.svg.diagonal();

    var vis = d3.select(selector_string).append("svg")
      .attr("width", width)
      .attr("height", height)
      .append("g")
      .attr("transform", "translate(20,20)");

    var nodes = cluster.nodes(json.tree),
        links = cluster.links(nodes),
        color = d3.scale.linear().domain([0, depth]).range([orange, green]),
        //Breaking out node types, uglyyyy
        split_nodes = [], split_values = [], leaf_nodes = [];
    _.each(nodes, function(node) {
      if( utils.kind(node.kind) == 0) {
        split_nodes.push(node);
      } else if ( utils.kind(node.kind) == 1 ) {
        split_values.push(node);
      } else if ( utils.kind(node.kind) == 2 || utils.kind(node.kind) == 3 ) {
        leaf_nodes.push(node);
      }
    });

    //Draw the links first so they're behind the nodes
    var link = vis.selectAll("path.link")
      .data(links)
      .enter().append("path")
      .transition().delay(400).duration(200)
      .attr("class", "link")
      .attr("d", diagonal)
      .style("stroke", function(d) { return color(d.source.depth) } )
      .style("stroke-width", function(d) { return 1.3*(depth - d.source.depth+1) +"px" } );

    var split_node = vis.selectAll("g.split_node")
      .data(split_nodes)
      .enter().append("g")
      .attr("class", "split_node")
      .attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; })
    var split_value = vis.selectAll("g.split_value")
      .data(split_values)
      .enter().append("g")
      .attr("class", "split_value")
      .attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; })
    var leaf_node = vis.selectAll("g.leaf_node")
      .data(leaf_nodes)
      .enter().append("g")
      .attr("class", "leaf_node")
      .attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; })

    split_node.append("rect")
      .transition()
      .delay(10*depth)
      .duration(100*depth)
      .attr("height", "18")
      .attr("width", function(d) { return $.trim(d.name).length*9  })
      .attr("x", function(d) { return -( $.trim(d.name).length*4.5 ) } )
      .attr("y", "-2")
    split_node.append("text")
      //move it down slightly
      .attr("dy", 12)
      .style("font-family", "Helvetica")
      .style("font-size", "12px")
      .style("font-weight", "bold")
      .style("fill", "#000")
      .style("text-anchor", "middle")
      .text(function(d) { return d.name.toUpperCase() });

    split_value.append("circle")
      .style("fill", function(d) { return color( d.depth ) })
      .transition()
      .delay(10*depth)
      .duration(100*depth)
      .attr("r", "8")
    split_value.append("text")
      //move it down slightly
      .attr("dy", 4)
      .attr("dx", 32)
      .style("font-family", "Helvetica")
      .style("font-size", "10px")
      .style("font-weight", "bold")
      .style("fill", "#000")
      .style("text-anchor", "middle")
      .text(function(d) { return d.name.toUpperCase() });

    leaf_node.append("text")
      //move it down slightly
      .attr("dy", 12)
      .style("font-family", "Helvetica")
      .style("font-size", "12px")
      .style("font-weight", "bold")
      .style("fill", "#000")
      .style("text-anchor", "middle")
      .text(function(d) { return d.name.toUpperCase() });
  }
}

$(document).ready(function() {
  CURE.load();
});
