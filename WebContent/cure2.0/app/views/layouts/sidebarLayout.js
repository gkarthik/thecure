define([
	'jquery',
	'marionette',
	//Views
	'app/views/AddRootNodeView',
	'app/views/CommentView', 
  'app/views/TreeBranchCollectionView', 'app/views/ScoreBoardView',
  'app/views/ScoreView', 'app/views/CollaboratorCollectionView', 
  'app/views/ScoreKey', 'app/views/BadgeCollectionView',
  'app/views/layouts/PathwaySearchLayout',
  'app/views/layouts/AggregateNodeLayout',
	//Templates
	'text!app/templates/sidebarLayout.html',
	//Plugins
	'odometer'
    ], function($, Marionette, AddRootNodeView, CommentView, TreeBranchCollectionView, ScoreBoardView, ScoreView, CollaborativeCollectionView, ScoreKeyView, BadgeCollectionView, PathwaySearchLayout, AggNodeLayout, sidebarLayoutTemplate, Odometer) {
sidebarLayout = Marionette.Layout.extend({
    template: sidebarLayoutTemplate,
    regions: {
    	"ScoreRegion" : "#ScoreRegion",
	    "CommentRegion" : "#CommentRegion",
	    "ScoreBoardRegion" : "#scoreboard_innerwrapper",
	    "TreeBranchRegion": "#tree-explanation-wrapper",
	    "CollaboratorsRegion": "#CollaboratorsRegion",
	    "ScoreKeyRegion": "#ScoreKeyRegion",
	    "BadgeRegion": "#BadgeRegion",
	    "PathwaySearchRegion": "#PathwaySearchRegion",
	    "AggNodeRegion":"#AggNodeRegion"
    },
    ui: {
    	ScoreWrapper: "#score-board-outerWrapper",
    	TreeExpWrapper: "#tree-explanation-outerWrapper",
    	displayWrapper: "#displayWrapper"
    },
    events:{
    	'click #current-tree-rank': 'showCurrentRank',
    	'click #tree-explanation-button': 'toggleTreeExp',
    	'click #BadgesPlaceholder': 'showBadges',
    	'click #new-tree': 'createNewTree'
    },
    className: 'panel panel-default',
    initialize: function(){
    	_.bindAll(this,'toggleTreeExp');
    },
    createNewTree: function(){
    	$("div.node:nth-child(1) > .delete").trigger('click');
    },
    showCurrentRank: function(){
    	$(this.ui.ScoreWrapper).show();
    },
    showBadges: function(){
    	$("#badge-outer-wrapper").show();
    },
    toggleTreeExp: function(ev){
    	if(Cure.PlayerNodeCollection.length != 0){
    		if($(ev.target).hasClass("showTreeExp")){
    			$(this.ui.displayWrapper).hide();
    			$(ev.target).removeClass("showTreeExp");
        	$(ev.target).addClass("closeTreeExp");
        	$(ev.target).html('<i class="glyphicon glyphicon-pencil"></i>Close Tree Explanation');
        	$(this.ui.TreeExpWrapper).show();
      	} else {
      		$(this.ui.displayWrapper).show();
      		$(ev.target).removeClass("closeTreeExp");
        	$(ev.target).addClass("showTreeExp");
        	$(ev.target).html('<i class="glyphicon glyphicon-pencil"></i>Show Tree Explanation');
        	$(this.ui.TreeExpWrapper).hide();
      	}
    	} else {
    		Cure.utils
        .showAlert("<strong>Empty Tree!</strong><br>You can build the tree by using the autocomplete.", 0);
    	}
    },
    onRender: function(){
    	Cure.ScoreView = new ScoreView({
        "model" : Cure.Score
      });
      Cure.CommentView = new CommentView({
        model : Cure.Comment
      });
      Cure.ScoreBoardView = new ScoreBoardView({
        collection : Cure.ScoreBoard
      });
      Cure.TreeBranchCollectionView = new TreeBranchCollectionView({
      	collection: Cure.TreeBranchCollection
      });
      Cure.CollaboratorCollectionView = new CollaborativeCollectionView({
      	collection: Cure.CollaboratorCollection
      });
      Cure.ScoreKeyView = new ScoreKeyView({
      	model: Cure.Score
      });
      Cure.BadgeCollectionView = new BadgeCollectionView({
      	collection: Cure.BadgeCollection
      });
      this.ScoreRegion.show(Cure.ScoreView);
      this.ScoreBoardRegion.show(Cure.ScoreBoardView);
      this.CommentRegion.show(Cure.CommentView);
      this.TreeBranchRegion.show(Cure.TreeBranchCollectionView);
      this.CollaboratorsRegion.show(Cure.CollaboratorCollectionView);
      this.ScoreKeyRegion.show(Cure.ScoreKeyView);
      this.BadgeRegion.show(Cure.BadgeCollectionView);
    },
    onShow: function(){
    	this.$el.attr('id',"cure-panel");
    	this.$el.draggable({handle: '.panel-heading-main'});
  		var el = document.getElementById("score");
  		od = new Odometer({
  		  el: el,
  		  value: 0,
  		  duration: '2000',
  		  format: '',
  		  theme: 'train-station'
  		});
    }
});
return sidebarLayout;
});
