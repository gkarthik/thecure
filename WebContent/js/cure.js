var CURE=CURE||{};CURE.load=function(){var a=window.location.href.split("/cure/")[1];if(a.indexOf("?")>0){a=a.substring(0,a.indexOf("?"))}CURE.page=a.replace(".jsp","");switch(CURE.page){case"about":break;case"contact":break;case"forgot":CURE.forgot.init();break;case"help":CURE.utilities.collapseInfo();break;case"login":CURE.login.init();break;case"boardroom":CURE.user_id=cure_user_id;CURE.user_experience=cure_user_experience;CURE.boardroom.init();break;case"boardgame":CURE.user_id=cure_user_id;CURE.user_experience=cure_user_experience;CURE.boardgame.init();break;case"stats":CURE.stats.init();break;default:CURE.landing.init()}};CURE.forgot={init:function(){$("input#refEmail").blur(function(){var a=$("input#refEmail").val();CURE.utilities.validateEmail(a)});$(".emailsub").click(function(){var a=$("input#refEmail");if(CURE.utilities.validateEmail(a.val())){if(CURE.forgot.submitEmail(a.val())){a.val("")}}})},submitEmail:function(a){$.get("SocialServer",{command:"iforgot",mail:a}).success(function(b){$("#emailAlert").html("Thank you, your password request has been sent to the provided email address.").fadeIn();return true}).error(function(b){$("#emailAlert").html("Sorry, error occured :(").fadeIn();return false});return false}};CURE.stats={heat_map:[{board_id:0,hits:877},{board_id:1,hits:733},{board_id:2,hits:713},{board_id:3,hits:702},{board_id:4,hits:607},{board_id:5,hits:597},{board_id:6,hits:649},{board_id:7,hits:673},{board_id:8,hits:593},{board_id:9,hits:708},{board_id:10,hits:560},{board_id:11,hits:606},{board_id:12,hits:697},{board_id:13,hits:606},{board_id:14,hits:613},{board_id:15,hits:520},{board_id:16,hits:645},{board_id:17,hits:577},{board_id:18,hits:613},{board_id:19,hits:626},{board_id:20,hits:595},{board_id:21,hits:515},{board_id:22,hits:546},{board_id:23,hits:621},{board_id:24,hits:655}],init:function(){var a=CURE.utilities.seedData();CURE.stats.competition_bar(a.chart1,"#compbar");CURE.utilities.drawLineGraph(a.chart1,"#chart1",600);CURE.stats.heatmap()},competition_bar:function(e,n){var l=CURE.utilities;var j=d3.time.format.utc("%Y-%m-%d");var o=400;var c=800;var k=c*0.025;var f=c*0.125;var h=d3.time.scale.utc().range([0,c]);var g=j.parse("2012-09-01");var b=j.parse("2012-10-15");h.domain([g,b]);var i=d3.scale.linear().domain([0,1]).range([o,0]);var d=d3.svg.axis().scale(h);var a=d3.svg.axis().scale(i).orient("left");var m=d3.select(n).append("svg:svg");m.append("g").attr("class","x_axis").attr("transform","translate("+f+","+(o+k)+")").call(d.tickFormat(d3.time.format("%b %d")));m.append("g").attr("class","y_axis").attr("transform","translate("+f+","+k+")").call(a);m.append("g").attr("class","guide_lines_x").attr("transform","translate("+f+","+(o+k)+")").call(d.tickFormat("").tickSize(-o,0,0));m.append("g").attr("class","guide_lines_y").attr("transform","translate("+(f)+", "+k+")").call(a.tickFormat("").tickSize(-c,0,0));m.append("g").attr("class","bars").attr("transform","translate("+f+", "+(k+o)+")").selectAll("rect").data(e.data).enter().append("rect").attr("width","12px").attr("height",function(p){return i(p.y)}).attr("x",function(p){return h(p.timestamp)}).attr("y","0px")},heatmap:function(){var d=CURE.stats.heat_map;var g=5;var e=80;var a=_(d).pluck("hits");var b=d3.scale.linear().domain([_.min(a),_.max(a)]).range(["#00FF00","#FF0000"]);var a=d3.scale.linear().domain([_.min(a),_.max(a)]).range([1,2]);var c=d3.select("#heatmap").append("svg");var f=c.selectAll("circle").data(d);f.enter().append("circle").attr("cy",function(j,h){return e+Math.floor(h/g)*40}).attr("cx",function(j,h){var k=Math.floor(h/g);return e+((h/g)*400)-(k*400)}).attr("r",function(h){return 10*a(h.hits)}).attr("fill",function(h){return b(h.hits)})}};CURE.boardgame={metadata:{game_started:(new Date).getTime(),mouse_action:[]},cards:[],p1_hand:[],p2_hand:[],p1_score:0,p2_score:0,board_state_clickable:true,cached_info_panel_unique_id:0,init:function(){var b=CURE.boardgame,a=CURE.utilities;b.board_id=a.getParameterByName("b");b.max_hand=a.getParameterByName("h");var d=a.getParameterByName("t");document.title=d+" : The Cure";b.metadata.board_id=b.board_id;b.metadata.player1_id=CURE.user_id;b.metadata.player2_id="215";var c={command:"getboard",board_id:b.board_id,};$.getJSON("MetaServer",c,function(f){CURE.dataset=f.dataset;b.cards=f.cards;_.each(b.cards,function(g){g.metadata.ux=[]});b.generateBoard();b.addCardSelectionEventHandlers();b.setupInfoToggle();b.setupHelpDisplay();b.termCalculations();if(CURE.user_experience==0){b.setupGameMetaInfo()}});var e=b.metadata.mouse_action;$(window).mousemove(_.debounce(function(f){e.push({x:f.pageX,y:f.pageY,timestamp:(new Date).getTime()})},20))},termCalculations:function(){var a=CURE.boardgame;$("#search").keyup(function(c){$("#board .gamecard").removeClass("highlight");var b=$(this).val();a.performDescriptionSearch(b);a.performOntologySearch(b);a.performRifSearch(b)})},performDescriptionSearch:function(b){var a=CURE.boardgame;var c=b.toLowerCase().trim();_.each(a.cards,function(d,e){var f=d.description;if(f==null){return false}if((c==f.substring(0,c.length)||f.indexOf(c)!=-1)&&c.length>0){$("#card_"+d.unique_id).addClass("highlight")}})},performOntologySearch:function(b){var a=CURE.boardgame;var c=b.toLowerCase().trim();_.each(a.cards,function(d,e){var f=_.pluck(_.flatten(_.pluck(d.metadata.ontology,"values")),"term").join(" ").toLowerCase().trim();if((c==f.substring(0,c.length)||f.indexOf(c)!=-1)&&c.length>0){$("#card_"+d.unique_id).addClass("highlight")}})},performRifSearch:function(b){var a=CURE.boardgame;var c=b.toLowerCase().trim();_.each(a.cards,function(d,e){var f=_.flatten(_.pluck(d.metadata.rifs,"text")).join(" ").toLowerCase().trim();if((c==f.substring(0,c.length)||f.indexOf(c)!=-1)&&c.length>0){$("#card_"+d.unique_id).addClass("highlight")}})},generateBoard:function(){var a=CURE.boardgame,b=$("#board");b.html("").unbind();_(a.cards).each(function(c){var d=c.short_name||c.unique_id;b.append("        <div id='card_"+c.unique_id+"' class='gamecard active'>          <span class='help_label'>i</span>          <span class='gene_label'>"+d+"</span>        </div>")})},addCardSelectionEventHandlers:function(){var a=CURE.boardgame;$("#board div.gamecard").click(function(f){var h=$(this);if(a.board_state_clickable){var g=0;if(a.p1_hand){g=a.p1_hand.length}if(g<a.max_hand){var b=h.attr("id").split("card_")[1];var d=_.find(a.cards,function(e){return e.unique_id==b});var c=a.returnCard(d);$("#p1_hand").append(c);a.setupHelpDisplay();a.p1_hand.push(d);a.savePlayedCard(d,1);a.getScore(a.p1_hand,1);h.removeClass("active").addClass("selected").unbind("click").html("");a.board_state_clickable=false;a.addCardToBarney()}else{$("#endgame").html("").append("<p>Sorry, you can only have 5 cards in your hand in this game.</p>").leanModal()}}else{$("#endgame").html("").append("<p>Wait your turn!</p>")}})},returnCard:function(c){var b=c.short_name||c.unique_id;var a="                <div id='playedcard_"+c.unique_id+"' class='gamecard active'>                <span class='help_label'>i</span>                <span class='gene_label'>"+b+"</span>                </div>";return a},savePlayedCard:function(c){var a=CURE.boardgame;var b={command:"saveplayedcard",board_id:a.board_id,player_id:CURE.user_id,unique_id:c.unique_id,display_loc:(c.board_index+1),timestamp:(new Date).getTime()};$.ajax({type:"POST",url:"MetaServer",data:JSON.stringify(b),dataType:"json",contentType:"application/json; charset=utf-8"})},getScore:function(f,d){var b=CURE.boardgame,a=CURE.utilities;var e=0;if(d==1){e=CURE.user_id}else{if(d==2){e="215"}}var c={board_id:b.board_id,player_id:e,command:"getscore",unique_ids:[]};_(f).each(function(g){c.unique_ids.push(g.unique_id)});$.ajax({type:"POST",url:"MetaServer",data:JSON.stringify(c),dataType:"json",contentType:"application/json; charset=utf-8",success:function(i){var g=250,h=420;if(i.max_depth>2){g=200+30*i.max_depth}$("#p"+d+"_current_tree").empty();a.drawTree(i,h,g,"#p"+d+"_current_tree");if(i.max_depth<2){b["p"+d+"_score"]=50}else{b["p"+d+"_score"]=i.evaluation.accuracy}$("span#p"+d+"_score").html(b["p"+d+"_score"]);if(d=="2"){b.board_state_clickable=true;if(b.p2_hand.length!=b.max_hand&&b.p1_score<b.p2_score&&b.p1_score>0){b.moveBarney("correct")}else{if(b.p1_score>b.p2_score){b.moveClayton("correct")}}}if(b.p2_hand.length==b.max_hand){b.saveHand(d);window.setTimeout(b.showTheResults(),1500)}}})},showTheResults:function(){var a=CURE.boardgame,b=$("#endgame");b.html("");if(a.p1_score<a.p2_score&&a.p1_score>0){b.append("<h2>Sorry, you lost this hand.</h2>");b.append("<h3><span class='replay_level'>Play Level Again?</span></h3>");a.moveBarney("win")}else{if(a.p1_score>a.p2_score&&CURE.dataset=="mammal"&&a.board_id==204){b.append("<h2>Congratulations! You finished your training!</h1>");b.append("<h3>You have gained access to the challenge area.</h3>");b.append("<h3><span class='pink'><a href='boardroom.jsp'>Start the challenge!</a></h3>");$("#holdem_button").hide();a.moveBarney("lose");a.moveClayton("win");$("#lean_overlay").click(function(){$(this).fadeOut(200);$("#endgame").css({display:"none"});window.location.href="boardroom.jsp"})}else{if(a.p1_score>a.p2_score){b.append("<h2>You beat Barney!</h2>");b.append("<h3>You earned "+a.p1_score+" points!</h3>");a.moveBarney("lose");a.moveClayton("win")}else{if(a.p1_score==a.p2_score){b.append("<h2>You tied Barney!</h2>");b.append("<h3><span class='replay_level'>Play Level Again?</span></h3>");a.moveBarney("win");a.moveClayton("win")}}}}$("#endgame").leanModal();$("span.replay_level").click(function(){a.replayHand();$("#lean_overlay").fadeOut(200);$("#endgame").css({display:"none"})});$("#lean_overlay").click(function(){$(this).fadeOut(200);$("#endgame").css({display:"none"});if(_.include(["201","202","203"],a.board_id)){window.location.href="training.jsp"}else{window.location.href="boardroom.jsp"}})},addCardToBarney:function(){var a=CURE.boardgame;var c=a.getBarneysNextCard();a.p2_hand.push(c);var d=a.returnCard(c);$("#p2_hand").append(d);a.setupHelpDisplay();var b=$("#boardgame #game_area #board div#card_"+c.unique_id);b.removeClass("active").addClass("selected").unbind("click").html("");a.getScore(a.p2_hand,2)},getBarneysNextCard:function(){var b=CURE.boardgame;var c=Math.floor(Math.random()*b.cards.length);var d=b.cards[c];var a=_.union(_(b.p1_hand).pluck("unique_id"),_(b.p2_hand).pluck("unique_id"));if(_(a).include(d.unique_id)){return b.getBarneysNextCard()}else{return d}},setupInfoToggle:function(){var a=CURE.boardgame;if(CURE.dataset=="mammal"){$("#tabs ul li.ontology").hide();$("#tabs ul li.rifs").hide();$("input#search").hide()}$("#tabs ul li").click(function(d){var b=$(this).attr("class").split(" ")[0];a.closeAllInfoTabs();$("#"+b).show();if(a.cached_info_panel_unique_id!=0){var c=_.find(a.cards,function(e){return e.unique_id==a.cached_info_panel_unique_id});c.metadata.ux.push({timestamp:(new Date).getTime(),panel:a.activeInfoPanel(),board_hover:false})}})},activeInfoPanel:function(){var a="";$.each($("div#help_area div#infoboxes div.infobox"),function(c,b){if($(b).is(":visible")){a=$(b).attr("id")}});return a},closeAllInfoTabs:function(){_.each($("div#help_area div#infoboxes div.infobox"),function(a){$(a).hide()})},setupGameMetaInfo:function(){var b=CURE.boardgame,c="";if(CURE.dataset=="mammal"){var a="features that distinguish";if(b.max_hand=="1"){a="feature that distinguishes"}c="Pick <strong>"+b.max_hand+"</strong> "+a+" mammals from other creatures.  Think of things that separate mammals from fish, insects, amphibians, reptiles..."}else{if(CURE.dataset=="dream_breast_cancer"){c="Pick <b>"+b.max_hand+"</b> genes that track breast cancer survival.  Look for genes that you think will have prognostic RNA expression or copy number variation."}}$("#endgame").html("<p>"+c+"</p>").leanModal()},infoBoxGeneHeader:function(a,b){b.append("<h1><a target='_blank' href='http://www.ncbi.nlm.nih.gov/gene/"+a.unique_id+"'>"+a.short_name+"</a></h1>");b.append("<h2>"+(a.long_name||"")+"</h2>");b.append("<p>"+(a.description||"<span class='lightfade'>No RefSeq summary available.</span>")+"</p>")},setupHelpDisplay:function(){var b=CURE.boardgame,a=CURE.utilities;$("span.help_label").hover(function(i){var g=$(this).parent().attr("id").split("card_")[1];b.cached_info_panel_unique_id=g;var h=_.find(b.cards,function(e){return e.unique_id==g});card_metadata=h.metadata;scope=$("#game_area #help_area #infoboxes");var f=$("div#ontology",scope).html("");b.infoBoxGeneHeader(h,f);_.each(card_metadata.ontology,function(j,k){f.append("<h3>"+j.type+"</h3>");var e=[];_.each(j.values,function(l,m){var n=(m==0)?a.upcaseStringFirstLetter(l.term):l.term;e.push("<a target='_blank' href='http://www.ebi.ac.uk/QuickGO/GTerm?id="+l.accession+"'>"+n+"</a>")});f.append("<p>"+e.join(", ")+"</p>")});var d=$("div#rifs",scope).html("");b.infoBoxGeneHeader(h,d);var c=d.append("<ol></ol>");_.each(card_metadata.rifs,function(e,j){c.append("<li><a target='_blank' href='http://www.ncbi.nlm.nih.gov/pubmed/"+e.pubmed_id+"'>"+a.upcaseStringFirstLetter(e.text)+"</a></li>")});card_metadata.ux.push({timestamp:(new Date).getTime(),panel:b.activeInfoPanel(),board_hover:true});i.stopPropagation()})},moveBarney:function(a){$("img#barney5").removeClass("win lose correct").hide().addClass(a).show()},moveClayton:function(a){$("img#clayton1").removeClass("win lose correct").hide().addClass(a).show()},saveHand:function(f){var c=CURE.boardgame,b=CURE.utilities;if(f==2){return false}c.metadata.game_finished=(new Date).getTime();c.cards=_.map(c.cards,function(g){delete g.metadata.ontology;delete g.metadata.rifs;return g});var a=_.pick(c,"cards","p1_hand","p2_hand","p1_score","p2_score","metadata");var e=0;if(c.p1_score>c.p2_score){e=1}else{if(c.p1_score==c.p2_score){e=2}}var d={command:"savehand",player1_id:CURE.user_id,player2_id:"215",win:e,game:a};$.ajax({type:"POST",url:"MetaServer",data:JSON.stringify(d),dataType:"json",contentType:"application/json; charset=utf-8",})},replayHand:function(){var a=CURE.boardgame;_.each([1,2],function(b){$("span#p"+b+"_score").html("0");$("div#p"+b+"_hand").html("").unbind()});$("#infoboxes div.infobox").html("");$("div#game_area div#board").html("");$("input#search").val("");a.cards=[];a.p1_hand=[];a.p2_hand=[];a.p1_score=0;a.p2_score=0;a.metadata={game_started:(new Date).getTime(),mouse_action:[]};a.board_state_clickable=true;a.cached_info_panel_unique_id=0;a.init()}};CURE.login={init:function(){var a=CURE.utilities;var c=a.getParameterByName("bad"),b=$("#alertmsg");if(c=="nametaken"){b.html("<p>Sorry, that user name has been taken. Please try another one.</p>").leanModal();$("#newuser").show();$("#olduser").hide()}else{if(c=="pw"){b.html("<p>Sorry, that username/password combination is invalid. Please try again.</p>").leanModal();$("#olduser").show()}else{if(c=="n"){$("#newuser").show();$("#olduser").hide()}else{$("#olduser").show();$("#newuserlink").click(function(d){d.preventDefault();$("#newuser").show();$("#olduser").hide()})}}}},clearInput:function(){$("input[type=text]").focus(function(){$(this).val("")})}};CURE.boardroom={init:function(){var a={command:"boardroom",user_id:CURE.user_id,dataset:CURE.dataset,room:"1"};$.getJSON("SocialServer",a,function(b){CURE.boardroom.drawGrid("#boards",b,45)})},drawGrid:function(j,e,c){var a=Math.sqrt(e.boards.length);if(Math.floor(a)!==a){e.boards=_.first(e.boards,Math.pow(Math.floor(a),2))}var f=d3.scale.linear().domain([0,10]).range([0,100]);var j=$(j),h=c,g=Math.floor(h*0.435),d=Math.floor(h*0.09),b=Math.floor(h*0.09),i=Math.floor(h*0.15);_.each(e.boards,function(m,n){var l=f(m.attempts);if(l>100){l=100}var p,q,k=g;(m.enabled==true)?p="enabled":p="disabled";if(m.enabled==false){q="&#8226;";k=k*3;b=(h*0.2)}if(m.trophy==true){q="&#9733;";k=k*0.4;b=Math.floor(h*0.2)}if(m.enabled==false&&m.trophy==true){q="&#9733;";b=Math.floor(h*0.2)}if(m.enabled==true&&m.trophy==false&&m.attempts<10){q=(m.position+1);b=Math.floor(h*0.2)}var o=j.append("          <div id='board_"+m.board_id+"' class='board "+p+"' style='height:"+h+"px; width:"+h+"px; font-size:"+k+"px; margin:"+d+"px' >             <span class='symbol' style='top:"+b+"px'>"+q+"</span>            <div class='score_slider' style='width:"+h+"px; height:"+i+"px;'>              <div class='score_value' style='width:"+l+"%; height:"+i+"px;'></div>            </div>            </div>")});$.each($("div.board.enabled"),function(l,k){var m=$(this).attr("id").split("_")[1];$(this).click(function(p){var n=$(this).find(".symbol").html();var o="boardgame.jsp?b="+m+"&t=Level "+n+" Breast Cancer Survival&h=5";window.location.href=o})})}};CURE.landing={init:function(){CURE.utilities.collapseInfo();var a={command:"gamelogs"};$.getJSON("SocialServer",a,function(d){var c=CURE.utilities.listOfLeaderBoard(d,6);var b=$("div#leaderboard ol").append(c)});$("input.playnow").click(function(c){var b="login.jsp";window.location.replace(b);return false})},getTwitterTimeline:function(){var a=document.createElement("link");a.href="twitovr.css";a.rel="stylesheet";a.type="text/css";$("twitter-widget-1").append(a)},submitEmail:function(b,a){$.get("/cure/SocialServer",{command:"invite",by:a,invited:b}).success(function(c){$("#emailAlert").html("Thank You!").fadeIn();return true}).error(function(c){$("#emailAlert").html("Sorry, error occured :(").fadeIn();return false});return false}};CURE.utilities={listOfLeaderBoard:function(c,b){var a="",d=_.sortBy(c.leaderboard,function(e){return -e.score});_(_.first(d,b)).each(function(e,f){a+='<li><span class="username">'+e.username+'</span> <span class="score">'+e.score+"</span></li>"});return a},seedData:function(){var b={};for(var a=1;a<6;a++){var c={};c.metadata={x_axis_label:"foo",y_axis_label:"bar"};c.data=d3.range(20).map(function(d){return{timestamp:(new Date(2012,8,(6+d),1,1,23,23).getTime()),y:(Math.sin(d/3)+1)/2}});b["chart"+a]=c}return b},collapseInfo:function(){$.each($("h3"),function(b,a){$(a).toggle(function(){var c=$(this).attr("class").split(" ")[0];$("#"+c).slideDown()},function(){var c=$(this).attr("class").split(" ")[0];$("#"+c).slideUp()})})},drawLineGraph:function(d,m,o){var j=d3.time.format.utc("%Y-%m-%d");var h=o||200;var k=h*0.025;var e=h*0.125;var g=d3.time.scale.utc().range([0,h]);var f=j.parse("2012-09-01");var b=j.parse("2012-10-15");g.domain([f,b]);var i=d3.scale.linear().domain([0,1]).range([h,0]);var c=d3.svg.axis().scale(g);var a=d3.svg.axis().scale(i).orient("left");var l=d3.select(m).append("svg:svg");l.append("g").attr("class","x_axis").attr("transform","translate("+e+","+(h+k)+")").call(c.tickFormat(d3.time.format("%b %d")));l.append("g").attr("class","y_axis").attr("transform","translate("+e+","+k+")").call(a);l.append("g").attr("class","guide_lines_x").attr("transform","translate("+e+","+(h+k)+")").call(c.tickFormat("").tickSize(-h,0,0));l.append("g").attr("class","guide_lines_y").attr("transform","translate("+e+", "+k+")").call(a.tickFormat("").tickSize(-h,0,0));var n=d3.svg.line().x(function(p){return g(p.timestamp)}).y(function(p){return i(p.y)}).interpolate("basis");l.append("g").attr("class","line").attr("transform","translate("+e+", "+k+")").append("path").attr("class","line").attr("d",n(d.data));l.append("g").attr("class","points").attr("transform","translate("+e+", "+k+")").selectAll("circle").data(d.data).enter().append("circle").attr("r",1.5).attr("cx",function(p){return g(p.timestamp)}).attr("cy",function(p){return i(p.y)});l.append("text").attr("class","x label").attr("text-anchor","center").attr("x",(h/2)+e).attr("y",h+(e-k)).text(d.metadata.x_axis_label);l.append("text").attr("class","y label").attr("text-anchor","center").attr("dy",h).attr("x","100").attr("transform","rotate(-90)").text(d.metadata.y_axis_label)},validateEmail:function(a){var b=/^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,6}$/;var d=$("input#refEmail"),c=b.test(a);if(c){d.css({boxShadow:"0px 0px 4px 0px #20282B"})}else{d.css({boxShadow:"0px 0px 8px 0px red"})}return c},getParameterByName:function(b){b=b.replace(/[\[]/,"\\[").replace(/[\]]/,"\\]");var a="[\\?&]"+b+"=([^&#]*)";var d=new RegExp(a);var c=d.exec(window.location.search);if(c==null){return""}else{return decodeURIComponent(c[1].replace(/\+/g," "))}},upcaseStringFirstLetter:function(a){return a.charAt(0).toUpperCase()+a.slice(1)},kind:function(a){switch(a){case"split_node":return 0;case"split_value":return 1;case"leaf_node":return 2;default:return 3}return 3},drawTree:function(o,i,g,c){var q=CURE.utilities;if(o.max_depth<2){$(c).append("Could not build a useful tree with the selected features...");return}var e="#1FA13A",b="#D44413",u=o.max_depth-1;var m=d3.layout.tree().size([i-40,g-40]),k=d3.svg.diagonal();var f=d3.select(c).append("svg").attr("width",i).attr("height",g).append("g").attr("transform","translate(0,8)");var j=m.nodes(o.tree),a=m.links(j),l=d3.scale.linear().domain([0,u]).range([b,e]),s=[],n=[],p=[];_.each(j,function(v){if(q.kind(v.kind)==0){s.push(v)}else{if(q.kind(v.kind)==1){n.push(v)}else{if(q.kind(v.kind)==2||q.kind(v.kind)==3){p.push(v)}}}});var d=f.selectAll("path.link").data(a).enter().append("path").transition().delay(400).duration(200).attr("class","link").attr("d",k).style("stroke",function(v){return l(v.source.depth)}).style("stroke-width",function(v){return 1.3*(u-v.source.depth+1)+"px"});var h=f.selectAll("g.split_node").data(s).enter().append("g").attr("class","split_node").attr("transform",function(v){return"translate("+v.x+","+v.y+")"});var t=f.selectAll("g.split_value").data(n).enter().append("g").attr("class","split_value").attr("transform",function(v){return"translate("+v.x+","+v.y+")"});var r=f.selectAll("g.leaf_node").data(p).enter().append("g").attr("class","leaf_node").attr("transform",function(v){return"translate("+v.x+","+v.y+")"});h.append("rect").transition().delay(100).duration(400).attr("height","18").attr("width",function(v){return $.trim(v.name).length*9}).attr("x",function(v){return -($.trim(v.name).length*4.5)}).attr("y","-2");h.append("text").transition().delay(100).duration(400).attr("dy",12).text(function(v){return v.name.toUpperCase()});t.append("circle").transition().delay(100).duration(400).style("fill",function(v){return l(v.depth)}).attr("r","8");t.append("text").transition().delay(100).duration(400).attr("dy",4).attr("dx",22).text(function(v){return v.name.toUpperCase()});r.append("text").transition().delay(100).duration(400).attr("dy",12).text(function(v){return v.name.toUpperCase()})},drawTreeNoLeaf:function(o,i,g,c){var q=CURE.utilities;var e="#1FA13A",b="#D44413",u=o.max_depth-1;var m=d3.layout.tree().size([i-40,g-40]),k=d3.svg.diagonal();var f=d3.select(c).append("svg").attr("width",i).attr("height",g).append("g").attr("transform","translate(20,20)");var j=m.nodes(o.tree),a=m.links(j),l=d3.scale.linear().domain([0,u]).range([b,e]),s=[],n=[],p=[];_.each(j,function(v){if(q.kind(v.kind)==0){s.push(v)}else{if(q.kind(v.kind)==1){n.push(v)}else{if(q.kind(v.kind)==2||q.kind(v.kind)==3){p.push(v)}}}});var d=f.selectAll("path.link").data(a).enter().append("path").transition().delay(400).duration(200).attr("class","link").attr("d",k).style("stroke",function(v){return l(v.source.depth)}).style("stroke-width",function(v){return 1.3*(u-v.source.depth+1)+"px"});var h=f.selectAll("g.split_node").data(s).enter().append("g").attr("class","split_node").attr("transform",function(v){return"translate("+v.x+","+v.y+")"});var t=f.selectAll("g.split_value").data(n).enter().append("g").attr("class","split_value").attr("transform",function(v){return"translate("+v.x+","+v.y+")"});var r=f.selectAll("g.leaf_node").data(p).enter().append("g").attr("class","leaf_node").attr("transform",function(v){return"translate("+v.x+","+v.y+")"});h.append("rect").transition().delay(10*u).duration(100*u).attr("height","18").attr("width",function(v){return $.trim(v.name).length*9}).attr("x",function(v){return -($.trim(v.name).length*4.5)}).attr("y","-2");h.append("text").attr("dy",12).style("font-family","Helvetica").style("font-size","12px").style("font-weight","bold").style("fill","#000").style("text-anchor","middle").text(function(v){return v.name.toUpperCase()});t.append("circle").style("fill",function(v){return l(v.depth)}).transition().delay(10*u).duration(100*u).attr("r","8");t.append("text").attr("dy",4).attr("dx",32).style("font-family","Helvetica").style("font-size","10px").style("font-weight","bold").style("fill","#000").style("text-anchor","middle").text(function(v){return v.name.toUpperCase()});r.append("text").attr("dy",12).style("font-family","Helvetica").style("font-size","12px").style("font-weight","bold").style("fill","#000").style("text-anchor","middle").text(function(v){return v.name.toUpperCase()})}};$(document).ready(function(){CURE.load()});