<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%@ page import="org.scripps.combo.weka.Weka"%>
<%@ page import="org.scripps.combo.GameLog"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.ArrayList"%>

<% 
String full_request = request.getRequestURI()+"?"+request.getQueryString();
String mosaic_url = request.getParameter("mosaic_url"); //"cranio_coronal_mosaic.jsp";
String dataset = request.getParameter("dataset"); // "coronal_case_control";
String title = request.getParameter("title"); // "Craniostenososis - coronal verse control";
String nrows = request.getParameter("nrows"); // "5";
String ncols = request.getParameter("ncols"); // "5";
String max_hand = request.getParameter("max_hand"); // "5";
String showgeneinfo = "1";
if(request.getParameter("geneinfo")!=null){
	showgeneinfo = request.getParameter("geneinfo");
}

String username = (String)session.getAttribute("username");
String level = request.getParameter("level");
int ilevel = 0;
if(level!=null){
	ilevel = Integer.parseInt(level);
}
int display_level = ilevel+1;
if(username==null){
	username = "anonymous_hero";
}
GameLog glog = new GameLog();
Integer multiplier = glog.getPheno_multiplier().get(dataset);

%>

<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html">


<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<meta name="description" content="A game">
<meta name="author" content="Ben">

<link rel="stylesheet" href="assets/css/barney.css"
	type="text/css" media="screen">
<link rel="stylesheet" href="assets/css/combo_bootstrap.css"
	type="text/css" media="screen">
<link rel="stylesheet" href="assets/css/combo.css" type="text/css"
	media="screen">
	
<link
	href="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8/themes/base/jquery-ui.css"
	rel="stylesheet" type="text/css" />
		
<style>
body {
	padding-top: 60px;
	/* 60px to make the container go all the way to the bottom of the topbar */
}
</style>



<title>COMBO: <%=title %></title>

<%
	String ran = request.getParameter("level");
	if (ran == null) {		
		ran = ""+(int)Math.rint(Math.random()*1000);
	}
%>

<script src="js/libs/d3.v2.min.js"></script>
<script src="js/libs/underscore-min.js"></script>
<script src="js/libs/jquery-1.8.0.min.js"></script>

<script src="js/libs/jquery-ui-1.8.0.min.js"></script>

<script src="js/libs/jquery.sparkline.min.js"></script>
<script src="js/tree.js"></script>

<script>	
var cards = new Array();
var opponent_sort = new Array();
var nrows = <%=nrows%>;
var ncols = <%=ncols%>;
var level = <%=level%>;
var showgeneinfo = <%=showgeneinfo%>;
var max_hand = <%=max_hand%>;
var dataset = "<%=dataset%>";
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
var board_state_clickable = true;
var replay = "<%=full_request%>";
var multiplier = "<%=multiplier%>";

/* Move Barney! */

function moveBarney(moveChoice) {
    $("#barney5").removeClass().hide().addClass(moveChoice).show();
}
function moveClayton(moveChoice) {
    $("#clayton1").removeClass().hide().addClass(moveChoice).show();
}

//playSound("sounds/ray_gun-Mike_Koenig-1169060422.wav");
function playSound(url) {
	document.getElementById("sound").innerHTML = "<embed src='"+url+"' hidden=true autostart=true loop=false>";
}

