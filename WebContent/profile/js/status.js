//App
Status = new Backbone.Marionette.Application();

//Models
Badge = Backbone.Model.extend({
	defaults: {
		description: ''
	},
	initialize: function(){
		this.createDescription();
	},
	createDescription: function(){
		var desctext = "Create ";
		var constraints = this.get('constraints');
		var ctr = 0;
		var flag = 1;
		for(var temp in constraints){
			if(temp == "globaltreeno" || temp == "treeno"){
				if(temp=="globaltreeno"){
					desctext += constraints[temp]+" trees to earn this badge";
					flag = 0;
				} 
				if(temp=="treeno"){
					desctext += constraints[temp]+" trees ";
					flag = 0;
				}
			}
		}
		if(flag){
			desctext+="a tree ";
		}
		for(var temp in constraints){
			if(ctr>0 && temp!="globaltreeno" && temp!="treeno"){
				desctext+=" and ";
			}
				if(temp=="genenumber"){
					if(constraints[temp]>1){
						desctext +=" with "+constraints[temp]+" genes";
					} else if(constraints[temp]==1){
						desctext += " with atleast one gene";
					} else if(constraints[temp]==0){
						desctext += " with only clinical features";
					}
				}
				if(temp=="cfnumber"){
					if(constraints[temp]>1){
						desctext += " with "+constraints[temp]+" clinical features";
					} else if(constraints[temp]==1){
						desctext += " with atleast one clinical feature";
					} else if(constraints[temp]==0){
						desctext += " with only genes";
					}
				}
			if(temp=="leafnodeacc"){
				desctext += "where atleast one leaf node is "+constraints[temp]+"% accurate";
			} else if(temp=="leafnodesize") {
				desctext += "where atleast one leaf node contains "+constraints[temp]+"% of all cases";
			} else if(temp=="score" || temp == "size" || temp == "novelty" || temp == "accuracy") {
				desctext += "with a "+temp+" ";
				var compText="greater";
				if(constraints[temp]<0){
					compText = "lesser";
				}
				desctext+=compText+" than "+constraints[temp];
				}
			
			if(temp=="collaborators"){
				desctext+="by collaborating with atleast "+constraints[temp]+" other players";
			}
			ctr++;
			console.log(ctr);
		}
		desctext+=".";
		this.set('description',desctext);
	}
});

//Collections
BadgeCollection = Backbone.Collection.extend({
	model: Badge,
	initialize : function(){
		_.bindAll(this,'parseResponse');
	},
	url: '/cure/MetaServer',
	fetch: function(){
		var args = {
				command : "get_badges",
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
		var json = [];
		for(var temp in data){
			json.push({
				level_id: data[temp].level_id,
				constraints: data[temp]
			});
			delete json[json.length-1].constraints.level_id;
		}
		this.add(json);
	},
	error: function(){
		console.log("Error!");
	}
});

//Views
BadgeItemView = Marionette.ItemView.extend({
	tagName: 'tr',
	className: function(){
		
	},
	ui: {
		
	},
	initialize : function() {
		this.$el.click(this.loadNewTree);
		this.model.set("cid",this.model.cid);
	},
	template: "#badge-entry-template",
});

BadgeCollectionView = Backbone.Marionette.CollectionView.extend({
	itemView : BadgeItemView,
	tagName: 'table',
	className: 'table table-bordered',
	initialize : function() {
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

Status.BadgeCollection = new BadgeCollection();
Status.BadgeCollection.fetch();
Status.BadgeCollectionView = new BadgeCollectionView({collection: Status.BadgeCollection});

Status.mainWrapper.show(Status.BadgeCollectionView);
});

//App Start
Status.start({
	regions:{
		mainWrapper: '#profile-container'
	}
});