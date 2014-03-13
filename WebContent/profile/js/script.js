//App
Library = new Backbone.Marionette.Application();

//Models
Tree = Backbone.Model.extend({
	defaults: {
		comment: "",
		created: 0,
		id: 0,
		ip: "",
		rank: 0,
		player_name: "",
		json_tree :{
			novelty : 0,
			pct_correct : 0,
			size : 1,// Least Size got form server = 1.
			score : 0,
			text_tree : '',
			treestruct : {}
		}
	},
	initialize: function(){
		_.bindAll(this, 'updateScore');
		this.bind('change', this.updateScore);
		this.updateScore();
	},
	updateScore: function(){
		if(this.get("json_tree").score != "Score"){
			var scoreVar = this.get('json_tree');
			if(scoreVar.size>=1) {
				scoreVar.score = Math.round(750 * (1 / scoreVar.size) + 
						500 * scoreVar.novelty + 
						1000 * scoreVar.pct_correct);
			} else {
				scoreVar.score = 0;
			}
			this.set("json_tree", scoreVar);
		}
	}
});

//Collections
UserTreeCollection = Backbone.Collection.extend({
	model: Tree,
	initialize : function(){
		_.bindAll(this,'parseResponse');
	},
	url: '/cure/MetaServer',
	fetch: function(){
		var args = {
				command : "get_trees_user_id",
				user_id: cure_user_id
		};
		$.ajax({
			type : 'POST',
			url : '/cure/MetaServer',
			data : JSON.stringify(args),
			dataType : 'json',
			contentType : "application/json; charset=utf-8",
			success : this.parseResponse,
			error : this.error
		});
	},
	parseResponse: function(data){
		var trees = data.trees;
		trees.unshift({
			comment: "Comment",
			created: "Created",
			id: "id",
			ip: "ip",
			player_name: "<i class='glyphicon glyphicon-user'></i>",
			json_tree :{
				novelty : "Nov",
				pct_correct : "Acc",
				size : "Size",
				score : "Score",
				text_tree : '',
				treestruct : {}
			}
		});
		this.add(trees);
	}
});

CommunityTreeCollection = Backbone.Collection.extend({
	model: Tree,
	initialize : function(){
		_.bindAll(this,'parseResponse');
	},
	lowerLimit: 0,
	upperLimit: 200,
	comparator: 'rank',
	url : '/cure/MetaServer',
	fetch: function(direction){
		if(this.allowRequest){
			var args = {
					command : "get_trees_with_range",
					lowerLimit : this.lowerLimit,
					upperLimit : this.upperLimit,
					orderby: "score"
			};
			this.lowerLimit +=200;
			this.upperLimit += 200;
			$.ajax({
				type : 'POST',
				url : this.url,
				data : JSON.stringify(args),
				dataType : 'json',
				contentType : "application/json; charset=utf-8",
				success : this.parseResponse,
				error : this.error,
				async: true
			});
		}
	},
	allowRequest : 1,
	parseResponse : function(data) {
		//If empty tree is returned, no tree rendered.
		var trees = data.trees;
		trees.unshift({
			comment: "Comment",
			created: "Created",
			id: "id",
			ip: "ip",
			player_name: "<i class='glyphicon glyphicon-user'></i>",
			json_tree :{
				novelty : "Nov",
				pct_correct : "Acc",
				size : "Size",
				score : "Score",
				text_tree : '',
				treestruct : {}
			}
		});
		
		if(data.n_trees > 0) {
			this.add(trees);
			this.allowRequest = 1;
		} else {
			this.allowRequest = 0;
		}
	},
	error : function(data) {
		Cure.utils
    .showAlert("<strong>Server Error</strong><br>Please try saving again in a while.", 0);
	}
});

//Views
TreeItemView = Marionette.ItemView.extend({
	tagName: 'tr',
	className: function(){
		if(this.model.get('json_tree').pct_correct!="Accuracy"){
			return "tree-score-entry";
		}
		return "";
	},
	ui : {
		
	},
	initialize : function() {
		this.$el.click(this.loadNewTree);
	},
	template: "#score-entry-template",
	render: function(){
		if(this.model.get('rank')!=0){
			console.log("#treePreview"+this.model.get('rank'));
			d3.selectAll("#treePreview"+this.model.get('rank'))
				.attr("width",200)
				.attr("height",200)
				.append("svg:g");
		}
	}
});

TreeCollectionView = Backbone.Marionette.CollectionView.extend({
	itemView : TreeItemView,
	tagName: 'table',
	className: 'table table-bordered',
	initialize : function() {
	},
	appendHtml: function(collectionView, itemView, index){
    	if(collectionView.children.findByModel(index-1)){
    		collectionView.children.findByModel(index-1).$el.after(itemView.$el);
    	} else {
    		collectionView.$el.append(itemView.$el);
    	}
   },
   ScoreBoardRequestSent: false
});

MainLayout = Marionette.Layout.extend({
  template: "#main-layout-template",
  className: 'row',
  regions: {
  	"UserRegion" : "#user-treecollection-wrapper",
  	"CommunityRegion" : "#community-treecollection-wrapper"
  },
  ui:{
  	'navLinks':'#sidebar-fixed li'
  },
  events:{
  	'click #sidebar-fixed li a': 'toggleNav'
  },
  initialize: function(){
  	_.bindAll(this,'toggleNav');
  },
  toggleNav: function(ev){
  		if(!$(ev.target).parent().hasClass("active")){
  			$(this.ui.navLinks).removeClass("active");
      	$(ev.target).parent().addClass("active");
      	var elid = "#"+$(ev.target).parent().attr('id').replace("button","wrapper");
      	console.log(elid);
      	$('.collection-wrapper').hide();
      	$(elid).show();
    	}
  },
  onRender: function(){
  	this.UserRegion.show(Library.UserTreeCollectionView);
  	this.CommunityRegion.show(Library.CommunityTreeCollectionView);
  },
  onShow: function(){

  }
});

//
//-- App init!
// 
Library.addInitializer(function(options) {
//JSP Uses <% %> to render elements and this clashes with default underscore templates.
_.templateSettings = {
	interpolate : /\<\@\=(.+?)\@\>/gim,
	evaluate : /\<\@([\s\S]+?)\@\>/gim,
	escape : /\<\@\-(.+?)\@\>/gim
};
Library.addRegions(options.regions);

Library.UserTreeCollection = new UserTreeCollection();
Library.UserTreeCollection.fetch();
Library.CommunityTreeCollection = new CommunityTreeCollection();
Library.CommunityTreeCollection.fetch();

Library.UserTreeCollectionView = new TreeCollectionView({
	collection: Library.UserTreeCollection
});
Library.CommunityTreeCollectionView = new TreeCollectionView({
	collection: Library.CommunityTreeCollection
});
Library.MainLayout = new MainLayout();
Library.mainWrapper.show(Library.MainLayout);
});

//App Start
Library.start({
	regions:{
		mainWrapper: '#profile-container'
	}
});