function evaluateHand(cardsinhand, player){
	var url = 'MetaServer?dataset=<%=dataset%>&command=getscore&features=';
	features = "";
	feature_names = "";
	var chand = "cardsinhand_"+player;

	$.each(cardsinhand, function(index, value) {
	    features+=value.att_index+",";
		feature_names += value.att_name + ":"+value.name+"|";
	  });
	url +=features;

	//goes to server, runs the default evaluation with a decision tree
 	$.getJSON(url, function(data) {
 		var treeheight = 250;
 		var treewidth = 420;
 		//console.log(data.max_depth +" depth");
 		if(data.max_depth>6){
 			treeheight = 500;
 		}
 		if(player=="1"){
 			//draw the current tree
 			$("#p1_current_tree").empty();
 			drawTree(data, treewidth, treeheight, "#p1_current_tree");
 			//var prev_score = p1_score;
 			p1_score = data.evaluation.accuracy;
 			if(p1_hand.length==max_hand){
 				$("#player1_j48_score").html('<strong> score '+data.evaluation.accuracy+'</strong>');
 			}
			$("#game_score_1").text(p1_score);
 		}else if(player=="2"){
 			//draw the current tree
 			$("#p2_current_tree").empty();
 			drawTree(data, treewidth, treeheight, "#p2_current_tree");
 			//var prev_score = p2_score;
 			$("#player2_j48_score").html('<strong> score '+data.evaluation.accuracy+'</strong>');
 			p2_score = data.evaluation.accuracy;
 			if(p2_hand.length==max_hand){
 				$("#player2_j48_score").html('<strong> score '+data.evaluation.accuracy+'</strong>');
 			}
 			$("#game_score_2").text(p2_score);
 			board_state_clickable = true;
 			if(p2_hand.length!=max_hand&&p1_score<p2_score&&p1_score>0){
 				moveBarney("correct"); //incorrect win lose
			}else if (p1_score>p2_score){
				moveClayton("correct"); //incorrect win lose
			}
 			
 		}
 		//if its the last hand, show the results
 		if(p2_hand.length==max_hand){ 
 			window.setTimeout(function() {
 				var $tabs = $("#tabs").tabs();		
 	 			if(p1_score<p2_score&&p1_score>0){
 					$("#winner").text("Sorry, you lost this hand. ");
 	 				$tabs.tabs('select', 4); 
 	 				moveBarney("win"); //incorrect win lose
 	 	 			$("#winner").append("<br><a href=\""+replay+"\">Play Level Again?</a>");
 				}else if (p1_score>p2_score){
 					$("#winner").parent().parent().css('background-color', '#FFA500');
 					$("#winner").html("<h1>You beat Barney!</h1>You earned "+p1_score+" * "+multiplier+"= </strong><span style=\"font-size:30px;\">"+(p1_score*multiplier)+"</span> points!");
 					$tabs.tabs('select', 3); 
 	 				moveBarney("lose"); //incorrect win lose
 	 				moveClayton("win");
 				}else if (p1_score==p2_score){
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
}

function getStyleByScore(power){
	var colors = ["#F8F0DF","#F7E6C1","#F7DB9B","#F5CC6C","#F5BE3F", "#F26B10"];
	var cellstyle = "width: 75px; position:relative; ";
	//color by estimated individual predictive power
	if(power==0){
		//cellstyle+=" background-color:"+colors[0]+";";
	}else if(power>0&&power<0.12){
		cellstyle+=" background-color:"+colors[0]+";";
	}else if(power<0.13){
		cellstyle+=" background-color:"+colors[1]+";";
	}else if(power<0.14){
		cellstyle+=" background-color:"+colors[2]+";";
	}else if(power<0.15){
		cellstyle+=" background-color:"+colors[3]+";";
	}else if(power<0.16){
		cellstyle+=" background-color:"+colors[4]+";";
	}else if(power>0.15){
		cellstyle+=" background-color:"+colors[5]+";";
	}
	return cellstyle;
}

function getChessStyle(cell_index){
	var cellstyle = "width: 75px; position:relative; ";
//	if(cell_index % 2 == 0){
//		cellstyle+=" background-color:#F26B10;";
//	}else{
		cellstyle+=" background-color:#F8F0DF;";
//	}
	return cellstyle;
}


function generateBoardCell(cardindex){
	var boardhtml = "";
	var displayname = cards[cardindex].name;
	var power = cards[cardindex].power;
	var cellstyle = getChessStyle(cardindex);
	if(displayname==null||displayname.length==0){
		displayname = cards[cardindex].att_name;
	}
	cards[cardindex].displayname = displayname;
	boardhtml+="<td style=\""+cellstyle+"\">";
	if(showgeneinfo=="1"){
		boardhtml+="<div class=\"feature_name\" id=\""+cards[cardindex].unique_id+"\" style=\"position:absolute; top:4; right:0;\"><a href=\"#\"><img src=\"images/info-icon.png\"></a></div>";
	}
//	boardhtml+="<div class=\"select_card_button\" id=\"card_index_"+cardindex+"\"><a title=\"add to hand\" class=\"selectable\" style=\"color:black;\" href=\"#\">"+displayname+"</a></div></td>";
	boardhtml+="<div class=\"select_card_button btn btn-primary\" id=\"card_index_"+cardindex+"\"><a title=\"add to hand\" class=\"selectable small_level_button\" style=\"height:60px; line-height:60px; text-align:center; font-size:13; width:60px; text-decoration:none;\" href=\"#\">"+displayname+"</a></div></td>";
	
	return boardhtml;
}

/**
 * cardindex refers to the location on the board and the handle in the board card Map
 */
function generateHandCell(cardindex, player){
	var boardhtml = "";
	var displayname = cards[cardindex].name;
	var power = cards[cardindex].power;
	var cellstyle = getStyleByScore(power);
	if(displayname==null||displayname.length==0){
		displayname = cards[cardindex].att_name;
	}
	cards[cardindex].displayname = displayname;
	boardhtml+="<td style=\""+cellstyle+"\">";
/* 	if(showgeneinfo=="1"){
		boardhtml+="<div class=\"feature_name\" id=\""+cards[cardindex].unique_id+"\" style=\"position:absolute; top:0; right:0;\"><a href=\"#\"><img src=\"images/info-icon.png\"></a></div>";
	}
	boardhtml+="<div class=\"select_card_button\" id=\"card_index_"+cardindex+"\">"+displayname+"</div></td>";
 */	
	if(showgeneinfo=="1"){
		boardhtml+="<div class=\"feature_name\" id=\""+cards[cardindex].unique_id+"\" style=\"position:absolute; top:4; right:0;\"><a href=\"#\"><img src=\"images/info-icon.png\"></a></div>";
	}
//	boardhtml+="<div class=\"select_card_button\" id=\"card_index_"+cardindex+"\"><a title=\"add to hand\" class=\"selectable\" style=\"color:black;\" href=\"#\">"+displayname+"</a></div></td>";
	boardhtml+="<div class=\"select_card_button btn btn-primary\" style=\"cursor:default;\" id=\"card_index_"+cardindex+"\"><span class=\"selectable small_level_button\" style=\"cursor:default; height:60px; line-height:60px; text-align:center; font-size:13; width:60px; text-decoration:none;\">"+displayname+"</span></div></td>";

	return boardhtml;
}

function generateUsedBoardCell(cardindex){

	var boardhtml = "";
	var displayname = cards[cardindex].name;
	var power = cards[cardindex].power;
	var cellstyle = getStyleByScore(power);
	if(displayname==null||displayname.length==0){
		displayname = cards[cardindex].att_name;
	}
	cards[cardindex].displayname = displayname;
	boardhtml+="<td style=\""+cellstyle+"\">";
	boardhtml+="<div class=\"feature_name\" id=\""+cards[cardindex].unique_id+"\">"+displayname+"<br/>Used..</div>";
	return boardhtml;
}


function setupOpponent(){
	opponent_sort = cards.slice(0);
	//maintain the indexes to the board
	$.each(opponent_sort, function(index, value) {
		opponent_sort[index].board_index = index;
	  });
	//rank by the power value - now ascending
	opponent_sort.sort(function(a, b){
		 return a.power - b.power;
		});
	//check
	//$.each(opponent_sort, function(index, value) {
	//    console.log(index+" "+value.power+" "+opponent_sort[index].board_index);
	//  });
}

function generateBoard(){
	var cardindex = -1;
	var boardhtml ="<table >"; //border=\"4\" bordercolor=\"orange\"
	for (var r = 0; r < nrows; r++) {
		boardhtml+="<tr align=\"center\" style=\"height: 75px\";>";
			for (var c = 0; c < ncols; c++) {
				cardindex++;
				var boardcell = generateBoardCell(cardindex);
				boardhtml+=boardcell;
			}
			boardhtml+="</tr>";	
		}
	boardhtml+="</table>";
	$("#board").empty(); 
	$("#board").append(boardhtml);
	
	//set the par - use all the features on the board
	var url = 'MetaServer?dataset=<%=dataset%>&command=getscore&features=';
	var features = "";
	$.each(cards, function(index, value) {
	    features+=value.att_index+",";
	  });
	url +=features;
	
	$.getJSON(url, function(data) {
		par = data.evaluation.accuracy;
		$("#par_score").html("<strong>"+par+"</strong>");
	});
}


function showgene(geneid, name){
	$("#infobox_header").empty();
    var gene_url = 'http://mygene.info/gene/'+geneid+'?filter=name,symbol,summary,go,genomic_pos&jsoncallback=?';
//    show_loading("#infobox");
    if(geneid!="_"&&geneid!=""){
    	//mygene info
    	$.getJSON(gene_url, mygene_info_get_gene_callback);
    	//ncbi eutils 
    	args = {'apikey' : 'ba0b21611890b5bc23c8c57033001a47',
	        'db'     : 'gene',
	        'id'   : geneid};
		$.getJSON('http://entrezajax.appspot.com/efetch?callback=?', args, entrezajax_callback);
    }else{
    	$("#infobox").empty();
    	$("#infobox").append('<strong>'+name+'</strong><h1>Mystery Card!</h1><p>No data available</p>');
    }
}

function entrezajax_callback(data){
	$("#rifs").empty(); 
	$("#ncbi_phenos").empty();
	$.each(data.result, function(i, item) {
		var generif_list = '<ul>';
			var phenotypes = '<ul>';
			var haspheno = false;
			for(var i = 0; i < item.Entrezgene_comments.length; i ++) {
				if(item.Entrezgene_comments[i]["Gene-commentary_type"] == 18){
					var pmid_obj = item.Entrezgene_comments[i]["Gene-commentary_refs"];
					var pmid;
					if(pmid_obj){
						pmid = pmid_obj[0].Pub_pmid.PubMedId;
					}
					var riftext = item.Entrezgene_comments[i]["Gene-commentary_text"];
					if(riftext){
						generif_list += '<li><a target=\"blank\" href=\'http://www.ncbi.nlm.nih.gov/pubmed/' + pmid + '\'>' + riftext + '</a></li>';
					}
				}else if(item.Entrezgene_comments[i]["Gene-commentary_type"] == 254){
					var pheno_obj = item.Entrezgene_comments[i]["Gene-commentary_comment"];
					if(pheno_obj){
						for(var p=0; p<pheno_obj.length; p++){
							if(pheno_obj[p]["Gene-commentary_type"]==19){
								phenotypes += '<li>'+pheno_obj[p]["Gene-commentary_heading"]+"</li>";
								haspheno = true;
							}
						}
					}
				}
			}
			generif_list += '</ul>'; phenotypes += '</ul>';
			$("#rifs").append(generif_list);
			if(haspheno){
				$("#gene_description").append("<div id=\"ncbi_phenos\">Phenotypes<br>"+phenotypes+"</div>");
			}
		});
}

function mygene_info_get_gene_callback(result){
    $("#gene_description").empty();    
    $("#ontology").empty();
	$("#ncbi_phenos").empty();
    if (result && result.name && result.symbol){
    	var chromosome = "";
    	if(result.genomic_pos){
    		chromosome = result.genomic_pos.chr;
    	}
    	$("#gene_description").append('<a href=\"http://www.ncbi.nlm.nih.gov/gene/'+result._id+'\" target = "blank">'+result.symbol+' : '+result.name+' (chr '+chromosome+ ')</a><p>'+result.summary+'</p>');
    	
    	if(result.go){
    	if(result.go.BP){
    		var ps_list = result.go.BP;
        	if (!$.isArray(ps_list)){
            	ps_list = [ps_list];
       		 }        
        	$("#ontology").append("<p><strong>Biological Processes</strong><br>");
       		$.each(ps_list, function(i, ps){
           		 $("#ontology").append(ps.term+', ');
        	});
       		$("#ontology").append("</p>");   	
    	}
    	if(result.go.CC){
    		var ps_list = result.go.CC;
        	if (!$.isArray(ps_list)){
            	ps_list = [ps_list];
       		 }        
        	$("#ontology").append("<p><strong>Cellular Component</strong><br>");
       		$.each(ps_list, function(i, ps){
           		 $("#ontology").append(ps.term+', ');
        	});
       		$("#ontology").append("</p>");   	
    	}
    	if(result.go.MF){
    		var ps_list = result.go.MF;
        	if (!$.isArray(ps_list)){
            	ps_list = [ps_list];
       		 }        
        	$("#ontology").append("<p><strong>Molecular Functions</strong><br>");
       		$.each(ps_list, function(i, ps){
           		 $("#ontology").append(ps.term+', ');
        	});
       		$("#ontology").append("</p>");   	
    	}
    	}
    }
    else {
        $("#gene_description").append('<p>No description available for this gene.</p>');
    }    
}

function setupShowInfoHandler(){
	//console.log("setting showInfoHandlers");
	var $tabs = $("#tabs").tabs();
	//reset and rebind (#todo - this unbind hack is here because of the way cards are added to hands, should improve that so we don't hav to rebind very element on the baord..)
	$(".feature_name").unbind("click");
	$(".feature_name").on("click", function () {
		var cell_id = this.id;
		var name = this.innerText;
		showgene(cell_id, name);
//		$tabs.tabs('select', 0); //it always goes back to the gene description on an info request
	  });
}

function saveHand(){
	par_score = p1_score - par;
	 var win = "0";
	 if(p1_score > p2_score){
		 win = "1";
	 }
		var saveurl = 'MetaServer?dataset=<%=dataset%>&command=savehand&features='
			+ features + '&player_name=' + player_name + '&score='
			+ par_score + '&cv_accuracy=' + p1_score + '&board_id=' + level
			+ "&win=" + win+'&game=verse_barney&feature_names=' + feature_names;
		
		//player_name , score, cv_accuracy, board_id
		//console.log("saved "+saveurl);
		$.getJSON(saveurl, function(data) {
			window.location.replace("<%=mosaic_url%>");
		});
}

function setupHoldem(){
	//add the save handler
	 $("#holdem_button").on("click", function () {				
		 saveHand();
	});	
}

//function to get random number upto m
function randomXToY(minVal,maxVal,floatVal)
{
  var randVal = minVal+(Math.random()*(maxVal-minVal));
  return typeof floatVal=='undefined'?Math.round(randVal):randVal.toFixed(floatVal);
}

function getBarneysNextCardGettingHarder(){
	var lower = level;
	if((lower+10)>=cards.length){
		lower = cards.length - 11;
	}
	var upper = lower+10;
	
	sorted_index = randomXToY(lower,upper);
	//console.log("sorted index "+sorted_index);
	card_index = opponent_sort[sorted_index].board_index+"";

	if((($.inArray(card_index, p1_indexes)==-1)&&($.inArray(card_index, p2_indexes)==-1))){
		//console.log(p1_indexes);
		//console.log(p2_indexes);//
		//console.log(" returned "+card_index);
		return card_index;
	}else{
		//console.log(" iterated on "+card_index);
		return getBarneysNextCard();
	}
}

function getBarneysNextCard(){
	/* var lower = level;
	if((lower+10)>=cards.length){
		lower = cards.length - 11;
	}
	var upper = lower+10; 
	
	sorted_index = randomXToY(lower,upper); */
	sorted_index = randomXToY(0, cards.length - 1);
	//console.log("sorted index "+sorted_index);
	card_index = opponent_sort[sorted_index].board_index+"";

	if((($.inArray(card_index, p1_indexes)==-1)&&($.inArray(card_index, p2_indexes)==-1))){
		//console.log(p1_indexes);
		//console.log(p2_indexes);//
		//console.log(" returned "+card_index);
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

$(document).ready(function() {
	//moveBarney("correct"); //incorrect win lose
	//moveBarney("incorrect");
	//moveBarney("correct");

	//set up accordian for result viewer
	// $("#cv_results").accordion({ collapsible: true });
	//hide the end button
	$("#endgame").hide();
	//set up the board
	if (level != null&&dataset=="mammal") {
		url = "MetaServer?dataset=mammal&command=getspecificboard&x="
									+ ncols
									+ "&y="
									+ nrows
									+ "&board=mammal_"
									+ level;
	} else {
	    url = "MetaServer?dataset=<%=dataset%>&command=getboard&x="+ncols+"&y="+nrows+"&ran="+level;
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
						<li><a href="logout.jsp">logout</a>
						</li>
					</ul>
				</div>
				<!--/.nav-collapse -->
			</div>
		</div>
	</div>

	<div>
		<%
		if(nrows.equals("1")){ 
		%>
		<div style="height: 200px; left: 150px; position: absolute; top: 265px; width: 200px;">
			<h2>Pick a feature</h2>
		</div>
		<div id="board" style="height: 200px; left: 150px; position: absolute; top: 300px; width: 200px;">
		</div>
		<%}else if(nrows.equals("2")){ 
		%>
		<div style="height: 200px; left: 130px; position: absolute; top: 215px; width: 200px;">
			<h2>Pick a feature</h2>
		</div>
			<div id="board"
				style="height: 300px; left: 130px; position: absolute; top: 250px; width: 300px;">
			</div>
		<%} 
		else if(nrows.equals("3")){ 
		%>
		<div style="height: 200px; left: 130px; position: absolute; top: 215px; width: 200px;">
			<h2>Pick a feature</h2>
		</div>
			<div id="board"
				style="height: 300px; left: 100px; position: absolute; top: 250px; width: 300px;">
			</div>
		<%} else{ 
		%>
		<div id="board"
			style="height: 500px; left: 30px; position: absolute; top: 180px; width: 500px;">
		</div>
		<%} %>
		
	</div>


	<div id="game_score_box_1"
		style="text-align: center; left: 460px; position: absolute; top: 600px; width: 350px; z-index: 2;">
		<img style="left:0px; top:0px; position:absolute;" id="clayton1" width="100px" src="images/200px-Clayton.png"/>
		<h4>Your score</h4>
		<h1 id="game_score_1" style="text-align: center;">0</h1>
	</div>

	<div id="player1_title_area"
		style="left: 30px; position: absolute; top: 560px;">
		<h3>Your hand</h3>
	</div>

	<div id="player1"
		style="height: 500px; left: 30px; position: absolute; top: 535px;">
		<div id="hand_info_box_1"
			style="position: relative; top: 45px; width: 500px;">
			<div id="player_box_1"
				style="position: relative; top: 15px; width: 400px;">
				<table border='1'>
					<tr id="player1_hand" align='center' style='height: 75px; background-color:#82CAFA;'>

					</tr>
				</table>
			</div>
		</div>
	</div>


	<div id="game_score_box_2"
		style="text-align: center; left: 460px; position: absolute; top: 75px; width: 350px; z-index: 2;">
		<img style="left:0px; top:0px; position:absolute;" id="barney5" src="images/barney.png"/>		
		<div>
		<strong style="text-align: center; font-size:20px;">Barney's score</strong><br/><br/>
		<strong id="game_score_2" style="text-align: center; font-size:30px;">0</strong>
		</div>
	</div>

	<div id="game_meta_info"
		style="text-align: center; left: 750px; position: absolute; top: 55px; z-index: 2;">
		<strong>Dataset: <%=dataset %></strong><br/><strong>Point Multiplier: <%=multiplier %></strong>
	</div>

	<div id="player2_title_area"
		style="left: 30px; position: absolute; top: 50px;">
		<h3>Barney's hand</h3>
	</div>

	<div id="player2" style="left: 30px; position: absolute; top: 25px;">
		<div id="hand_info_box_2"
			style="position: relative; top: 45px; width: 500px;">
			<div id="player_box_2"
				style="position: relative; top: 15px; width: 400px;">
				<table border='1'>
					<tr id="player2_hand" align='center' style='height: 75px; background-color:#FBBBB9;'>

					</tr>
				</table>
			</div>
		</div>
	</div>

<div id="infobox"
		style="height: 375px; left: 460px; position: absolute; top: 170px; width: 500px; overflow: scroll; padding:10;">
		<div id="infobox_header"><strong>Click on a <img src="images/info-icon.png"> for clues </strong></div>
		<div id="tabs">
	<ul>
	<%if(!(dataset.equals("mammal")||dataset.equals("zoo"))){ %>
		<li><a href="#gene_description">Gene</a></li>
		<li><a href="#ontology">Ontology</a></li>
		<li><a href="#rifs">Rifs</a></li>
	<%} %>	
		<li><a href="#p1_current_tree">Yours</a></li>
		<li><a href="#p2_current_tree">Barney's</a></li>
	</ul>
	<%if(!(dataset.equals("mammal")||dataset.equals("zoo"))){ %>
	<div id="gene_description">
		<p>Gene description</p>
		
	</div>
	<div id="ontology">
		<p>Gene Ontology terms</p>
	</div>
	<div id="rifs">
		<p>Gene References into Function</p>
	</div>
		<%} %>	
	<div id="p1_current_tree" style="left: 5px; position: absolute; top: 50px; width: 400px;">
		<p>Your decision tree will be displayed here.</p>
	</div>
	<div id="p2_current_tree" style="left: 5px; position: absolute; top: 50px; width: 400px;">
		<p>Barney's decision tree will be displayed here.</p>
	</div>
</div>	
	</div>

	<div id="endgame"
		style="height: 410px; left: 30px; position: absolute; top: 175px; width: 400px; background-color: #F2F2F2; z-index:3; overflow: scroll;">
		<div  style="margin-top:10px; margin-left:10px;">
		<h1>Round Over</h1>
		<div id="winner"  ></div> 
		<br><input id="holdem_button" type="submit" value="Try another board" />
		</div> 
		<!-- <div class="row">
		<div id="cv_results_1" 
		style="left: 5px; position: absolute; top: 50px; width: 400px;">
			<h3>
				<a href="#">Your Predictor</a>
			</h3>
			<div id="player1_j48_score">
				<p style='height: 270px'> </p>
			</div>
		</div>
		<div id="cv_results_2"
			style="left: 455px; position: absolute; top: 50px; width: 400px;">
			<h3>
				<a href="#">Barney's Predictor</a>
			</h3>
			<div id="player2_j48_score">
				<p style='height: 270px'> </p>
			</div>
		</div>
		</div> -->
	
	</div>
	
	<div id="sound"></div>

</body>
</html>


