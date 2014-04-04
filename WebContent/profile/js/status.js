//App
Status = new Backbone.Marionette.Application();

//Models
Badge = Backbone.Model.extend({
	defaults: {
		description: ''
	},
	initialize: function(){

	}
});

//Collections
RecBadgeCollection = Backbone.Collection.extend({
	model: Badge,
	initialize : function(){
		_.bindAll(this,'parseResponse');
	},
	url: '/cure/MetaServer',
	fetch: function(){
		var args = {
				command : "get_badges",
				user_id: cure_user_id,
				reccomendbadges: 1
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
		var json = [];
		for(var temp in data){
			json.push({
				level_id: data[temp].level_id,
				description: data[temp].description,
				id: data[temp].id,
				constraints: data[temp]
			});
			delete json[json.length-1].constraints.level_id;
			delete json[json.length-1].constraints.description;
			delete json[json.length-1].constraints.id;
		}
		this.add(json);
	},
	error: function(){
		console.log("Error!");
	}
});

PlayerBadgeCollection = Backbone.Collection.extend({
	model: Badge,
	initialize : function(){
		_.bindAll(this,'parseResponse');
	},
	url: '/cure/MetaServer',
	fetch: function(){
		var args = {
				command : "get_badges",
				user_id: cure_user_id,
				reccomendbadges: 0
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
		var json = [];
		for(var temp in data){
			json.push({
				level_id: data[temp].level_id,
				description: data[temp].description,
				id: data[temp].id,
				constraints: data[temp]
			});
			delete json[json.length-1].constraints.level_id;
			delete json[json.length-1].constraints.description;
			delete json[json.length-1].constraints.id;
		}
		this.add(json);
	},
	error: function(){
		console.log("Error!");
	}
});

//Views
EmptyBadgeCollectionView = Marionette.ItemView.extend({
	template: "#empty-badge-collection-template"
	});

BadgeItemView = Marionette.ItemView.extend({
	tagName: 'tr',
	template: "#badge-entry-template",
});

BadgeCollectionView = Backbone.Marionette.CollectionView.extend({
	itemView : BadgeItemView,
	emptyView: EmptyBadgeCollectionView,
	tagName: 'table',
	className: 'table table-bordered',
	initialize : function() {
	}
});

RecBadgeItemView = Marionette.ItemView.extend({
	tagName: 'tr',
	template: "#rec-badge-entry-template",
});

RecBadgeCollectionView = Backbone.Marionette.CollectionView.extend({
	itemView : RecBadgeItemView,
	tagName: 'table',
	className: 'table table-bordered',
	initialize : function() {
	}
});

MainLayout = Marionette.Layout.extend({
	template: "#main-layout-tmpl",
  regions: {
  	"PlayerBadgeRegion" : "#PlayerBadgeRegion",
  	"RecBadgeRegion" : "#RecBadgeRegion"
  },
  initialize: function(){
  },
  onRender: function(){
  	Status.RecBadgeCollectionView = new RecBadgeCollectionView({collection: Status.RecBadgeCollection});
  	this.RecBadgeRegion.show(Status.RecBadgeCollectionView);
  	Status.PlayerBadgeCollectionView = new BadgeCollectionView({collection: Status.PlayerBadgeCollection});
  	this.PlayerBadgeRegion.show(Status.PlayerBadgeCollectionView);
  }
});

//
//-- App init!
// 
Status.addInitializer(function(options) {
//JSP Uses <% %> to render elements and this clashes with default underscore templates.
_.templateSettings = {
	interpolate : /\<\@\=(.+?)\@\>/gim,
	evaluate : /\<\@([\s\S]+?)\@\>/gim,
	escape : /\<\@\-(.+?)\@\>/gim
};
Status.addRegions(options.regions);

Status.RecBadgeCollection = new RecBadgeCollection();
Status.PlayerBadgeCollection = new PlayerBadgeCollection();
Status.RecBadgeCollection.fetch();
Status.PlayerBadgeCollection.fetch();
Status.MainLayout = new MainLayout();

Status.mainWrapper.show(Status.MainLayout);
});

//App Start
Status.start({
	regions:{
		mainWrapper: '#badge-collection-wrapper'
	}
});