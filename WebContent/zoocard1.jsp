<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%@ page import="org.scripps.combo.weka.Weka"%>
<%@ page import="org.scripps.combo.Player"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.ArrayList"%>

<%
	String level = request.getParameter("level");

	int nrows = 1;
	int ncols = 2;
	int ilevel = Integer.parseInt(level);
	int hand_size = 1;
	if (ilevel > 3) {
		nrows = 2;
		hand_size = 2;
	}

	String username = "";
	Player player = (Player) session.getAttribute("player");
	if (player == null) {
		response.sendRedirect("/combo/login.jsp");
	} else {
		username = player.getName();
		//refresh.. ack ugly..
		player = Player.lookupPlayer(username);
	}
	if (player != null) {
%>


<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html">


<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<meta name="description" content="A game">
<meta name="author" content="Ben">

<link rel="stylesheet" href="assets/css/combo_bootstrap.css"
	type="text/css" media="screen">
<link rel="stylesheet" href="assets/css/combo.css" type="text/css"
	media="screen">
<style>
body {
	padding-top: 60px;
	/* 60px to make the container go all the way to the bottom of the topbar */
}
</style>



<title>COMBO: Mammal Mosaic</title>

<%
	String ran = request.getParameter("level");
		if (ran == null) {
			ran = "" + (int) Math.rint(Math.random() * 1000);
		}
%>
<link
	href="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8/themes/base/jquery-ui.css"
	rel="stylesheet" type="text/css" />
<script src="//ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js"
	type="text/javascript"></script>
<script
	src="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8/jquery-ui.min.js"></script>

<script>	
var cards = new Array();
var opponent_sort = new Array();
var seed = <%=ran%>;
var level = <%=level%>;

var nrows = <%=nrows%>;
var ncols = <%=ncols%>;

var max_hand = <%=nrows%>;
var p1_score = 0;
var p2_score = 0; 
var par_score = 0;
var par = 0;
var p1_hand = new Array();
var p1_indexes = new Array();
var p2_hand = new Array();
var p2_indexes = new Array();
var player_name = "<%=username%>";
	var features = "";
	var feature_names = "";
	var barney_init = 0;

	//playSound("sounds/ray_gun-Mike_Koenig-1169060422.wav");
	function playSound(url) {
		//document.getElementById("sound").innerHTML = "<embed src='"+url+"' hidden=true autostart=true loop=false>";
	}

	function evaluateHand(cardsinhand, player) {
		var url = 'MetaServer?dataset=mammal&command=getscore&features=';
		features = ""; 
		feature_names = "";
		var chand = "cardsinhand_" + player;

		$.each(cardsinhand, function(index, value) {
			features += value.att_index + ",";
			feature_names += value.att_name + ":"+value.name+"|";
		});
		url += features;

		//goes to server, runs the default evaluation with a decision tree
		$.getJSON(url, function(data) {
			if (player == "1") {
				//var prev_score = p1_score;
				$("#player1_j48_score").html(
						'<strong> score </strong><span>"' + data.accuracy
								+ '</span><p><pre>' + data.modelrep
								+ '</pre></p>');
				p1_score = data.accuracy;
				if (p1_score >= p2_score) {
					playSound("sounds/human/MMMMM1.WAV");
				} else {
					playSound("sounds/human/SNORTAHH.WAV");
				}
				$("#game_score_1").text(p1_score);
			} else if (player == "2") {
				//var prev_score = p2_score;
				$("#player2_j48_score").html(
						'<strong> score </strong><span>"' + data.accuracy
								+ '</span><p><pre>' + data.modelrep
								+ '</pre></p>');
				p2_score = data.accuracy;
				if (p2_score < p1_score) {
					playSound("sounds/human/MMMMM1.WAV");
				} else {
					playSound("sounds/human/SNORTAHH.WAV");
				}
				$("#game_score_2").text(p2_score);
			}
		});
	}

	function getStyleByScore(power) {
		var colors = [ "#F8F0DF", "#F7E6C1", "#F7DB9B", "#F5CC6C", "#F5BE3F",
				"#F26B10" ];
		var cellstyle = "width: 75px; position:relative; ";
		//color by estimated individual predictive power
		if (power == 0) {
			cellstyle += " background-color:" + colors[0] + ";";
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
	}

	function getChessStyle(cell_index) {
		var cellstyle = "width: 75px; position:relative; ";
		if (cell_index % 2 == 0) {
			cellstyle += " background-color:#F26B10;";
		} else {
			cellstyle += " background-color:#F8F0DF;";
		}
		return cellstyle;
	}

	function getOneColorStyle(cell_index) {
		var cellstyle = "width: 75px; position:relative;  background-color:#F8F0DF;";
		return cellstyle;
	}

	function generateBoardCell(cardindex) {
		var boardhtml = "";
		var displayname = cards[cardindex].name;
		var power = cards[cardindex].power;
		var cellstyle = getOneColorStyle(cardindex);
		if (displayname == null || displayname.length == 0) {
			displayname = cards[cardindex].att_name;
		}
		cards[cardindex].displayname = displayname;
		boardhtml += "<td style=\""+cellstyle+"\">"; //<div class=\"feature_name\" id=\""+cards[cardindex].unique_id+"\" style=\"position:absolute; top:0; right:0;\"><a href=\"#\"><img src=\"images/info-icon.png\"></a></div>
		boardhtml += "<div class=\"select_card_button\" id=\"card_index_"+cardindex+"\"><a title=\"add to hand\" class=\"selectable\" style=\"color:black;\" href=\"#\">"
				+ displayname + "</a></div></td>";

		return boardhtml;
	}

	/**
	 * cardindex refers to the location on the board and the handle in the board card Map
	 */
	function generateHandCell(cardindex, player) {
		var boardhtml = "";
		var displayname = cards[cardindex].name;
		var power = cards[cardindex].power;
		var cellstyle = getStyleByScore(power);
		if (displayname == null || displayname.length == 0) {
			displayname = cards[cardindex].att_name;
		}
		cards[cardindex].displayname = displayname;
		boardhtml += "<td style=\""+cellstyle+"\">"; //<div class=\"feature_name\" id=\""+cards[cardindex].unique_id+"\" style=\"position:absolute; top:0; right:0; z-index:2;\"><a href=\"#\"><img src=\"images/info-icon.png\"></a></div>
		boardhtml += "<div class=\"select_card_button\" id=\"card_index_"+cardindex+"\" style=\"background-color: white; z-index:3;\">"
				+ displayname + "</div></td>";

		return boardhtml;
	}

	function generateUsedBoardCell(cardindex) {

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
	}

	function setupOpponent() {
		opponent_sort = cards.slice(0);
		//maintain the indexes to the board
		$.each(opponent_sort, function(index, value) {
			opponent_sort[index].board_index = index;
		});
		//rank by the power value - now ascending
		opponent_sort.sort(function(a, b) {
			return a.power - b.power;
		});
		//check
		//$.each(opponent_sort, function(index, value) {
		//    console.log(index+" "+value.power+" "+opponent_sort[index].board_index);
		//  });
	}

	function generateBoard() {
		var cardindex = -1;
		var boardhtml = "<table border=\"1\">";
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
		var url = 'MetaServer?dataset=mammal&command=getscore&features=';
		var features = "";
		$.each(cards, function(index, value) {
			features += value.att_index + ",";
		});
		url += features;

		$.getJSON(url, function(data) {
			par = data.accuracy;
			$("#par_score").html("<strong>" + par + "</strong>");
		});
	}

	function showgene(geneid, name) {
		var gene_url = 'http://mygene.info/gene/' + geneid
				+ '?filter=name,symbol,summary,go,genomic_pos&jsoncallback=?';
		//    show_loading("#infobox");
		if (geneid != "_" && geneid != "") {
			$.getJSON(gene_url, mygene_info_get_gene_callback);
		} else {
			$("#infobox").empty();
			$("#infobox")
					.append(
							'<strong>'
									+ name
									+ '</strong><h1>Mystery Card!</h1><p>No data available</p>');
		}
	}

	function mygene_info_get_gene_callback(result) {
		$("#infobox").empty();
		if (result && result.name && result.symbol) {
			var chromosome = "";
			if (result.genomic_pos) {
				chromosome = result.genomic_pos.chr;
			}
			$("#infobox").append(
					'<strong>' + result.symbol + ' : ' + result.name + ' (chr '
							+ chromosome + ')</strong><p>' + result.summary
							+ '</p>');

			if (result.go.BP) {
				var ps_list = result.go.BP;
				if (!$.isArray(ps_list)) {
					ps_list = [ ps_list ];
				}
				$("#infobox").append(
						"<p><strong>Biological Processes</strong><br>");
				$.each(ps_list, function(i, ps) {
					$("#infobox").append(ps.term + ', ');
				});
				$("#infobox").append("</p>");
			}
			if (result.go.CC) {
				var ps_list = result.go.CC;
				if (!$.isArray(ps_list)) {
					ps_list = [ ps_list ];
				}
				$("#infobox").append(
						"<p><strong>Cellular Component</strong><br>");
				$.each(ps_list, function(i, ps) {
					$("#infobox").append(ps.term + ', ');
				});
				$("#infobox").append("</p>");
			}
			if (result.go.MF) {
				var ps_list = result.go.MF;
				if (!$.isArray(ps_list)) {
					ps_list = [ ps_list ];
				}
				$("#infobox").append(
						"<p><strong>Molecular Functions</strong><br>");
				$.each(ps_list, function(i, ps) {
					$("#infobox").append(ps.term + ', ');
				});
				$("#infobox").append("</p>");
			}
		} else {
			$("#infobox").append('<p>No data available for this gene.</p>');
		}
	}

	function setupShowInfoHandler() {
		$(".feature_name").on("click", function() {
			var cell_id = this.id;
			var name = this.innerText;
			showgene(cell_id, name);
		});
	}

	function saveHand() {
		par_score = p1_score - par;
		var win = "0";
		if (p1_score > p2_score) {
			win = "1";
		}
		var saveurl = 'MetaServer?dataset=mammal&command=savehand&features='
				+ features + '&player_name=' + player_name + '&score='
				+ par_score + '&training_accuracy=' + p1_score + '&board_id=' + level
				+ "&win=" + win+'&game=training_verse_barney&feature_names=' + feature_names;
		//player_name , score, cv_accuracy, board_id
		console.log("saved " + saveurl);
		$.getJSON(saveurl, function(data) {
			window.location.replace("mammal_mosaic.jsp");
		});
	}

	function setupHoldem() {
		//add the save handler
		$("#holdem_button").on("click", function() {
			saveHand();
		});
	}

	//function to get random number upto m
	function randomXToY(minVal, maxVal, floatVal) {
		var randVal = minVal + (Math.random() * (maxVal - minVal));
		return typeof floatVal == 'undefined' ? Math.round(randVal) : randVal
				.toFixed(floatVal);
	}

	function getBarneysNextCard() {
		/* var lower = seed;
		if((lower+10)>=cards.length){
			lower = cards.length - 11;
		}
		var upper = lower+10; */

		sorted_index = randomXToY(0, cards.length - 1);
		//	console.log("sorted index "+sorted_index);
		card_index = opponent_sort[sorted_index].board_index + "";

		if ((($.inArray(card_index, p1_indexes) == -1) && ($.inArray(
				card_index, p2_indexes) == -1))) {
			//console.log(p1_indexes);
			//console.log(p2_indexes);//
			//console.log(" returned "+card_index);
			return card_index;
		} else {
			//console.log(" iterated on "+card_index);
			return getBarneysNextCard();
		}
	}

	function addCardToBarney() {
		card_index = getBarneysNextCard();
		p2_indexes.push(card_index);
		var handcell = generateHandCell(card_index);

		window.setTimeout(function() {
			$("#player2_hand").fadeTo(500, 1, function() {
				$("#player2_hand").append(handcell);
				setupShowInfoHandler();
			});
		}, 700);

		p2_hand.push(cards[card_index]);

		//hide button from board
		card_index = "#card_index_" + card_index;

		$(card_index).parent().fadeTo(500, 0.75, function() {
			$(card_index).parent().css('background-color', '#FBBBB9');
			$(card_index).parent().html("");
		});

		evaluateHand(p2_hand, "2");
		window.setTimeout(function() {
			if (p2_hand.length == max_hand) {
				if (p1_score < p2_score) {
					$("#winner").text("Sorry, you lost this hand. ");
				} else {
					$("#winner").text("You beat Barney! ");
				}
				$("#endgame").show();
			}
		}, 3000);
	}

	function setupHandAddRemove() {
		$(".select_card_button").on(
				"click",
				function() {
					if (p1_hand) {
						hand_size = p1_hand.length;
					} else {
						hand_size = 0;
					}
					if (hand_size < max_hand) {
						var cell_id = this.id.replace("card_index_", "");
						p1_indexes.push(cell_id);
						//temp add it to barney's hand		
						addCardToBarney();

						var handcell = generateHandCell(cell_id);
						$("#player1_hand").fadeTo(1000, 1, function() {
							$("#player1_hand").append(handcell);
							setupShowInfoHandler();
						});
						p1_hand.push(cards[cell_id]);
						//			window.setTimeout(function() { 
						evaluateHand(p1_hand, 1);
						//			}, 2000); 
						//hide button from board
						//$(this).parent().fadeOut(1);
						$(this).parent().fadeTo(500, 0.75, function() {
							$(this).css('background-color', '#82CAFA');
							$(this).html("");
						});

					} else {
						alert("Sorry, you can only have " + max_hand
								+ " cards in your hand in this game. ");
					}
				});
	}

	function setupHandAddRemoveFirstVersion() {
		$(".select_card_button")
				.on(
						"click",
						function() {
							if (p1_hand) {
								hand_size = p1_hand.length;
							} else {
								hand_size = 0;
							}
							if (hand_size < max_hand) {
								var cell_id = this.id
										.replace("card_index_", "");
								var handcell = generateHandCell(cell_id);
								// does not work in firefox	
								//	$(handcell).hide().appendTo("#player1_hand").fadeIn(1000);
								$("#player1_hand").append(handcell);
								setupShowInfoHandler();
								p1_hand.push(cards[cell_id]);
								evaluateHand(p1_hand);
								//hide button from board
								$(this).hide();
								//change background color
								$(this).parent().css('background-color',
										'#98AFC7');
								//add the take out of hand handler
								$(".cardsinhand")
										.on(
												"click",
												function() {
													var cell_id = this.id
															.replace("p1_c_",
																	"");
													//put it back on the board
													var table_cell_id = "#card_index_"
															+ cell_id;
													var cellcontents = generateUsedBoardCell(cell_id);
													//$(handcell).hide().appendTo("#player1_hand").fadeIn(1000);

													$(table_cell_id).parent()
															.html(cellcontents);
													//take it out of our hand representation
													var tmp = new Array();
													var tmp_index = 0;
													for ( var r = 0; r < p1_hand.length; r++) {
														if (p1_hand[r] != cards[cell_id]) {
															tmp[tmp_index] = p1_hand[r];
															tmp_index++;
														}
													}
													p1_hand = tmp;
													//re-evaluate
													if (p1_hand != null
															&& p1_hand.length > 0) {
														evaluateHand(p1_hand);
													}
													//remove button from hand
													$(this).parent().remove(); //fadeOut(1000);
												});

							} else {
								alert("Sorry, you can only have 5 cards in your hand in this game.  Click a card to remove it from your hand.");
							}
						});
	}

	function createTooltip(event) {

		$('body')
				.append(
						'<div class="tooltippy"><p>Click a gene to add it to your hand</p></div>');
		positionTooltip(event);
	};

	function positionTooltip(event) {
		var tPosX = event.pageX - 10;
		var tPosY = event.pageY - 100;
		$('div.tooltippy').css({
			'position' : 'absolute',
			'top' : tPosY,
			'left' : tPosX,
			'background-color' : 'white'
		});
		console.log("moused over 3");
	};

	function hideTooltip(event) {
		$('div.tooltippy').hide();
		positionTooltip(event);
	};

	$(document)
			.ready(
					function() {
						var agent = navigator.userAgent;
						if ((agent.indexOf("Safari") == -1)
								&& (agent.indexOf("Chrome") == -1)) {
							alert("Sorry, this only works on Chrome and Safari right now... \nLooks like you are using \n"
									+ agent);
						}
						//set up accordian for result viewer
						// $("#cv_results").accordion({ collapsible: true });
						//hide the end button
						$("#endgame").hide();
						//set up the board
						if (level != null) {
							url = "MetaServer?dataset=mammal&command=getspecificboard&x="
									+ ncols
									+ "&y="
									+ nrows
									+ "&board=mammal_"
									+ level;
						} else {
							url = "MetaServer?dataset=mammal&command=getboard&x="
									+ ncols + "&y=" + nrows;
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
							//add mouseover handler for genes
							/* $('.selectable').mouseover(function(event) {
							    createTooltip(event);               
							}).mouseout(function(){
							    hideTooltip(); 
							}); */

						});

						//show default empty results
						//$("#cv_results").accordion( "activate" , 1 );

					});
</script>
</head>
<body>
	<div class="navbar navbar-fixed-top">
		<div class="navbar-inner">
			<div class="container">
				<a class="btn btn-navbar" data-toggle="collapse"
					data-target=".nav-collapse"> <span class="icon-bar"></span> <span
					class="icon-bar"></span> <span class="icon-bar"></span> </a> <a
					class="brand" href="/combo/">COMBO</a>
				<div class="nav-collapse">
					<ul class="nav">
						<li><a href="help.jsp" target="blank">Help!</a>
						</li>
						<li><a href="index.jsp">logout</a>
						</li>
					</ul>
				</div>
				<!--/.nav-collapse -->
			</div>
		</div>
	</div>

	<div id="board_container"
		style="height: 200px; left: 190px; position: absolute; top: 300px; width: 200px;"">
		<h2>Pick a feature</h2>
		<div id="board"></div>
	</div>

	<div id="game_score_box_1"
		style="text-align: center; left: 410px; position: absolute; top: 630px; width: 200px; z-index: 2;">
		<h4>Your score</h4>
		<h1 id="game_score_1" style="text-align: center;">0</h1>
	</div>

	<div id="player1_title_area"
		style="left: 30px; position: absolute; top: 590px;">
		<h3>Your hand</h3>
	</div>

	<div id="player1"
		style="height: 500px; left: 30px; position: absolute; top: 565px; z-index: 1;">
		<div id="hand_info_box_1"
			style="position: relative; top: 45px; width: 500px;">
			<div id="player_box_1"
				style="position: relative; top: 15px; width: 400px;">
				<table border='1'>
					<tr id="player1_hand" align='center' style='height: 75px;'>

					</tr>
				</table>
			</div>
		</div>
	</div>

	<div id="player1_masked"
		style="height: 500px; left: 30px; position: absolute; top: 565px; z-index: -1;">
		<div id="hand_info_box_masked_1"
			style="position: relative; top: 45px; width: 500px;">
			<div id="player_box_masked_1"
				style="position: relative; top: 15px; width: 400px;">
				<table border='1'>
					<tr id="player1_hand_masked" align='center'
						style='height: 75px; background-color: #82CAFA'>
						<%
							for (int i = 0; i < hand_size; i++) {
						%>
						<td style="width: 75px;">?</td>
						<%
							}
						%>
					</tr>
				</table>
			</div>
		</div>
	</div>


	<div id="game_score_box_2"
		style="text-align: center; left: 410px; position: absolute; top: 60px; width: 200px; z-index: 2;">
		<img src="images/barney.png">
		<!--  Level <%=ran%> -->
		<h4>Barney's score</h4>
		<h1 id="game_score_2" style="text-align: center;">0</h1>
	</div>

	<div id="player2_title_area"
		style="left: 30px; position: absolute; top: 80px;">
		<h3>Barney's hand</h3>
	</div>

	<div id="player2" style="left: 30px; position: absolute; top: 55px;">
		<div id="hand_info_box_2"
			style="position: relative; top: 45px; width: 500px;">
			<div id="player_box_2"
				style="position: relative; top: 15px; width: 400px;">
				<table border='1'>
					<tr id="player2_hand" align='center' style='height: 75px;'>

					</tr>
				</table>
			</div>
		</div>
	</div>

	<div id="player2_masked"
		style="left: 30px; position: absolute; top: 55px; z-index: -1">
		<div id="hand_info_box_masked_2"
			style="position: relative; top: 45px; width: 500px;">
			<div id="player_box_masked_2"
				style="position: relative; top: 15px; width: 400px;">
				<table border='1'>
					<tr id="player2_hand_masked" align='center'
						style='height: 75px; background-color: #FBBBB9'>
						<%
							for (int i = 0; i < hand_size; i++) {
						%>
						<td style="width: 75px;">?</td>
						<%
							}
						%>
					</tr>
				</table>
			</div>
		</div>
	</div>

	<div id="endgame"
		style="height: 390px; left: 30px; position: absolute; top: 195px; width: 900px; background-color: #F2F2F2; z-index: 3; overflow: scroll;">
		<h1>
			Round Over. <span id="winner">You won this hand! </span> <input
				id="holdem_button" type="submit" value="OK" />
		</h1>
		<div class="row">
			<div id="cv_results_1"
				style="left: 5px; position: absolute; top: 50px; width: 400px;">
				<h3>
					<a href="#">Your Predictor</a>
				</h3>
				<div id="player1_j48_score">
					<p style='height: 270px'></p>
				</div>
			</div>
			<div id="cv_results_2"
				style="left: 455px; position: absolute; top: 50px; width: 400px;">
				<h3>
					<a href="#">Barney's Predictor</a>
				</h3>
				<div id="player2_j48_score">
					<p style='height: 270px'></p>
				</div>
			</div>
		</div>

	</div>

	<div id="sound"></div>
</body>
</html>
<%} %>

