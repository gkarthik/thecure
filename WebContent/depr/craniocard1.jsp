<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%@ page import="org.scripps.combo.weka.Weka"%>
<%@ page import="org.scripps.combo.GameLog"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.ArrayList"%>

<% 
String username = (String)session.getAttribute("username");
if(username==null){
	username = "anonymous_hero";
	//touch to check if bitbucket migration worked
}
%>

<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html">


<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<meta name="description" content="A game">
<meta name="author" content="Ben">

<link href="assets/css/bootstrap.css" rel="stylesheet">
<style>
body {
	padding-top: 60px;
	/* 60px to make the container go all the way to the bottom of the topbar */
}
</style>
<link href="assets/css/bootstrap-responsive.css" rel="stylesheet">

<link rel="shortcut icon" href="../assets/ico/favicon.ico">
<link rel="apple-touch-icon-precomposed" sizes="114x114"
	href="assets/ico/apple-touch-icon-114-precomposed.png">
<link rel="apple-touch-icon-precomposed" sizes="72x72"
	href="assets/ico/apple-touch-icon-72-precomposed.png">
<link rel="apple-touch-icon-precomposed"
	href="assets/ico/apple-touch-icon-57-precomposed.png">


<title>Welcome to COMBO: game Craniosynostosis verse normal</title>

