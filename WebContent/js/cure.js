var CURE = CURE || {};

CURE.load = function() {
  var utils = CURE.utilities;
  var page = window.location.href.split("/cure/")[1];
  if ( page.indexOf('?') > 0 ) {
    page = page.substring(0, page.indexOf('?'));
  }
  CURE.page = page.replace(".jsp","");
  CURE.isiPad = navigator.userAgent.match(/iPad/i) != null;

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
      CURE.user_id = cure_user_id;
      CURE.user_experience = cure_user_experience;
      CURE.boardroom.init();
      break;
    case "boardgame":
      CURE.user_id = cure_user_id;
      CURE.user_experience = cure_user_experience;
      if(CURE.isiPad) {
        $("div.navbar.navbar-fixed-top").hide();
      } else {
        $("div#boardgame").css({"margin":"60px auto"});
      }
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

    $.get("SocialServer", { command: "iforgot", mail: email } )
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

  heat_map : [
    { "board_id": 0,
      "hits": 877
    },{
      "board_id": 1,
      "hits": 733
    },{
      "board_id": 2,
      "hits": 713
    },{
      "board_id": 3,
      "hits": 702
    },{
      "board_id": 4,
      "hits": 607
    },{
      "board_id": 5,
      "hits": 597
    },{
      "board_id": 6,
      "hits": 649
    },{
      "board_id": 7,
      "hits": 673
    },{
      "board_id": 8,
      "hits":593
    }, {
      "board_id": 9,
      "hits": 708
    },{
      "board_id": 10,
      "hits": 560
    },{
      "board_id": 11,
      "hits": 606
    },{
      "board_id": 12,
      "hits": 697
    },{
      "board_id": 13,
      "hits": 606
    },{
      "board_id": 14,
      "hits": 613
    },{
      "board_id": 15,
      "hits": 520
    },{
      "board_id": 16,
      "hits": 645
    },{
      "board_id": 17,
      "hits": 577
    },{
      "board_id": 18,
      "hits": 613
    },{
      "board_id": 19,
      "hits": 626
    },{
      "board_id": 20,
      "hits": 595
    },{
      "board_id": 21,
      "hits": 515
    },{
      "board_id": 22,
      "hits": 546
    },{
      "board_id": 23,
      "hits": 621
    },{
      "board_id": 24,
      "hits": 655
    }],

  init : function() {



    var data = CURE.utilities.seedData();
//
//     var url = "/cure/SocialServer?command=stats";
//     $.getJSON(url, function(data) {
//       CURE.utilities.drawLineGraph(data, "chart1");
//     });
    //CURE.stats.competition_bar(data.chart1, "#compbar");
    //CURE.stats.heatmap();
    
    var args = {
      command : "gamelogs",
    }
    $.getJSON("SocialServer", args, function(data) {
      var timedata = _.map(data.chart, function(obj) {
        obj["timestamp"] = obj["timestamp"]*1000;
        return obj;
      })
      CURE.utilities.drawLineGraph(timedata, "#games_won", "Games Won");
      CURE.utilities.drawPieChart(data.leaderboard, "#leaderPie");
    });

  },
  competition_bar : function(data, targetEl) {
    var utils = CURE.utilities;

    var format = d3.time.format.utc('%Y-%m-%d');
    var height = 400;
    var width = 800;
    var small_padding = width*0.025;
    var large_padding = width*0.125;

    var timeScale = d3.time.scale.utc().range([0, width]);
    var firstDomain = format.parse('2012-09-01');
    var secondDomain = format.parse('2012-10-15');
    timeScale.domain([firstDomain, secondDomain])

    var y = d3.scale.linear()
      .domain([0, 1])
      .range([height,0]);

    //-- Axes
    var xAxis = d3.svg.axis().scale(timeScale);
    var yAxis = d3.svg.axis().scale(y).orient('left');

    var graphSvg = d3.select(targetEl).append("svg:svg");

    graphSvg.append('g')
      .attr('class', 'x_axis')
      .attr('transform', 'translate('+ large_padding +','+(height + small_padding)+')')
      .call(xAxis.tickFormat(d3.time.format('%b %d')));

   graphSvg.append('g')
      .attr('class', 'y_axis')
      .attr('transform', 'translate('+ large_padding +','+ small_padding +')')
      .call(yAxis);

    //-- 
    graphSvg.append('g')
      .attr('class', 'guide_lines_x')
      .attr('transform', 'translate('+ large_padding +','+ (height + small_padding) +')')
      .call(xAxis.tickFormat('').tickSize(-height,0,0));

    //-- These lines go horizontal
    graphSvg.append('g')
      .attr('class', 'guide_lines_y')
      .attr('transform', 'translate('+ (large_padding) +', '+ small_padding +')')
      .call(yAxis.tickFormat('').tickSize(-width,0,0));

    graphSvg.append('g')
      .attr('class', 'bars')
      .attr('transform', 'translate('+ large_padding +', '+ (small_padding+height) +')').selectAll('rect')
      .data( data ).enter()
      .append('rect')
        .attr('width', "12px")
        .attr('height', function(d) { return y( d.y ); })
        .attr('x', function(d){ return timeScale( d.timestamp ); })
        .attr('y', '0px');

  },
  heatmap : function() {
    var data = CURE.stats.heat_map;

    var grid_size = 5;
    var padding = 80;
    //var attempt = d3.scale.linear()
    //  .domain([0, 10])
    //  .range([0, 100]);

    var hits = _(data).pluck("hits");

    var color = d3.scale.linear()
      .domain([_.min(hits), _.max(hits)])
      .range(["#00FF00", "#FF0000"]);

    var hits = d3.scale.linear()
      .domain([_.min(hits), _.max(hits)])
      .range([1, 2]);

    var vis = d3.select("#heatmap").append("svg");

    var circles = vis.selectAll("circle")
    .data(data);

    circles.enter().append("circle")
      .attr("cy", function(d,i) {

        return padding+Math.floor( i / grid_size )*40;
      })
      .attr("cx", function(d,i) {
        var level = Math.floor( i / grid_size )


        return padding+((i / grid_size) * 400) - (level*400);
      })
      .attr("r", function(d) {
        return 10*hits(d.hits);
      })
      .attr("fill", function(d) { return color(d.hits) });
  
  }
}



