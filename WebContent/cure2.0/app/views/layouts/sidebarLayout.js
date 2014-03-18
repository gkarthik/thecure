define([
	'jquery',
	'marionette',
	//Views
	'app/views/AddRootNodeView',
	'app/views/CommentView', 
  'app/views/TreeBranchCollectionView', 'app/views/ScoreBoardView',
  'app/views/ScoreView', 'app/views/CollaboratorCollectionView', 
	//Templates
	'text!app/templates/sidebarLayout.html',
	//Plugins
	'odometer'
    ], function($, Marionette, AddRootNodeView, CommentView, TreeBranchCollectionView, ScoreBoardView, ScoreView, CollaborativeCollectionView, sidebarLayoutTemplate, Odometer) {
sidebarLayout = Marionette.Layout.extend({
    template: sidebarLayoutTemplate,
    regions: {
    	"ScoreRegion" : "#ScoreRegion",
	    "CommentRegion" : "#CommentRegion",
	    "ScoreBoardRegion" : "#scoreboard_innerwrapper",
	    "TreeBranchRegion": "#tree-explanation-wrapper",
	    "CollaboratorsRegion": "#CollaboratorsRegion"
    },
    ui: {
    	ScoreWrapper: "#score-board-outerWrapper",
    	TreeExpWrapper: "#tree-explanation-outerWrapper",
    	displayWrapper: "#displayWrapper"
    },
    events:{
    	'click #save_tree': 'saveTree',
    	'click #current-tree-rank': 'showCurrentRank',
    	'click #tree-explanation-button': 'toggleTreeExp'
    },
    className: 'panel panel-default',
    initialize: function(){
    	_.bindAll(this,'toggleTreeExp');
    },
    showCurrentRank: function(){
    	$(this.ui.ScoreWrapper).show();
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
      this.ScoreRegion.show(Cure.ScoreView);
      this.ScoreBoardRegion.show(Cure.ScoreBoardView);
      this.CommentRegion.show(Cure.CommentView);
      this.TreeBranchRegion.show(Cure.TreeBranchCollectionView);
      this.CollaboratorsRegion.show(Cure.CollaboratorCollectionView);
    },
    onShow: function(){
    	this.$el.attr('id',"cure-panel");
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
