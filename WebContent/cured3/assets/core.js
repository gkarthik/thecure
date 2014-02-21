define([
    	'marionette',
    	'd3',
    	'jquery',
    	//Collection
    	'app/collections/ClinicalFeatureCollection',
    	'app/collections/NodeCollection',
    	'app/collections/ScoreBoard',
    	//Models
    	'app/models/Comment',
    	'app/models/Score',
    	//Views
    	'app/views/CommentView',
    	'app/views/JSONCollectionView',
    	'app/views/NodeCollectionView',
    	'app/views/ScoreBoardView',
    	'app/views/ScoreView',
    	//Utilitites
    	'app/utilities/utilities'
    ], function(Marionette, d3, $, ClinicalFeatureCollection, NodeCollection, ScoreBoard, Comment, Score, CommentView, JSONCollectionView, NodeCollectionView, ScoreBoardView, ScoreView, CureUtils) {
	
	Cure = new Marionette.Application();
	Cure.utils = CureUtils;
	Cure.addInitializer(function(options) {
		//JSP Uses <% %> to render elements and this clashes with default underscore templates.
		_.templateSettings = {
			interpolate : /\<\@\=(.+?)\@\>/gim,
			evaluate : /\<\@([\s\S]+?)\@\>/gim,
			escape : /\<\@\-(.+?)\@\>/gim
		};
		Backbone.emulateHTTP = true;
		
		var args = {
				command : "get_trees_all",
		};
		
		$(options.regions.PlayerTreeRegion).html(
				"<div id='" + options.regions.PlayerTreeRegion.replace("#", "")
						+ "Tree'></div><svg id='"
						+ options.regions.PlayerTreeRegion.replace("#", "") + "SVG'></svg>");
		Cure.width = options["width"];
		Cure.height = options["height"];
		Cure.posNodeName = options["posNodeName"];
		Cure.negNodeName = options["negNodeName"];
		
		//Scales
		Cure.accuracyScale = d3.scale.linear().domain([ 0, 100 ]).range([ 0, 100 ]);
		Cure.noveltyScale = d3.scale.linear().domain([ 0, 1 ]).range([ 0, 100 ]);
		Cure.sizeScale = d3.scale.linear().domain([ 0, 1 ]).range([ 0, 100 ]);
		
		function zoomed() 
		{
			if(Cure.PlayerNodeCollection.models.length > 0){
				 var t = d3.event.translate,
			      s = d3.event.scale,
			      height = Cure.height,
			      width = Cure.width;
				 if(Cure.PlayerSvg.attr('width')!=null && Cure.PlayerSvg.attr('height')!=null){
					  width = Cure.PlayerSvg.attr('width')*(8/9),
					  height = Cure.PlayerSvg.attr('height');
				 }
				 t[0] = Math.min(width/2 * (s), Math.max(width/2 * (-1 * s), t[0]));
			  	t[1] = Math.min(height/2 * (s), Math.max(height/2 * (-1 * s), t[1]));
			  	zoom.translate(t);
			  	Cure.PlayerSvg.attr("transform", "translate(" + t + ")scale(" + s + ")");
				var splitTranslate = String(t).match(/-?[0-9\.]+/g); 
				$("#PlayerTreeRegionTree").css({"transform": "translate(" + splitTranslate[0] + "px,"+splitTranslate[1]+"px)scale(" + s + ")"});
			}
		}
		
		var zoom = d3.behavior.zoom()
	    .scaleExtent([-10, 10])
	    .on("zoom", zoomed);
		
		$(options.regions.PlayerTreeRegion + "Tree").css({"width":Cure.width});
		Cure.PlayerSvg = d3.select(options.regions.PlayerTreeRegion + "SVG").attr(
				"width", Cure.width).attr("height", Cure.height).call(zoom).append("svg:g")
				.attr("transform", "translate(0,0)").attr("class","dragSvgGroup");
				
		//Event Initializers
						
		$("#HelpText").on("click",function(){
			var html = $(this).html();
			Cure.utils.ToggleHelp(false, html);
		});
		
		$("body").delegate("#closeHelp","click",function(){
			Cure.utils.ToggleHelp(true);
		});
		
		$("body").delegate(".close","click",function(){
			if($(this).parent().attr("class")=="alert")
			{
				$(this).parent().hide();
			}
			else{
				$(this).parent().parent().parent().hide();
			}
		});
		
		$(document).mouseup(function(e) {
			var container = $(".addnode_wrapper");
			var geneList = $(".ui-autocomplete");

			if (!container.is(e.target)	&& container.has(e.target).length == 0 && !geneList.is(e.target)	&& geneList.has(e.target).length == 0) 
			{
				$("input.mygene_query_target").val("");
				if (Cure.MyGeneInfoRegion) {
					Cure.MyGeneInfoRegion.close();
				}
			}
			
			var classToclose = $('.blurCloseElement');
			if (!classToclose.is(e.target)	&& classToclose.has(e.target).length == 0) 
			{
				classToclose.hide();
			}
		});
		
		Cure.ScoreBoardRequestSent = false;
		$("#scoreboard_wrapper").scroll(function () { 
			   if ($("#scoreboard_wrapper").scrollTop() >= $("#scoreboard_wrapper .ScoreBoardInnerWrapper").height() - $("#scoreboard_wrapper").height()) {
			      if(!Cure.ScoreBoardRequestSent){
			    	  Cure.ScoreBoard.fetch();
			    	  Cure.ScoreBoardRequestSent = true;
			      } 
			   }
			});
		
		$(".togglePanel").on("click",function(){
			$(".panel-body").slideUp();
			var panelBody = $(this).parent().parent().find(".panel-body");
			panelBody.slideToggle();
		});
		
		$("#save_tree").on("click",function(){
			var tree;
			if(Cure.PlayerNodeCollection.models[0])
			{
				tree = Cure.PlayerNodeCollection.models[0].toJSON();
				var args = {
						command : "savetree",
						dataset : "metabric_with_clinical",
						treestruct : tree,
						player_id : cure_user_id,
						comment: Cure.Comment.get("content")
				};
				$.ajax({
					type : 'POST',
					url : '/cure/MetaServer',
					data : JSON.stringify(args),
					dataType : 'json',
					contentType : "application/json; charset=utf-8",
					success : Cure.showAlert("saved"),
					error : Cure.showAlert("Error Occured. Please try again in a while.")
				});
			}
			else
			{
				tree = [];
				Cure.utils.showAlert("Empty Tree!<br>Please build a tree by using the auto complete box.");
			}
		});
		
		options.regions.PlayerTreeRegion+="Tree";
		Cure.addRegions(options.regions);
		Cure.colorScale = d3.scale.category10();
		Cure.edgeColor = d3.scale.category20();
		Cure.Scorewidth = options["Scorewidth"];
		Cure.Scoreheight = options["Scoreheight"];
		Cure.duration = 500;
		var width = 0;
		Cure.cluster = d3.layout.tree().size([ Cure.width*0.8, "auto" ]).separation(function(a, b) {
			try{
				if(a.children.length>2){
					return a.children.length;
				}
			} catch(e){
				
			}
			return (a.parent==b.parent) ?1 :2;
		});
		Cure.diagonal = d3.svg.diagonal().projection(function(d) {
			return [ d.x, d.y ];
		});
		Cure.ClinicalFeatureCollection = new ClinicalFeatureCollection();
		Cure.ClinicalFeatureCollection.fetch();
		Cure.ScoreBoard = new ScoreBoard();
		//Sync Score Board
		Cure.ScoreBoard.fetch();
		Cure.PlayerNodeCollection = new NodeCollection();
		Cure.Comment = new Comment();
		Cure.Score = new Score();
		Cure.ScoreView = new ScoreView({
			"model" : Cure.Score
		});
		Cure.CommentView = new CommentView({model:Cure.Comment});
		Cure.PlayerNodeCollectionView = new NodeCollectionView({
			collection : Cure.PlayerNodeCollection
		});
		Cure.JSONCollectionView = new JSONCollectionView({
			collection : Cure.PlayerNodeCollection
		});
		Cure.ScoreBoardView = new ScoreBoardView({
			collection: Cure.ScoreBoard
		});
		
		Cure.PlayerTreeRegion.show(Cure.PlayerNodeCollectionView);
		Cure.ScoreRegion.show(Cure.ScoreView);
		Cure.ScoreBoardRegion.show(Cure.ScoreBoardView);
		Cure.JSONSummaryRegion.show(Cure.JSONCollectionView);
		Cure.CommentRegion.show(Cure.CommentView);
		Cure.relCoord = $('#PlayerTreeRegionSVG').offset();
	});
	
	return Cure;
 });