CURE.boardgame = {
  metadata : {
    game_started : (new Date).getTime(),
    mouse_action : []
  },
  cards : [],
  p1_hand : [],
  p2_hand : [],

  p1_score : 0,
  p2_score : 0,

  board_state_clickable : true,
  cached_info_panel_unique_id : 0,

  init : function() {
    var game = CURE.boardgame,
        utils = CURE.utilities;
    game.board_id = utils.getParameterByName("b");
    game.max_hand = utils.getParameterByName("h"); //error check for 1 / 2 / 5
    var t = utils.getParameterByName("t");
     //-- Set the title
    document.title = t+" : The Cure"

    //-- Adding other metadata
    game.metadata.board_id = game.board_id; //int
    game.metadata.player1_id = CURE.user_id; //string
    game.metadata.player2_id = "215";

    //set up the board
    var args = {
      command : "getboard",
      board_id : game.board_id,
    }
    //data will contain the array of cards used to build the board for this game
    $.getJSON("MetaServer", args, function(data) {
      CURE.dataset = data.dataset;

      //-- Update thecure cards with the rooms cards
      game.cards = data.cards;
      //-- Inject the ux info object
      _.each(game.cards, function(v) { v.metadata.ux = []; })

      //-- Draw the boards to the display
      game.generateBoard();

      //-- Handle how cards are selected / moved into the respective player hand
      game.addCardSelectionEventHandlers();

      //-- Mouse hover events for the info boxes & Gene more info help areas
      game.setupInfoToggle();
      game.setupHelpDisplay();
      game.termCalculations();

      //-- Show help info if first time CURE player that passed training
      if( CURE.user_experience == 0 ) {
        game.setupGameMetaInfo();
      }
    });

    //-- Record the user mousemouse actions
    var ma = game.metadata.mouse_action;
      $(window).mousemove(
        _.debounce(function(e) {
          ma.push( { "x": e.pageX, "y": e.pageY, "timestamp": (new Date).getTime() } )
        }, 20)
      );
  },
  termCalculations : function() {
    //-- Show the top X Rifs/ontology
    var game = CURE.boardgame;
    //var rifs = _.pluck( _.flatten( _.map( game.cards, function(card_obj) { return card_obj.metadata.rifs } ) ) , "text")

    $("#search").keyup(function(e) {
      $("#board .gamecard").removeClass("highlight");
      var needle = $(this).val();
      //search descriptions
      game.performDescriptionSearch(needle);
      game.performOntologySearch(needle);
      game.performRifSearch(needle);
    })

  },
  performDescriptionSearch : function(term) {
    var game = CURE.boardgame;

    var needle = term.toLowerCase().trim();
    _.each( game.cards , function(v,i) {
      var haystack = v.description;
      if(haystack == null) { return false; }
      if ( (needle == haystack.substring(0, needle.length) || haystack.indexOf(needle) != -1 ) && needle.length > 0) {
        $("#card_"+v.unique_id).addClass("highlight")
      }
    })
  },
  performOntologySearch : function(term) {
    var game = CURE.boardgame;

    var needle = term.toLowerCase().trim();
    _.each( game.cards , function(v,i) {
      var haystack = _.pluck(_.flatten( _.pluck( v.metadata.ontology , "values" ) ), "term").join(" ").toLowerCase().trim()
      if ( (needle == haystack.substring(0, needle.length) || haystack.indexOf(needle) != -1 ) && needle.length > 0) {
        $("#card_"+v.unique_id).addClass("highlight")
      }
    })
  },
  performRifSearch : function(term) {
    var game = CURE.boardgame;

    var needle = term.toLowerCase().trim();
    _.each( game.cards , function(v,i) {
      var haystack = _.flatten( _.pluck( v.metadata.rifs , "text" ) ).join(" ").toLowerCase().trim()
      if ( (needle == haystack.substring(0, needle.length) || haystack.indexOf(needle) != -1 ) && needle.length > 0) {
        $("#card_"+v.unique_id).addClass("highlight")
      }
    })

  },
  generateBoard : function() {
    //-- Using the cards array, this draws the cards to the board div
    var game = CURE.boardgame,
        boardEl = $("#board");
    //-- Clear out the board and unbind all events just incase
    boardEl.html("").unbind();
    _(game.cards).each(function(v) {
      var display_name = v.short_name || v.unique_id;
      boardEl.append("\
        <div id='card_"+ v.unique_id +"' class='gamecard active'>\
          <span class='help_label'>i</span>\
          <span class='gene_label'>"+ display_name +"</span>\
        </div>");
    })
  },
  addCardSelectionEventHandlers : function() {
    //-- Adds click event listeners on the cards in the board, when clicked, add to hand/save/etc...
    var game = CURE.boardgame;

    $("#board div.gamecard").click(function(e) {
      var clicked_card = $(this);
      if( game.board_state_clickable ) {
        //-- Get the current size of the player's hand
        var hand_size = 0;
        if( game.p1_hand ) { hand_size = game.p1_hand.length; }

        if(hand_size < game.max_hand) {
          var unique_id  = clicked_card.attr('id').split('card_')[1];
          var card_obj = _.find(game.cards, function(obj){ return obj.unique_id == unique_id; });
          var playedCardHtml = game.returnCard(card_obj);

          $("#p1_hand").append(playedCardHtml);
          game.setupHelpDisplay();

          game.p1_hand.push( card_obj );
          game.savePlayedCard( card_obj, 1 );
          game.getScore(game.p1_hand, 1);

          //hide button from board
          clicked_card.removeClass("active").addClass("selected").unbind("click").html("");
          game.board_state_clickable = false;
          game.addCardToBarney();
        } else {  
          $("#modal").html("").append("<p>Sorry, you can only have 5 cards in your hand in this game.</p>").leanModal(); }
      } else { $("#modal").html("").append("<p>Wait your turn!</p>"); }
    })
  },
  returnCard : function(obj) {
    var display_name = obj.short_name || obj.unique_id;
    var card = "\
                <div id='playedcard_"+ obj.unique_id +"' class='gamecard active'>\
                <span class='help_label'>i</span>\
                <span class='gene_label'>"+ display_name +"</span>\
                </div>";

    return card;
  },
  savePlayedCard : function(card_obj) {
    var game = CURE.boardgame;

    var args = {
      command : "saveplayedcard",
      board_id : game.board_id,
      player_id : CURE.user_id,
      unique_id : card_obj.unique_id,
      display_loc : (card_obj.board_index+1),
      timestamp : (new Date).getTime()
    }
    $.ajax({
      type: 'POST',
      url: 'MetaServer',
      data: JSON.stringify(args),
      dataType: 'json',
      contentType: "application/json; charset=utf-8"
    });
  },
  getScore : function(cardsInHand, player) {
    var game = CURE.boardgame,
        utils = CURE.utilities;

    var reported_player = 0;
    if(player == 1) {
      reported_player = CURE.user_id;
    } else if (player == 2) {
      reported_player = "215";
    }

    var args = {
      board_id : game.board_id,
      player_id : reported_player,
      command : "getscore",
      unique_ids : []
    }
    _(cardsInHand).each( function(v) { args.unique_ids.push( v.unique_id ); });

    //-- Goes to server, runs the default evaluation with a decision tree
    $.ajax({
      type: 'POST',
      url: 'MetaServer',
      data: JSON.stringify(args),
      dataType: 'json',
      contentType: "application/json; charset=utf-8",
      success: function(data) {
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
        $("span#p"+ player +"_score").html( game["p"+ player +"_score"] );

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
          game.saveHand(player);
          window.setTimeout( game.showTheResults(), 1500 );
        }
      }
    });
  },
  showTheResults : function() {
    var game = CURE.boardgame,
        winnerEl = $("#endgame");
    winnerEl.html("");
    if (  game.p1_score < game.p2_score &&
          game.p1_score > 0 ) {
      winnerEl.append("<h2>Sorry, you lost this hand.</h2>");
      winnerEl.append("<h3><span class='replay_level'>Play Level Again?</span></h3>");
      winnerEl.append("<h3><span class='play_another'>Play Another Level?</span></h3>");

      game.showTab("p2_current_tree");

      game.moveBarney("win"); //incorrect win lose


    } else if ( game.p1_score > game.p2_score &&
                CURE.dataset == 'mammal' &&
                game.board_id == 204 ) {
      winnerEl.append("<h2>Congratulations! You finished your training!</h1>")
      winnerEl.append("<h3>You have gained access to the challenge area.</h3>")
      winnerEl.append("<h3><span class='pink'><a href='boardroom.jsp'>Start the challenge!</a></h3>");
      $("#holdem_button").hide();
      game.moveBarney("lose"); //incorrect win lose
      game.moveClayton("win");

      $("#lean_overlay").click(function() {
        $(this).fadeOut(200);
        $("#modal").fadeOut();
        window.location.href = "boardroom.jsp";
      })

    } else if ( game.p1_score > game.p2_score ) {

      winnerEl.append("<h2>You beat Barney!</h2>")
      winnerEl.append("<h3>You earned "+ (game.p1_score*21) +" points!</h3>");
      winnerEl.append("<h3><span class='play_another'>Play Another Level?</span></h3>");

      game.showTab("p1_current_tree");

      game.moveBarney("lose"); //incorrect win lose
      game.moveClayton("win");

    } else if ( game.p1_score == game.p2_score) {

      winnerEl.append("<h2>You tied Barney!</h2>");
      winnerEl.append("<h3><span class='replay_level'>Play Level Again?</span></h3>");

      game.showTab("p1_current_tree");

      game.moveBarney("win"); //incorrect win lose
      game.moveClayton("win");
    }

    var top_pos = "-24px",
        right_pos = "-40px";
    if( CURE.isiPad ) {
      top_pos = "-44px";
      right_pos = "-20px";
    }

    winnerEl.css({
      'position' : 'absolute',
      'top' : top_pos,
      'right' : right_pos,
      'width' : '196px',
    }).fadeIn(800);

    $("span.replay_level").click(function() {
        game.replayHand();
        winnerEl.fadeOut();
    })

    $("span.play_another").click(function() {
      if ( _.include(["201", "202", "203"], game.board_id) ){
        window.location.href = "training.jsp"
      } else {
        window.location.href = "boardroom.jsp";
      }
    })

    $("span.replay_level").glowText();
    $("span.play_another").glowText();

  },
  addCardToBarney : function() { 
    var game = CURE.boardgame;
    var card_obj = game.getBarneysNextCard();

    game.p2_hand.push(card_obj);
    var playedCard = game.returnCard(card_obj);
    $("#p2_hand").append( playedCard );
    game.setupHelpDisplay();

    var selected_card = $("#boardgame #game_area #board div#card_"+card_obj.unique_id);
    selected_card.removeClass("active").addClass("selected").unbind("click").html("");

    game.getScore( game.p2_hand, 2);
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
    var game = CURE.boardgame;

    if( CURE.dataset == "mammal") {
      $("#tabs ul li.ontology").hide();
      $("#tabs ul li.rifs").hide();
      $("input#search").hide();
    };

    //-- Handles switching between tab views
    $("#tabs ul li").click(function(e) {
      var selEl = $(this).attr('class').split(' ')[0];

      game.showTab(selEl);

      //-- Save the tab change to the card's metadata
      if ( game.cached_info_panel_unique_id != 0 ) {
        var card_obj = _.find(game.cards, function(obj){ return obj.unique_id == game.cached_info_panel_unique_id; });
        card_obj.metadata.ux.push({
          timestamp : (new Date).getTime(),
          panel : game.activeInfoPanel(),
          board_hover : false
        });
      };
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
  closeAllInfoTabs : function() { _.each( $("div#help_area div#infoboxes div.infobox"), function(v) { $(v).hide() }) },
  setupGameMetaInfo : function() {
    var game = CURE.boardgame,
        msg = "";
    if( CURE.dataset == "mammal" ) {
      var fs = "features that distinguish";
      if ( game.max_hand == "1" ) { fs = "feature that distinguishes"; }
      msg = "Pick <strong>"+ game.max_hand +"</strong> "+ fs +" mammals from other creatures.  Think of things that separate mammals from fish, insects, amphibians, reptiles...";
    } else if ( CURE.dataset == "dream_breast_cancer" ) {
      msg = "Pick <b>"+ game.max_hand +"</b> genes that track breast cancer survival.  Look for genes that you think will have prognostic RNA expression or copy number variation.";
    }
    $("#modal").html("<p>"+ msg +"</p>").leanModal();
  },
  infoBoxGeneHeader : function(card_obj, targetEl) {
    targetEl.append("<h1><a target='_blank' href='http://www.ncbi.nlm.nih.gov/gene/"+ card_obj.unique_id +"'>"+ card_obj.short_name +"</a></h1>");
    targetEl.append("<h2>"+ (card_obj.long_name || "") +"</h2>");
    targetEl.append("<p>"+ (card_obj.description || "<span class='lightfade'>No RefSeq summary available.</span>") +"</p>");
  },
  setupHelpDisplay : function() {
    var game = CURE.boardgame,
        utils = CURE.utilities;
    $("span.help_label").hover(function(e) {
      var unique_id = $(this).parent().attr('id').split("card_")[1];
      game.cached_info_panel_unique_id = unique_id;
      var card_obj = _.find(game.cards, function(obj){ return obj.unique_id == unique_id; });
          card_metadata = card_obj.metadata;
          scope = $("#game_area #help_area #infoboxes")

      //-- Ontology
      var ontologyEl = $("div#ontology", scope).html("");
      game.infoBoxGeneHeader(card_obj, ontologyEl);

      _.each(card_metadata.ontology, function(v,i) {
        ontologyEl.append("<h3>"+ v.type +"</h3>")
        var values = [];
        _.each(v.values, function(v,i) {
          var ontolText = (i == 0) ? utils.upcaseStringFirstLetter( v.term ) : v.term;
          values.push("<a target='_blank' href='http://www.ebi.ac.uk/QuickGO/GTerm?id="+ v.accession +"'>"+ ontolText +"</a>");
        })
        ontologyEl.append("<p>"+ values.join(", ") +"</p>")
      })

      //-- Rifs
      var rifsEl = $("div#rifs", scope).html("");
      game.infoBoxGeneHeader(card_obj, rifsEl);

      var rifsList = rifsEl.append("<ol></ol>")
      _.each(card_metadata.rifs, function(v,i) {
        rifsList.append("<li><a target='_blank' href='http://www.ncbi.nlm.nih.gov/pubmed/"+ v.pubmed_id +"'>"+ utils.upcaseStringFirstLetter(v.text) +"</a></li>");
      })

      //-- Log this out
      card_metadata.ux.push({
        timestamp : (new Date).getTime(),
        panel : game.activeInfoPanel(),
        board_hover : true
      });

      //-- Don't play this card by event propagation to parent div.boardgame
      e.stopPropagation()
    })
  },
  moveBarney : function(moveChoice) {
    $("img#barney5").removeClass("win lose correct").hide().addClass(moveChoice).show();
  },
  moveClayton : function(moveChoice) {
    $("img#clayton1").removeClass("win lose correct").hide().addClass(moveChoice).show();
  },

  saveHand : function(player) {
    var game = CURE.boardgame,
        utils = CURE.utilities;
    if (player == 2) {
      return false;
    }

    game.metadata.game_finished = (new Date).getTime();
    game.metadata.search_term = $("input#search").val();
    //-- So this is a hack, but before submitting the cards, go through each one and delete all the non-gameplay metadata
    var cached_cards = game.cards;
    game.cards = _.map(game.cards, function(obj){
      delete obj.metadata.ontology;
      delete obj.metadata.rifs;
      return obj;
    });
    var app_state = _.pick(game, 'cards', 'p1_hand', 'p2_hand', 'p1_score', 'p2_score', 'metadata');

    var score_results = 0;
    if( game.p1_score > game.p2_score ) {
      score_results = 1;
    } else if ( game.p1_score == game.p2_score ) {
      score_results = 2;
    }

    var args = {
      command : "savehand",
      player1_id : CURE.user_id,
      player2_id : "215",
      win : score_results,
      game : app_state
    }
    $.ajax({
      type: 'POST',
      url: 'MetaServer',
      data: JSON.stringify(args),
      dataType: 'json',
      contentType: "application/json; charset=utf-8",
    });
    game.cards = cached_cards;
  },
  replayHand : function() {
    var game = CURE.boardgame;
    //-- Empty DIVs
    _.each([1,2], function(v) {
      $("span#p"+ v +"_score").html("0");
      $("div#p"+ v +"_hand").html("").unbind();
    })
    //-- This doesn't show the default text for now...
    $("#infoboxes div.infobox").html("");
    $("div#game_area div#board").html("");
    $("input#search").val("");

    //-- Emtpy vars
    game.cards = [];
    game.p1_hand = [];
    game.p2_hand = [];
    game.p1_score = 0;
    game.p2_score = 0;
    game.metadata = {
      game_started : (new Date).getTime(),
      mouse_action : []
    };
    game.board_state_clickable = true;
    game.cached_info_panel_unique_id = 0;
    game.init();
  },
  showTab : function(tab_id) {
    var game = CURE.boardgame;
    game.closeAllInfoTabs();
    $("#"+tab_id).show();
    //-- Highlight the one that is open
    _.each( $("div#help_area div#infoboxes div.infobox"), function(v) {
      var tabEl = $("div#tabs ul li." + $(v).attr("id") );
      if( $(v).is(":visible") ) {
        tabEl.addClass("highlight");
      } else {
        tabEl.removeClass("highlight");
      }
    })
  }
}

CURE.login = {
  init : function() {
    var utils = CURE.utilities;
    var bad = utils.getParameterByName("bad"),
        alertEl = $("#alertmsg")
    //start with new user area hidden
    if( bad == "nametaken" ) {
      alertEl.html("<p>Sorry, that user name has been taken. Please try another one.</p>").leanModal();
      $("#newuser").show();
      $("#olduser").hide();
    } else if( bad=="pw" ) {
      alertEl.html("<p>Sorry, that username/password combination is invalid. Please try again.</p>").leanModal();
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
//not sure where to set this but need to add that room parameter- this enables multiple boardrooms
CURE.boardroom = {
  init: function() {
	$("#expert").hide();  
    
	var args = {
      command : "boardroom",
      user_id : CURE.user_id,
      dataset : CURE.dataset,
      room : "1" //cure dataset room
    }
    var boardsCompleted = 0;
    
    $.getJSON("SocialServer", args, function(data) {
      CURE.boardroom.drawGrid("#boards", data, 100);
      
      if(data.n_won>3){
    	  $("#expert").fadeIn("slow");
      }
    });
  },

  drawGrid: function(targetEl, data, box_size) {
    //double check the sort/ordering by difficulty
    //data.boards = _.sortBy(data.boards, function(obj){ return -obj.base_score; })

    // Check to ensure the board is/can be a grid
    // this will clip the most difficult boards // thoughts?
   // var base = Math.sqrt( data.boards.length );
   // if ( Math.floor(base) !== base) {
   //   data.boards = _.first(data.boards, Math.pow( Math.floor(base), 2 ) );
   // }

    var attempt = d3.scale.linear()
      .domain([0, 1000])
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
          content = "",
          font_size = text_size;
     ( v.enabled == true ) ? isEnabled = "enabled" : isEnabled = "disabled";

     if ( v.enabled == false || v.attempts > 1000) {
        content = "&#8226;";
        font_size = font_size*3;
        top_pos = (hw*.2);
      }
      if ( v.trophy == true) {
        content = "&#9733;";
        font_size = font_size*1.5; //font_size*0.4;
        top_pos = Math.floor(hw*.2);
      }
      if ( v.enabled == false && v.trophy == true) {
        content = "&#9733;";
        top_pos = Math.floor(hw*.2);
      }

      if ( v.enabled == true && v.trophy == false && v.attempts <= 1000 ) {
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
          //params for game board
          var l = $(this).find(".symbol").html();
          var url = "boardgame.jsp?b="+ board_id +"&t=Level "+ l +" Breast Cancer Survival&h=5";
          window.location.href = url;
        })
      })
  }
}

CURE.landing = {
  init : function() {
    CURE.utilities.collapseInfo();
      /*var args = {
        command : "gamelogs"
      }
      $.getJSON("SocialServer", args, function(data) {
        //drawGraph(data, "#chart");
        var listItems = CURE.utilities.listOfLeaderBoard(data, 6);
        var olEl = $("div#leaderboard ol").append(listItems);
     });
     */
    $("#leaderboard").html("<h4 style='margin-top: 40px;'>Second rounder leaderboard underway...</h4>");
    $("input.playnow").click(function(e) {
      //link to the game, need to do anything else?
      var gameUrl = "login.jsp";
      window.location.replace(gameUrl);
      return false;
    })

    $("#column2 h4").glowText();

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
  drawPieChart : function(data, selEl) {
    var w = 400,
      h = 400,
      r = Math.min(w, h) / 2.4,
      labelr = r + 10, // radius for label anchor
      color = d3.scale.category20(),
      donut = d3.layout.pie(),
      arc = d3.svg.arc().innerRadius(r * .4).outerRadius(r);

    var scores = _.pluck(data, "score");
    var label_color = d3.scale.linear()
      .domain([_.min(scores), _.max(scores)])
      .range(["#FFF", "#000"]);

    var vis = d3.select(selEl)
      .append("svg:svg")
      .data([data])
      .attr("width", w + 200)
      .attr("height", h + 200);

    var arcs = vis.selectAll("g.arc")
      .data(donut.value(function(d) { return d.score }))
      .enter().append("svg:g")
      .attr("class", "arc")
      .attr("transform", "translate(" + (r + 130) + "," + (r+100) + ")");

    arcs.append("svg:path")
      .attr("fill", function(d, i) { return color(i); })
      .attr("d", arc);

    arcs.append("svg:text")
      .attr("transform", function(d) {
      var c = arc.centroid(d),
        x = c[0],
        y = c[1],
        // pythagorean theorem for hypotenuse
        h = Math.sqrt(x*x + y*y);
        return "translate(" + (x/h * labelr) +  ',' + (y/h * labelr) +  ")"; 
    })
    .attr("fill", function(d) {
      return label_color(d.value);
    })
    .attr("dy", ".35em")
    .attr("text-anchor", function(d) {
      // are we past the center?
      return (d.endAngle + d.startAngle)/2 > Math.PI ? "end" : "start";
    })
    .text(function(d, i) { return d.data.username; });
  },
  drawLineGraph : function(data, targetEl, yLabel){
    var format = d3.time.format.utc('%Y-%m-%d'),
        hw = 600,
        small_padding = hw*0.025,
        large_padding = hw*0.125,
        y_label = yLabel || "Y Axis",
        x_label = "Competition Run Time";

    var x_values = _.pluck(data, "timestamp");
    var x = d3.time.scale()
      .domain([format.parse('2012-09-01'), format.parse('2012-10-15')])
      .range([0, hw]);

    var y_values = _.pluck(data, "y");
    var y = d3.scale.linear()
      .domain([  _.min(y_values), _.max(y_values) ])
      .range([hw, 0]);

    // axes
    var xAxis = d3.svg.axis().scale(x);
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
      .x(function(d) { return x( d.timestamp ) })
      .y(function(d) { return y( d.y ) })
      .interpolate("basis");

    graphSvg.append('g')
      .attr('class', 'line')
      .attr('transform', 'translate('+ large_padding +', '+ small_padding +')')
      .append('path')
      .attr('class', 'line')
      .attr('d', line( data ));

    graphSvg.append('g')
      .attr('class', 'points')
      .attr('transform', 'translate('+ large_padding +', '+ small_padding +')').selectAll('circle')
      .data( data ).enter()
      .append('circle')
        .attr('r', 1.5)
        .attr('cx', function(d){ return x( d.timestamp ); })
        .attr('cy', function(d){ return y( d.y ); })

    // Axis labels
    graphSvg.append("text")
      .attr("class", "x label")
      .attr("text-anchor", "center")
      .attr("x", ((hw/2) - (x_label.length*2))+large_padding )
      .attr("y", hw + (large_padding - small_padding) )
      .text( x_label );

    graphSvg.append("g")
      .attr("transform", "translate(15, "+ (hw/2 + (y_label.length*2)) +")")
      .append("text")
      .attr("class", "y label")
      .attr("transform", "rotate(-90)")
      .attr("text-anchor", "center")
      .text( y_label );
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
  upcaseStringFirstLetter : function(text) { return text.charAt(0).toUpperCase() + text.slice(1); },
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
    console.log("DrawTree");
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
    console.log("DrawTreeNoLeaf");
    
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
