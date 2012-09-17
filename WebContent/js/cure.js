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
  init : function() {
    //set up accordian for result viewer
    //// $("#cv_results").accordion({ collapsible: true });
    $("#endgame").hide();
    //set up the board
    if (level != null && CURE.dataset == "mammal") {
      url = "MetaServer?dataset=mammal&command=getspecificboard&x="
        + ncols
        + "&y="
        + nrows
        + "&board=mammal_"
        + level;
    } else {
      url = "MetaServer?dataset="+ CURE.dataset +"&command=getboard&x="+ncols+"&y="+nrows+"&ran="+level;
    }
    //data will contain the array of cards used to build the board for this game
    $.getJSON(url, function(data) {
      cards = data;
      generateBoard();
      //add to hand
      setupHandAddRemove();
      //set up handlers
      // info box
      setupShowInfoHandler();
      //save hand
      setupHoldem();
      //set up opponent
      setupOpponent();
    });

    var cards = new Array();
        opponent_sort = new Array(),
        nrows = <%=nrows%>,
        ncols = <%=ncols%>,
        level = <%=level%>,
        showgeneinfo = <%=showgeneinfo%>,
        max_hand = <%=max_hand%>,
        p1_score = 0,
        p2_score = 0,
        par_score = 0,
        par = 0,
        p1_hand = new Array(),
        p1_indexes = new Array(),
        p2_hand = new Array(),
        p2_indexes = new Array(),
        features = "",
        feature_names = "",
        barney_init = 0,
        board_state_clickable = true,
        replay = "<%=full_request%>",
        multiplier = "<%=multiplier%>";
  },
  moveBarney : function(moveChoice) {
    $("#barney5").removeClass().hide().addClass(moveChoice).show();
  },
  moveClayton : function(moveChoice) {
    $("#clayton1").removeClass().hide().addClass(moveChoice).show();
  },
  evaluateHand : function(cardsinhand, player) {
    var url = 'MetaServer?dataset='+ CURE.dataset +'&command=getscore&features=';
    if( CURE.dataset == 'dream_breast_cancer') {
      url = 'MetaServer?dataset='+ CURE.dataset +'&command=getscore&geneids=';
      geneids = "";
      feature_names = "";
      var chand = "cardsinhand_" + player;
      $.each(cardsinhand, function(index, value) {
        geneids += value.unique_id + ",";
        feature_names += value.unique_id+":"+value.att_name + ":" + value.name + "|";
      });
      url += geneids;
    } else {
      features = "";
      feature_names = "";
      var chand = "cardsinhand_"+player;

      $.each(cardsinhand, function(index, value) {
        features+=value.att_index+",";
        feature_names += value.unique_id+":"+value.att_name + ":"+value.name+"|";
      });
      url +=features;
    }

    //goes to server, runs the default evaluation with a decision tree
    $.getJSON(url, function(data) {
      var treeheight = 250,
          treewidth = 420;
      //console.log(data.max_depth +" depth");
      if (data.max_depth > 2) {
        treeheight = 200 + 30*data.max_depth;
      }
      if (player == "1") {
        //draw the current tree
        $("#p1_current_tree").empty();
        drawTree(data, treewidth, treeheight, "#p1_current_tree");
        //sometimes randomness in cross-validation gets you a different score even without producing
        //any tree at all.
        if (data.max_depth < 2) {
          p1_score = 50;
        } else {
          p1_score = data.evaluation.accuracy;
        }
        if (p1_hand.length == max_hand) {
          $("#player1_j48_score").html('<strong> score ' + p1_score + '</strong>');
        }
        $("#game_score_1").text(p1_score);
      } else if (player == "2") {
        //draw the current tree
        $("#p2_current_tree").empty();
        drawTree(data, treewidth, treeheight, "#p2_current_tree");
        //same as above
        if (data.max_depth < 2) {
          p2_score = 50;
        } else {
          p2_score = data.evaluation.accuracy;
        }
        $("#player2_j48_score").html('<strong> score '+ p2_score +'</strong>');
        if (p2_hand.length == max_hand) {
          $("#player2_j48_score").html('<strong> score '+ p2_score +'</strong>');
        }
        $("#game_score_2").text(p2_score);
        board_state_clickable = true;
        if (p2_hand.length != max_hand && p1_score<p2_score&&p1_score>0) {
          moveBarney("correct"); //incorrect win lose
        } else if (p1_score > p2_score) {
          moveClayton("correct"); //incorrect win lose
        }

      }
      //if its the last hand, show the results
      if (p2_hand.length == max_hand) {
        saveHand();
        //show
        window.setTimeout(function() {
          var $tabs = $("#tabs").tabs();
          if (p1_score<p2_score&&p1_score>0) {
            $("#winner").text("Sorry, you lost this hand. ");
            $tabs.tabs('select', 4);
            moveBarney("win"); //incorrect win lose
            $("#winner").append("<br><a href=\""+replay+"\">Play Level Again?</a>");
          } else if (p1_score > p2_score && CURE.dataset=='mammal' && level==3) {
            $("#winner").parent().parent().css('background-color', 'pink');
            $("#winner").html("<h1>Congratulations! You finished your training!</h1> <p>You have gained access to the challenge area.</p><h2><a href=\"boardroom.jsp\">Start the challenge!</a></h2>");
            $("#holdem_button").hide();
            $tabs.tabs('select', 3);
            moveBarney("lose"); //incorrect win lose
            moveClayton("win");
          } else if (p1_score > p2_score) {
            $("#winner").parent().parent().css('background-color', '#FFA500');
            $("#winner").html(<h1>You beat Barney!</h1>You earned "
              + p1_score+ " * "+ multiplier
              + "= </strong><span style=\"font-size:30px;\">"
              + (p1_score * multiplier)
              + "</span> points!");
            $tabs.tabs('select', 3);
            moveBarney("lose"); //incorrect win lose
            moveClayton("win");
          } else if (p1_score == p2_score) {
            $("#winner").text("You tied Barney! ");
            $tabs.tabs('select', 3);
            moveBarney("win"); //incorrect win lose
            moveClayton("win");
            $("#winner").append("<br><a href=\""+replay+"\">Play Level Again?</a>");
          }
          $("#board").hide();
          $("#endgame").show();
        }, 1500);
      }
    });
  },
  getStyleByScore : function(power) {
    var colors = [ "#F8F0DF", "#F7E6C1", "#F7DB9B", "#F5CC6C", "#F5BE3F", "#F26B10"];
    var cellstyle = "width: 75px; position:relative; ";
    //color by estimated individual predictive power
    if (power == 0) {
      //cellstyle+=" background-color:"+colors[0]+";";
    } else if (power > 0 && power < 0.12) {
      cellstyle += " background-color:" + colors[0] + ";";
    } else if (power < 0.13) {
      cellstyle += " background-color:" + colors[1] + ";";
    } else if (power < 0.14) {
      cellstyle += " background-color:" + colors[2] + ";";
    } else if (power < 0.15) {
      cellstyle += " background-color:" + colors[3] + ";";
    } else if (power < 0.16) {
      cellstyle += " background-color:" + colors[4] + ";";
    } else if (power > 0.15) {
      cellstyle += " background-color:" + colors[5] + ";";
    }
    return cellstyle;
  },
  getChessStyle : function(cell_index) {
    return "width: 75px; position:relative; background-color:#F8F0DF;";
  },
  generateBoardCell : function(cardindex) {
    var boardhtml = "";
    var displayname = cards[cardindex].name;
    var power = cards[cardindex].power;
    var cellstyle = getChessStyle(cardindex);
    if (displayname == null || displayname.length == 0) {
      displayname = cards[cardindex].att_name;
    }
    cards[cardindex].displayname = displayname;
    boardhtml += "<td><div style=\"margin:5px "+cellstyle+"\">";
    //style=\"position:absolute; top:4; right:0;\"
    if (showgeneinfo == "1") {
      boardhtml += "<div class=\"feature_name\" id=\""+cards[cardindex].unique_id+"\" style=\"position:absolute;top:4px; right:0px;\"><a href=\"#\"><img src=\"images/info-icon.png\"></a></div>";
    }
    //	boardhtml+="<div class=\"select_card_button\" id=\"card_index_"+cardindex+"\"><a title=\"add to hand\" class=\"selectable\" style=\"color:black;\" href=\"#\">"+displayname+"</a></div></td>";
    boardhtml += "<div class=\"select_card_button btn btn-primary\" id=\"card_index_"+cardindex+"\"><a title=\"add to hand\" class=\"selectable small_level_button\" style=\"height:60px; line-height:60px; text-align:center; font-size:13; width:60px; text-decoration:none;\" href=\"#\">"
      + displayname + "</a></div></div></td>";
    return boardhtml;
  },
  generateHandCell : function(cardindex, player) {
    /**
     * cardindex refers to the location on the board and the handle in the board card Map
    **/
    var boardhtml = "",
        displayname = cards[cardindex].name,
        power = cards[cardindex].power,
        cellstyle = getStyleByScore(power);
    if (displayname == null || displayname.length == 0) {
      displayname = cards[cardindex].att_name;
    }
    cards[cardindex].displayname = displayname;
    boardhtml += "<td><div style=\"margin:5px "+cellstyle+"\">";
    if (showgeneinfo == "1") {
      boardhtml += "<div class=\"feature_name\" id=\""+cards[cardindex].unique_id+"\" style=\"position:absolute; top:4; right:0;\"><a href=\"#\"><img src=\"images/info-icon.png\"></a></div>";
    }
    //	boardhtml+="<div class=\"select_card_button\" id=\"card_index_"+cardindex+"\"><a title=\"add to hand\" class=\"selectable\" style=\"color:black;\" href=\"#\">"+displayname+"</a></div></td>";
    boardhtml += "<div class=\"select_card_button btn btn-primary\" style=\"cursor:default;\" id=\"card_index_"+cardindex+"\"><span class=\"selectable small_level_button\" style=\"cursor:default; height:60px; line-height:60px; text-align:center; font-size:13; width:60px; text-decoration:none;\">"
      + displayname + "</span></div></div></td>";
    return boardhtml;
  },
  generateUsedBoardCell : function(cardindex) {
    var boardhtml = "";
    var displayname = cards[cardindex].name;
    var power = cards[cardindex].power;
    var cellstyle = getStyleByScore(power);
    if (displayname == null || displayname.length == 0) {
      displayname = cards[cardindex].att_name;
    }
    cards[cardindex].displayname = displayname;
    boardhtml += "<td style=\""+cellstyle+"\">";
    boardhtml += "<div class=\"feature_name\" id=\""+cards[cardindex].unique_id+"\">"
      + displayname + "<br/>Used..</div>";
    return boardhtml;
  },
  setupOpponent : function() {
    opponent_sort = cards.slice(0);
    //maintain the indexes to the board
    $.each(opponent_sort, function(index, value) {
      opponent_sort[index].board_index = index;
    });
    //rank by the power value - now ascending
    opponent_sort.sort(function(a, b) {
      return a.power - b.power;
    });
  },
  generateBoard : function() {
    var cardindex = -1;
    var boardhtml = "<table >"; //border=\"4\" bordercolor=\"orange\"
    for ( var r = 0; r < nrows; r++) {
      boardhtml += "<tr align=\"center\" style=\"height: 75px\";>";
      for ( var c = 0; c < ncols; c++) {
        cardindex++;
        var boardcell = generateBoardCell(cardindex);
        boardhtml += boardcell;
      }
      boardhtml += "</tr>";
    }
    boardhtml += "</table>";
    $("#board").empty();
    $("#board").append(boardhtml);

    //set the par - use all the features on the board
    var url = 'MetaServer?dataset='+ CURE.dataset +'&command=getscore&features=';
    var features = "";
    $.each(cards, function(index, value) {
      features+=value.att_index+",";
    });
    url +=features;

    $.getJSON(url, function(data) {
      var par = data.evaluation.accuracy;
      $("#par_score").html("<strong>"+par+"</strong>");
    });
  },
  showgene : function(geneid, name) {
    $("#infobox_header").empty();
    var gene_url = 'http://mygene.info/gene/'+geneid+'?filter=name,symbol,summary,go,genomic_pos&jsoncallback=?';
    if(geneid!="_"&&geneid!=""){
      //mygene info
      $.getJSON(gene_url, mygene_info_get_gene_callback);
      //ncbi eutils
      args = {'apikey' : 'ba0b21611890b5bc23c8c57033001a47', 'db' : 'gene', 'id' : geneid};
      $.getJSON('http://entrezajax.appspot.com/efetch?callback=?', args, entrezajax_callback);
    } else {
      $("#infobox").empty();
      $("#infobox").append('<strong>'+name+'</strong><h1>Mystery Card!</h1><p>No data available</p>');
    }
  },
  entrezajax_callback : function(data) {
    $("#rifs").empty();
    $("#ncbi_phenos").empty();
    $.each(data.result, function(i, item) {
      var generif_list = '<ul>';
      var phenotypes = '<ul>';
      var haspheno = false;
      for(var i = 0; i < item.Entrezgene_comments.length; i ++) {
        if( item.Entrezgene_comments[i]["Gene-commentary_type"] == 18 ) {
          var pmid_obj = item.Entrezgene_comments[i]["Gene-commentary_refs"];
          var pmid;
          if(pmid_obj) {
            pmid = pmid_obj[0].Pub_pmid.PubMedId;
          }
          var riftext = item.Entrezgene_comments[i]["Gene-commentary_text"];
          if(riftext) {
            generif_list += '<li><a target=\"blank\" href=\'http://www.ncbi.nlm.nih.gov/pubmed/' + pmid + '\'>' + riftext + '</a></li>';
          }
        } else if(item.Entrezgene_comments[i]["Gene-commentary_type"] == 254 ) {
          var pheno_obj = item.Entrezgene_comments[i]["Gene-commentary_comment"];
          if(pheno_obj){
            for(var p=0; p<pheno_obj.length; p++) {
              if(pheno_obj[p]["Gene-commentary_type"]==19) {
                phenotypes += '<li>'+pheno_obj[p]["Gene-commentary_heading"]+"</li>";
                haspheno = true;
              }
            }
          }
        }
      }
      generif_list += '</ul>'; phenotypes += '</ul>';
      generif_list += '</ul>'; phenotypes += '</ul>';
      $("#rifs").append(generif_list);
      if(haspheno) {
        $("#gene_description").append("<div id=\"ncbi_phenos\">Phenotypes<br>"+phenotypes+"</div>");
      }
    });
  },
  mygene_info_get_gene_callback : function(result) {
    $("#gene_description").empty();
    $("#ontology").empty();
    $("#ncbi_phenos").empty();
    if (result && result.name && result.symbol){
      var chromosome = "";
      if(result.genomic_pos) {
        chromosome = result.genomic_pos.chr;
      }
      $("#gene_description").append('<a href=\"http://www.ncbi.nlm.nih.gov/gene/'+result._id+'\" target = "blank">'+result.symbol+' : '+result.name+' (chr '+chromosome+ ')</a><p>'+result.summary+'</p>');
      if(result.go) {
        if(result.go.BP) {
          var ps_list = result.go.BP;
          if (!$.isArray(ps_list)){
            ps_list = [ps_list];
          }
          $("#ontology").append("<p><strong>Biological Processes</strong><br>");
          $.each(ps_list, function(i, ps) {
            $("#ontology").append(ps.term+', ');
          });
          $("#ontology").append("</p>");
        }
        if(result.go.CC) {
          var ps_list = result.go.CC;
          if (!$.isArray(ps_list)) {
            ps_list = [ps_list];
          }
          $("#ontology").append("<p><strong>Cellular Component</strong><br>");
          $.each(ps_list, function(i, ps){
            $("#ontology").append(ps.term+', ');
          });
          $("#ontology").append("</p>");
        }
        if(result.go.MF) {
          var ps_list = result.go.MF;
          if (!$.isArray(ps_list)) {
            ps_list = [ps_list];
          }
          $("#ontology").append("<p><strong>Molecular Functions</strong><br>");
          $.each(ps_list, function(i, ps) {
            $("#ontology").append(ps.term+', ');
          });
          $("#ontology").append("</p>");
        }
      }
    } else {
      $("#gene_description").append('<p>No description available for this gene.</p>');
    }
  },
  setupShowInfoHandler : function() {
    //console.log("setting showInfoHandlers");
    var $tabs = $("#tabs").tabs();
    //reset and rebind (#todo - this unbind hack is here because of the way cards are added to hands, should improve that so we don't hav to rebind very element on the baord..)
    $(".feature_name").unbind("click");
    $(".feature_name").on("click", function () {
      var cell_id = this.id;
      var name = this.innerText;
      showgene(cell_id, name);
      // $tabs.tabs('select', 0); //it always goes back to the gene description on an info request
    });
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
  setupHoldem : function(){
    //add the save handler
    $("#holdem_button").on("click", function () {
      window.location.replace("<%=mosaic_url%>");
    });
  },
  randomXToY : function(minVal,maxVal,floatVal) {
    //function to get random number upto m
    var randVal = minVal+(Math.random()*(maxVal-minVal));
    return typeof floatVal=='undefined'?Math.round(randVal):randVal.toFixed(floatVal);
  },
  getBarneysNextCardGettingHarder : function() {
    var lower = level;
    if((lower+10)>=cards.length) {
      lower = cards.length - 11;
    }
    var upper = lower+10;

    sorted_index = randomXToY(lower,upper);
    //console.log("sorted index "+sorted_index);
    card_index = opponent_sort[sorted_index].board_index+"";
    if(
        ( $.inArray(card_index, p1_indexes) == -1 ) &&
        ( $.inArray(card_index, p2_indexes) == -1 )
      ) {
        return card_index;
    } else{
      return getBarneysNextCard();
    }
  },
  getBarneysNextCard : function(){
    sorted_index = randomXToY(0, cards.length - 1);
    card_index = opponent_sort[sorted_index].board_index+"";
    if(
        ($.inArray(card_index, p1_indexes) == -1) &&
        ($.inArray(card_index, p2_indexes) == -1)
        ){
		return card_index;
	}else{
		//console.log(" iterated on "+card_index);
		return getBarneysNextCard();
	}
}
 

function addCardToBarney(){
	card_index = getBarneysNextCard();
	p2_indexes.push(card_index);
	var handcell = generateHandCell(card_index);
	
	window.setTimeout(function() {  
		$("#player2_hand").fadeTo(500, 1, function (){
			$("#player2_hand").append(handcell);
			setupShowInfoHandler();
		//boardhtml+="<div class=\"feature_name\" id=\""+cards[cardindex].unique_id+"\" style=\"position:absolute; top:0; right:0;\"><a href=\"#\"><img src=\"images/info-icon.png\"></a></div>";

			//here
			/* var $tabs = $("#tabs").tabs();
			$(".feature_name").on("click", function () {
				var cell_id = this.id;
				var name = this.innerText;
				showgene(cell_id, name);
				$tabs.tabs('select', 0); //it always goes back to the gene description on an info request
	  		}); */
			//
			
		});
	}, 700); 
	
	p2_hand.push(cards[card_index]);
		
	//hide button from board
	card_index = "#card_index_"+card_index;
	
	$(card_index).parent().fadeTo(500, 0.75, function (){
		$(card_index).parent().css('background-color', 'transparent');
		$(card_index).parent().html("");
	});
	
	evaluateHand(p2_hand, "2");
	//console.log(board_state_clickable);
}

function saveSelection(card){
	var geneid = card.unique_id;
	var saveurl = "MetaServer?dataset="+ CURE.dataset +"&command=playedcard&board_id="+level+"&player_name="+ CURE.username +"&player_id="+ CURE.user_id +"&geneid="+geneid;
	$.getJSON(saveurl, function(data) {
		//console.log("saved a card");
		//console.log(saveurl);
	});
}

function setupHandAddRemove(){
	$(".select_card_button").on("click", function () {
		if(board_state_clickable){
		if(p1_hand){
			hand_size = p1_hand.length;
		}else{
			hand_size = 0;
		}
		if(hand_size < max_hand){
			var cell_id = this.id.replace("card_index_", "");
			p1_indexes.push(cell_id);			
			var handcell = generateHandCell(cell_id);
			$("#player1_hand").fadeTo(1000, 1, function (){
				$("#player1_hand").append(handcell);
				setupShowInfoHandler();
			});
			p1_hand.push(cards[cell_id]);
			saveSelection(cards[cell_id]);
			evaluateHand(p1_hand, 1);
			//hide button from board
			$(this).parent().fadeTo(500, 0.75, function (){
				$(this).css('background-color', 'transparent');
				$(this).html("");
				//add a card to barney's hand
				board_state_clickable = false;
				addCardToBarney();
			});
					
		}else{
			alert("Sorry, you can only have 5 cards in your hand in this game."); 
		}
		}else{
			alert("Wait your turn!"); 
		}
	  });
}

function createTooltip(event){       
	
    $('body').append('<div class="tooltippy"><p>Click a gene to add it to your hand</p></div>');
	positionTooltip(event);        
};

function positionTooltip(event){
    var tPosX = event.pageX - 10;
    var tPosY = event.pageY - 100;
    $('div.tooltippy').css({'position': 'absolute', 'top': tPosY, 'left': tPosX, 'background-color': 'white' });
    //console.log("moused over 3");
};

function hideTooltip(event){       
	$('div.tooltippy').hide();
	positionTooltip(event);        
};

	












  
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
    var url = "/cure/SocialServer?command=boardroom&username="+CURE.username+"&dataset="+CURE.dataset;
    $.getJSON(url, function(data) {
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
  }
}

$(document).ready(function() {
  CURE.load();
});