<%
	String ran = request.getParameter("ran");
	if (ran == null) {		
		ran = ""+(int)Math.rint(Math.random()*1000);
	}
	
	String dataset_name = request.getParameter("dataset_name");
	if(dataset_name==null){
		dataset_name = "cranio_case_control";
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
var nrows = 5;
var ncols = 5;
var seed = <%=ran%>;
var max_hand = 5;
var score = 0;
var par_score = 0;
var par = 0;
var p1_hand = new Array();
var p2_hand = new Array();
var player_name = "<%=username%>";
var features = "";

//playSound("sounds/ray_gun-Mike_Koenig-1169060422.wav");
function playSound(url) {
	document.getElementById("sound").innerHTML = "<embed src='"+url+"' hidden=true autostart=true loop=false>";
}

function evaluateHand(cardsinhand){
	var url = 'MetaServer?dataset=<%=dataset_name%>&command=getscore&features=';
	features = "";
	$.each(cardsinhand, function(index, value) {
	    features+=value.att_index+",";
	  });
	url +=features;
	
	//goes to server, runs the default evaluation with a decision tree
	$.getJSON(url, function(data) {
		var prev_score = score;
		score = data.accuracy;
		if(score > prev_score){
			playSound("sounds/human/MMMMM1.WAV");
		}else if(score < prev_score){
			playSound("sounds/human/SNORTAHH.WAV");
		}else{
			//playSound("sounds/human/AHH3.WAV");
		}
		par_score = score - par;
		$("#best_game_score").text(par_score);

	});
	

	
}

function getStyleByScore(power){
	var colors = ["#F8F0DF","#F7E6C1","#F7DB9B","#F5CC6C","#F5BE3F", "#F26B10"];
	var cellstyle = "width: 75px;";
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



function generateBoardCell(cardindex){
	var boardhtml = "";
	var displayname = cards[cardindex].name;
	var power = cards[cardindex].power;
	var cellstyle = getStyleByScore(power);
	if(displayname==null||displayname.length==0){
		displayname = cards[cardindex].att_name;
	}
	cards[cardindex].displayname = displayname;
	boardhtml+="<td style=\""+cellstyle+"\">";
	boardhtml+="<div class=\"feature_name\" id=\""+cards[cardindex].unique_id+"\">"+displayname+"</div>";
	boardhtml+="<div class=\"select_card_button\" id=\"card_index_"+cardindex+"\"><img src=\"images/BlurMetalLc0.gif\" alt=\"select card\"></div></td>";
	
	return boardhtml;
}

function generateHandCell(cardindex){
	var boardhtml = "";
	var displayname = cards[cardindex].name;
	var power = cards[cardindex].power;
	var cellstyle = getStyleByScore(power);
	if(displayname==null||displayname.length==0){
		displayname = cards[cardindex].att_name;
	}
	cards[cardindex].displayname = displayname;
	boardhtml+="<td style=\""+cellstyle+"\">";
	boardhtml+="<div class=\"feature_name\" id=\""+cards[cardindex].unique_id+"\">"+displayname+"</div>";
	boardhtml+="<div class=\"cardsinhand\" id=\"p1_c_"+cardindex+"\"><img style=\"background-color:#98AFC7;\" src=\"images/BlurMetalDb3.gif\"></div></td>";
	
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

function generateBoard(){
	var cardindex = -1;
	var boardhtml ="<table border=\"1\">";
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
	var url = 'MetaServer?dataset=<%=dataset_name%>&command=getscore&features=';
	var features = "";
	$.each(cards, function(index, value) {
	    features+=value.att_index+",";
	  });
	url +=features;
	
	$.getJSON(url, function(data) {
		par = data.accuracy;
		$("#par_score").html("<strong>"+par+"</strong>");
	});
}


function showgene(geneid, name){
    var gene_url = 'http://mygene.info/gene/'+geneid+'?filter=name,symbol,summary,go,genomic_pos&jsoncallback=?';
//    show_loading("#infobox");
    if(geneid!="_"&&geneid!=""){
    	$.getJSON(gene_url, mygene_info_get_gene_callback);
    }else{
    	$("#infobox").empty();
    	$("#infobox").append('<strong>'+name+'</strong><h1>Mystery Card!</h1><p>No data available</p>');
    }
}

function mygene_info_get_gene_callback(result){
    $("#infobox").empty();    
    if (result && result.name && result.symbol){
    	var chromosome = "";
    	if(result.genomic_pos){
    		chromosome = result.genomic_pos.chr;
    	}
    	$("#infobox").append('<strong>'+result.symbol+' : '+result.name+' (chr '+chromosome+ ')</strong><p>'+result.summary+'</p>');
    	
    	if(result.go.BP){
    		var ps_list = result.go.BP;
        	if (!$.isArray(ps_list)){
            	ps_list = [ps_list];
       		 }        
        	$("#infobox").append("<p><strong>Biological Processes</strong><br>");
       		$.each(ps_list, function(i, ps){
           		 $("#infobox").append(ps.term+', ');
        	});
       		$("#infobox").append("</p>");   	
    	}
    	if(result.go.CC){
    		var ps_list = result.go.CC;
        	if (!$.isArray(ps_list)){
            	ps_list = [ps_list];
       		 }        
        	$("#infobox").append("<p><strong>Cellular Component</strong><br>");
       		$.each(ps_list, function(i, ps){
           		 $("#infobox").append(ps.term+', ');
        	});
       		$("#infobox").append("</p>");   	
    	}
    	if(result.go.MF){
    		var ps_list = result.go.MF;
        	if (!$.isArray(ps_list)){
            	ps_list = [ps_list];
       		 }        
        	$("#infobox").append("<p><strong>Molecular Functions</strong><br>");
       		$.each(ps_list, function(i, ps){
           		 $("#infobox").append(ps.term+', ');
        	});
       		$("#infobox").append("</p>");   	
    	}
    }
    else {
        $("#infobox").append('<p>No data available for this gene.</p>');
    }    
}

function setupShowInfoHandler(){
	$(".feature_name").on("click", function () {
		var cell_id = this.id;
		var name = this.innerText;
		showgene(cell_id, name);
	  });
}

function setupHoldem(){
	//add the save handler
	 $("#holdem_button").on("click", function () {				
			var saveurl = 'MetaServer?dataset=<%=dataset_name%>&command=savehand&features='+features+'&player_name='+player_name+'&score='+par_score+'&cv_accuracy='+score+'&board_id='+seed;
			//player_name , score, cv_accuracy, board_id
			console.log("saved "+saveurl);
			$.getJSON(saveurl, function(data) {
				//and go to the next board
				window.location.reload(true);
			});
	});	
}

function setupHandAddRemove(){
	$(".select_card_button").on("click", function () {
		if(p1_hand){
			hand_size = p1_hand.length;
		}else{
			hand_size = 0;
		}
		if(hand_size < max_hand){
			var cell_id = this.id.replace("card_index_", "");	
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
			$(this).parent().css('background-color', '#98AFC7');
		//add the take out of hand handler
			$(".cardsinhand").on("click", function () {
				var cell_id = this.id.replace("p1_c_", "");	
				//put it back on the board
				var table_cell_id = "#card_index_"+cell_id;
				var cellcontents = generateUsedBoardCell(cell_id);
				//$(handcell).hide().appendTo("#player1_hand").fadeIn(1000);
				
				
				$(table_cell_id).parent().html(cellcontents);
				//take it out of our hand representation
				var tmp = new Array();
				var tmp_index = 0;
				for (var r = 0; r < p1_hand.length; r++) {
					if(p1_hand[r]!=cards[cell_id]){
						tmp[tmp_index] = p1_hand[r];
						tmp_index++;
					}
				}
				p1_hand = tmp;
				//re-evaluate
				if(p1_hand!=null&&p1_hand.length>0){
					evaluateHand(p1_hand);
				}
				//remove button from hand
				$(this).parent().remove(); //fadeOut(1000);
			  });
		
		}else{
			alert("Sorry, you can only have 5 cards in your hand in this game.  Click a card to remove it from your hand."); 
		}
	  });
}

$(document).ready(function() {
	//set up accordian for result viewer
	 $("#cv_results").accordion({ collapsible: true });
	//set up the baord
	url = "MetaServer?dataset=<%=dataset_name%>&command=getboard&x="+ncols+"&y="+nrows+"&ran="+seed;
	//data will contain the array of cards used to build the board for this game
	$.getJSON(url, function(data) {
		cards = data;
		generateBoard();
		
		//set up handlers
		// info box
		setupShowInfoHandler();
		//add to hand
		setupHandAddRemove();
		//save hand
		setupHoldem();
		
	});			

	
	//show default empty results
	//$("#cv_results").accordion( "activate" , 1 );
 
});
</script>
</head>
<body>

<div id="header" style="text-align: right; height: 20px; left: 15px; position: absolute; top: 5px; width: 830px; margin:1px 1px 1px 1px; padding:1px 1px 1px 5px; background-color:#b0c4de">
<%=username%> is currently playing <a href="index.jsp">logout</a>.  
</div>

	<div id="Instructions"
		style="height: 100px; left: 15px; position: absolute; top: 30px; width: 420px;"">
		<h3>Instructions</h3>
		<p>
			Click the buttons <img src="images/BlurMetalLc0.gif"/> to pick genes to put in your hand.  Try it..<br/>
			Click a gene name to see more information about it.<br/>  
			Maximize your score by selecting groups of genes whose expression may correlate with breast cancer prognosis.<a target="_blank" href="genecard1_inst.jsp">(more info)</a>
		</p>
	</div>

	<div>
		<div id="board"
			style="height: 500px; left: 15px; position: absolute; top: 150px; width: 500px;">

		</div>
	</div>

	<div id="best_game_score_box" style="text-align: center; left: 550px; position: absolute; top: 300px; width: 200px; z-index:2;">
		<h4>Score for hand</h4>
		<h1 id="best_game_score" style="text-align: center;">0</h1>
			<input id="holdem_button" type="submit" value="Holdem!" /> 
	</div>
	
	<div id="player1_title_area" style="left: 450px; position: absolute; top: 120px; width: 500px;">
	<h3>Your hand of gene cards</h3>
	</div>
	
	<div id="player1" style="left: 450px; position: absolute; top: 100px; width: 500px;">
		<div id="hand_info_box" style="position: relative; top: 45px; width: 500px;">
			<div id="player_box" style="position: relative; top: 15px; width: 400px;">
			<table border='1'>
				<tr id="player1_hand" align='center' style='height: 75px;'>

				</tr>
			</table>
			</div>
		</div>
	</div>

	<div id="player1_masked" style="left: 450px; position: absolute; top: 100px; width: 500px; z-index:-1">
		<div id="hand_info_box_masked" style="position: relative; top: 45px; width: 500px;">
			<div id="player_box_masked" style="position: relative; top: 15px; width: 400px;">
			<table border='1'>
				<tr id="player1_hand_masked" align='center' style='height: 75px;'>
					<td style="width: 75px;">?</td>
					<td style="width: 75px;">?</td>
					<td style="width: 75px;">?</td>
					<td style="width: 75px;">?</td>
					<td style="width: 75px;">?</td>
				</tr>
			</table>
			</div>
		</div>
	</div>
	
	<div id="scoreboard" style="left: 550px; position: absolute; top: 30px; width: 300px; z-index:-1">
		<table>
			<caption><b><u>Score for board (<%=ran%>)</u></b></caption>
				<thead>
					<tr>
						<th>Best score</th>
						<th>Avg. score</th>
					</tr>
				</thead>
				<tbody>
					<%
					GameLog log = new GameLog();
					GameLog.high_score sb = log.getScoreBoard();	
					Integer board_id = Integer.parseInt(ran);
					Integer max = 0;
					int attempts = 0;
					 if(sb.getBoard_max()!=null&&sb.getBoard_max().get(board_id)!=null){
						 max = sb.getBoard_max().get(board_id);
					 }
					Float avg = new Float(0);
					if(sb.getBoard_avg()!=null&&sb.getBoard_avg().get(board_id)!=null){
						 avg = sb.getBoard_avg().get(board_id);
					 }
					
						%>
						<tr align="center">
						<td><%=max %></td>
						<td><%=avg %></td>
						</tr>

				</tbody>
			</table>
	</div>	
<!--		
		<div style="text-align: center; position: relative; top: 30px;">
			<strong>Score</strong>
		</div>

	<div id="cv_results">
			<h3>
				<a href="#">Decision Tree</a>
			</h3>
			<div id="player1_j48_score">
				<p style='height: 270px'> </p>
			</div>
			<h3>
				<a href="#">Ripper Rules</a>
			</h3>
			<div id="player1_jrip_score">
				<p style='height: 270px'> </p>
			</div>
		</div>
  -->			
	</div>

	<div id="infobox"
		style="height: 500px; left: 15px; position: absolute; top: 530px; width: 840px;">
		<strong>Click on a gene name for a clue</strong>
	</div>
	
	<div id="sound"></div>
</body>
</html>


