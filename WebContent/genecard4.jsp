<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%@ page import="org.scripps.combo.weka.Weka"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.ArrayList"%>

<jsp:declaration>//private Weka weka;

	public void jspInit() {
		//		weka = new Weka();
	}</jsp:declaration>

<%
	/*int nrows = 5;
	 int ncols = 5;
	 List<Weka.card> cards = weka.getRandomCards(nrows * ncols); */
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


<title>Welcome to COMBO: game Breast Cancer prognosis</title>

<%
	String ran = request.getParameter("ran");
	if (ran == null) {
		ran = "0";
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
var hand_size = 0;
var max_hand = 5;
var score = 0;
var p1_hand = new Array();
var p2_hand = new Array();

function evaluateHand(cardsinhand){
	console.log(cardsinhand);
	var url = 'WekaServer?command=getscore&features=';
	var features = "";
	$.each(cardsinhand, function(index, value) {
	    features+=value.att_index+",";
	    console.log(value.att_name);
	  });
	url +=features;
	console.log(url);
	
	//goes to server, runs the default evaluation with a decision tree
	$.getJSON(url, function(data) {
		$("#player1_j48_score").html('<strong> score </strong><span style="background-color:#FF9933;>">'+data.accuracy+'</span><p><pre>'+data.modelrep+'</pre></p>');
		score = data.accuracy;
		//run the evlauation with ripper
		url+="&wekamodel=jrip";
		$.getJSON(url, function(data) {
			$("#player1_jrip_score").html('<strong> score </strong><span style="background-color:#FF9933;>">'+data.accuracy+'</span><p><pre>'+data.modelrep+'</pre></p>');
			if(data.accuracy>score){
				score = data.accuracy;
			}
			$("#best_game_score").html("<strong>"+score+"</strong>");
		});	
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
	boardhtml+="<div class=\"select_card_button\" id=\"card_index_"+cardindex+"\"><img src=\"images/GoDe0.gif\"></div></td>";
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
}


function showgene(geneid, name){
    var gene_url = 'http://mygene.info/gene/'+geneid+'?filter=name,symbol,summary,go,genomic_pos&jsoncallback=?';
    console.log(gene_url);
//    show_loading("#infobox");
    if(geneid!="_"&&geneid!=""){
    	$.getJSON(gene_url, mygene_info_get_gene_callback);
    }else{
    	$("#infobox").empty();
    	$("#infobox").append('<strong>'+name+'</strong><h1>Mystery Card!</h1><p>No data available</p>');
    }
}

function mygene_info_get_gene_callback(result){
	console.log(result);
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


function setupHandAddRemove(){
	$(".select_card_button").on("click", function () {
		if(hand_size < max_hand){
			hand_size++;
			var cell_id = this.id.replace("card_index_", "");	
			var cstyle = getStyleByScore(cards[cell_id].power);
			console.log("style "+cstyle+" "+cards[cell_id].power+" "+cards[cell_id]);
			$("#player1_hand").append('<td class=\'cardsinhand\' style=\''+cstyle+'\' id=\'p1_c_'+cell_id+'\'>'+cards[cell_id].displayname+'</td> ');
			p1_hand.push(cards[cell_id]);
			evaluateHand(p1_hand);
			//hide button from board
			$(this).hide();
			//change background color
			$(this).parent().css('background-color', '#98AFC7');
		//add the take out of hand handler
			$(".cardsinhand").on("click", function () {
				hand_size--;
				var cell_id = this.id.replace("p1_c_", "");	
				//put it back on the board
				var table_cell_id = "#card_index_"+cell_id;
				var cellcontents = generateUsedBoardCell(cell_id);
				console.log("cell contents "+cellcontents+" cell id "+cell_id);
				$(table_cell_id).parent().html(cellcontents);
				//take it out of our hand representation
				var tmp = new Array();
				console.log("hand "+p1_hand);
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
				$(this).remove();
			  });
		
		}else{
			alert("Sorry, you can only have 5 cards in your hand in this game.  Click a card to remove it from your hand"); 
		}
	  });
}

$(document).ready(function() {
	//set up accordian for result viewer
	 $("#cv_results").accordion({ collapsible: true });
	//set up the baord
	url = "WekaServer?command=getboard&x="+ncols+"&y="+nrows+"&ran="+seed;
	//data will contain the array of cards used to build the board for this game
	$.getJSON(url, function(data) {
		cards = data;
		generateBoard();
		
		//set up handlers
		// info box
		setupShowInfoHandler();
		//add to hand
		setupHandAddRemove();
		});
	//show default empty results
	$("#cv_results").accordion( "activate" , 1 );
});
</script>
</head>
<body>

	<div id="phenotype_description"
		style="height: 100px; left: 15px; position: absolute; top: 15px; width: 420px;"">
		<p>
			<strong>Sample source:</strong> primary breast tumours of young
			patients<br /> <strong>Predict:</strong> short interval to distant
			metastases ('poor prognosis' signature) in patients without tumour
			cells in local lymph nodes at diagnosis (lymph node negative).<br />
			<strong>Hint:</strong> genes regulating cell cycle, invasion,
			metastasis and angiogenesis may be important
		</p>
	</div>

	<div>
		<div id="board"
			style="height: 500px; left: 15px; position: absolute; top: 120px; width: 500px;">

		</div>
	</div>

	<div id="best_game_score_box" style="left: 850px; position: absolute; top: 15px; width: 100px;">
	<h4>Score for hand</h4>
	<div id="best_game_score" style="text-align: center;">0</div>
	</div>
	<div id="player1"
		style="left: 450px; position: absolute; top: 15px; width: 500px;">
		<div id="player_box" style="position: relative; top: 15px;">
			<table border='1'>
				<tr id="player1_hand" align='center' style='height: 75px'>

				</tr>
			</table>
		</div>
		<div style="text-align: center; position: relative; top: 15px;">
			<strong>Score</strong>
		</div>

		<div id="cv_results">
			<h3>
				<a href="#">Decision Tree</a>
			</h3>
			<div id="player1_j48_score">
				<p style='height: 250px'> </p>
			</div>
			<h3>
				<a href="#">Ripper Rules</a>
			</h3>
			<div id="player1_jrip_score">
				<p style='height: 250px'> </p>
			</div>
		</div>
	</div>

	<div id="infobox"
		style="height: 500px; left: 15px; position: absolute; top: 500px; width: 450px;">
		<strong>Click on a gene name for a clue</strong>
	</div>
</body>
</html>


