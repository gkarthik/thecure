define(
    [
    // Libraries
    'marionette', 'd3', 'jquery',
    // Collection
    'app/collections/ClinicalFeatureCollection',
        'app/collections/NodeCollection', 'app/collections/ScoreBoard',
        'app/collections/TreeBranchCollection', 'app/collections/CollaboratorCollection',
        // Models
        'app/models/Comment', 'app/models/Score',
        // Views
        'app/views/JSONCollectionView',
        'app/views/NodeCollectionView','app/views/layouts/sidebarLayout',
        // Utilitites
        'app/utilities/utilities',
        //Tour
        'app/tour/tour',
        'app/tour/treeTour'
        ],
    function(Marionette, d3, $, ClinicalFeatureCollection, NodeCollection,
        ScoreBoard, TreeBranchCollection, CollaboratorCollection, Comment, Score, JSONCollectionView,
        NodeCollectionView, sidebarLayout, CureUtils, InitTour, TreeTour) {

	    Cure = new Marionette.Application();
	    Cure.utils = CureUtils;
	    //Tours
	    Cure.initTour = InitTour;
	    Cure.treeTour = TreeTour;
	    //Initialize Tours
	    Cure.initTour.init();
	    Cure.treeTour.init();
	    
	    Cure
	        .addInitializer(function(options) {
		        // JSP Uses <% %> to render elements and this clashes
		        // with default underscore templates.
		        _.templateSettings = {
		          interpolate : /\<\@\=(.+?)\@\>/gim,
		          escape : /\<\@\-(.+?)\@\>/gim
		        };
		        Backbone.emulateHTTP = true;

		        var args = {
			        command : "get_trees_all",
		        };

		        $(options.regions.PlayerTreeRegion).html(
		            "<div id='" + options.regions.PlayerTreeRegion.replace("#", "")
		                + "Tree'></div><svg id='"
		                + options.regions.PlayerTreeRegion.replace("#", "")
		                + "SVG'></svg>");
		        Cure.width = options["width"];
		        Cure.height = options["height"];
		        Cure.posNodeName = options["posNodeName"];
		        Cure.negNodeName = options["negNodeName"];

		        // Scales
		        Cure.accuracyScale = d3.scale.linear().domain([ 0, 100 ]).range(
		            [ 0, 100 ]);
		        Cure.noveltyScale = d3.scale.linear().domain([ 0, 1 ]).range(
		            [ 0, 100 ]);
		        Cure.sizeScale = d3.scale.linear().domain([ 0, 1 ]).range(
		            [ 0, 100 ]);
		        
		        //Zoom Controls TODO: Move to view.
		        Cure.scaleLevel = 1;
		        
		        $(".zoomin").on("click",function(){
		        	if (Cure.PlayerNodeCollection.models.length > 0){
		        		if(Cure.scaleLevel <= 1.5){
				        	Cure.scaleLevel += 0.1;
			        	}
			        	Cure.utils.transformRegion(Cure.PlayerSvg.attr('transform'),Cure.scaleLevel);
		        	}
		        });
		        
		        $(".zoomout").on("click",function(){
		        	if (Cure.PlayerNodeCollection.models.length > 0){
		        		if(Cure.scaleLevel >= 0.5){
				        	Cure.scaleLevel -= 0.1;
			        	}
			        	Cure.utils.transformRegion(Cure.PlayerSvg.attr('transform'),Cure.scaleLevel);
		        	} 
		        });
		        
      				function zoomed() {
      					if(d3.event.sourceEvent.type!="mousemove"){
      						var top = $("body").scrollTop();
  			        	$("body").scrollTop(top+d3.event.sourceEvent.deltaY);
      					} else {
      						if (Cure.PlayerNodeCollection.models.length > 0) {
    				        var t = d3.event.translate, s = d3.event.scale, height = Cure.height, width = Cure.width;
    				        if (Cure.PlayerSvg.attr('width') != null
    				            && Cure.PlayerSvg.attr('height') != null) {
    					        width = Cure.PlayerSvg.attr('width') * (8 / 9),
    					            height = Cure.PlayerSvg.attr('height');
    				        }
    				        t[0] = Math.min(width / 2 * (s), Math.max(width / 2 * (-1 * s),
    				            t[0]));
    				        t[1] = Math.min(height / 2 * (s), Math.max(height / 2
    				            * (-1 * s), t[1]));
    				        zoom.translate(t);
    				        Cure.PlayerSvg.attr("transform", "translate(" + t + ")scale("+Cure.scaleLevel+")");
    				        var splitTranslate = String(t).match(/-?[0-9\.]+/g);
    				        $("#PlayerTreeRegionTree").css(
    				            {
    					            "transform" : "translate(" + splitTranslate[0] + "px,"
    					                + splitTranslate[1] + "px)scale("+Cure.scaleLevel+")"
    				            });
    			        }
      					}
  			        
  		        }

  		        var zoom = d3.behavior.zoom().scaleExtent([ 1, 1 ]).on("zoom",
  		            zoomed);
		        
		        $(options.regions.PlayerTreeRegion + "Tree").css({
			        "width" : Cure.width
		        });
		        
		        Cure.PlayerSvg = d3
		            .select(options.regions.PlayerTreeRegion + "SVG").attr("width",
		                Cure.width).attr("height", Cure.height).call(zoom).append(
		                "svg:g").attr("transform", "translate(0,0)").attr("class",
		                "dragSvgGroup");

		        // Event Initializers
		        $("#HelpText").on("click", function() {
			        var html = $(this).html();
			        Cure.utils.ToggleHelp(false, Cure.helpText);
		        });
		        
		        $("body").delegate("#closeHelp", "click", function() {
			        Cure.utils.ToggleHelp(true);
		        });

		        $("body").delegate(".close", "click", function() {
			        if ($(this).parent().hasClass("alert")) {
				        $(this).parent().hide();
			        } else {
				        $(this).parent().parent().parent().hide();
			        }
		        });
		        $(document).mouseup(
		            function(e) {
			            var container = $(".addnode_wrapper");
			            var geneList = $(".ui-autocomplete");

			            if (!container.is(e.target)
			                && container.has(e.target).length == 0
			                && !geneList.is(e.target)
			                && geneList.has(e.target).length == 0) {
				            $("input.mygene_query_target").val("");
				            if (Cure.MyGeneInfoRegion) {
					            Cure.MyGeneInfoRegion.close();
				            }
			            }

			            var classToclose = $('.blurCloseElement');
			            if (!classToclose.is(e.target)
			                && classToclose.has(e.target).length == 0) {
				            classToclose.hide();
			            }
		          });
		        //TODO: MOVE TO ScoreBoardView
		        Cure.ScoreBoardRequestSent = false;
		        $("#scoreboard_wrapper").scroll(
		            function() {
			            if ($("#scoreboard_wrapper").scrollTop()+$("#scoreboard_wrapper").height() >= $("#scoreboard_innerwrapper").height()) {
			            	var t = window.setTimeout(function(){
					            if (!Cure.ScoreBoardRequestSent) {
						            window.clearTimeout(t);
							          Cure.ScoreBoard.fetch();
						            Cure.ScoreBoardRequestSent = true;
					            }
			            	}, 500);
			            }
		            });
		        options.regions.PlayerTreeRegion += "Tree";
		        Cure.addRegions(options.regions);
		        Cure.colorScale = d3.scale.category10();
		        Cure.edgeColor = d3.scale.category20();
		        Cure.Scorewidth = options["Scorewidth"];
		        Cure.Scoreheight = options["Scoreheight"];
		        Cure.duration = 500;
		        Cure.cluster = d3.layout.tree().size([ (Cure.width-100), "auto" ])
		            .separation(function(a, b) {
			            try {
				            if (a.children.length > 2) {
					            return a.children.length;
				            }
			            } catch (e) {

			            }
			            return (a.parent == b.parent) ? 1 : 2;
		            });
		        Cure.diagonal = d3.svg.diagonal().projection(function(d) {
			        return [ d.x, d.y ];
		        });
		        Cure.ClinicalFeatureCollection = new ClinicalFeatureCollection();
		        Cure.ClinicalFeatureCollection.fetch();
		        Cure.ScoreBoard = new ScoreBoard();
		        // Sync Score Board
		        Cure.PlayerNodeCollection = new NodeCollection();
		        Cure.TreeBranchCollection = new TreeBranchCollection();
		        Cure.CollaboratorCollection = new CollaboratorCollection();
		        Cure.Comment = new Comment();
		        Cure.Score = new Score();
		        Cure.PlayerNodeCollectionView = new NodeCollectionView({
			        collection : Cure.PlayerNodeCollection
		        });
		        Cure.JSONCollectionView = new JSONCollectionView({
			        collection : Cure.PlayerNodeCollection
		        });
		        Cure.sidebarLayout = new sidebarLayout();
		        Cure.PlayerTreeRegion.show(Cure.PlayerNodeCollectionView);
		        Cure.JSONSummaryRegion.show(Cure.JSONCollectionView);
		        Cure.SideBarRegion.show(Cure.sidebarLayout);
		        Cure.relCoord = $('#PlayerTreeRegionSVG').offset();
	        });
	    return Cure;
    });