//App
DashBoard = new Backbone.Marionette.Application();

//Models

//Views
AddRowView = Marionette.ItemView.extend({
	ui: {
		"row": '.rowvalue',
		"levelid": "#level-id",
		"postRequest": "#add-badge"
	},
	events: {
		'click #add-badge': 'postRequest'
	},	
	url: '/cure/MetaServer',
	template: "#add-row",
	initialize: function(){
		_.bindAll(this,'postRequest','sucess','error');
	},
	postRequest: function(){
		var desctext = "Create ";
		var constraints = {};
		var listConstraints = {};
		$(this.ui.row).each(function(){ 
			if($(this).val()!=""){
				constraints[$(this).attr("id")]= $(this).val();
			}
			listConstraints[$(this).attr("id")]= $(this).val();
		});
		var ctr = 0;
		var flag = 1;
		for(var temp in constraints){
			if(temp == "globaltreeno" || temp == "treeno"){
				if(temp=="globaltreeno" && constraints[temp]>1){
					desctext += constraints[temp]+" trees";
					flag = 0;
				} 
				if(temp=="treeno" && constraints[temp]>1){
					desctext += constraints[temp]+" trees ";
					flag = 0;
				}
			}
		}
		if(flag){
			desctext+="a tree";
		}
		
		for(var temp in constraints){
			if(ctr>0 && temp!="globaltreeno" && temp!="treeno"){
				desctext+=" and ";
			}
				if(temp=="genenumber"){
					if(constraints[temp]>1){
						desctext +=" with "+constraints[temp]+" genes";
					} else if(constraints[temp]==1){
						desctext += " with at least one gene";
					} else if(constraints[temp]==0){
						desctext += " with only clinical features";
					}
				}
				if(temp=="cfnumber"){
					if(constraints[temp]>1){
						desctext += " with "+constraints[temp]+" clinical features";
					} else if(constraints[temp]==1){
						desctext += " with at least one clinical feature";
					} else if(constraints[temp]==0){
						desctext += " with only genes";
					}
				}
			if(temp=="leafnodeacc"){
				desctext += " where at least one leaf node is "+constraints[temp]+"% accurate";
			} else if(temp=="leafnodesize") {
				desctext += " where at least one leaf node contains "+constraints[temp]+"% of all cases";
			} else if(temp=="score" || temp == "size" || temp == "novelty" || temp == "accuracy") {
				desctext += " with a "+temp+" ";
				var compText="greater";
				if(constraints[temp]<0){
					compText = "lesser";
				}
				desctext+=compText+" than "+constraints[temp];
				}
			
			if(temp=="collaborators"){
				desctext+="by collaborating with at least "+constraints[temp]+" other players";
			}
			ctr++;
		}
		desctext+=".";
		
		var args = {
				command : "add_badge",
				level_id: $(this.ui.levelid).val(),
				constraints: listConstraints,
				description: desctext
		};
		$(this.ui.postRequest).html("Adding...");
		$.ajax({
			type : 'POST',
			url : this.url,
			data : JSON.stringify(args),
			dataType : 'json',
			contentType : "application/json; charset=utf-8",
			success : this.sucess,
			error : this.error,
			async: true
		});
	},
	sucess: function(){
		var button = $(this.ui.postRequest);
		button.html("Added!");
		$(this.ui.row).val("");
		$(this.ui.levelid).val("");
		window.setTimeout(function(){
			button.html("Add Badge");	
		}, 2000);
	},
	error: function(){
		var button = $(this.ui.postRequest);
		button.html("Error!");
		$(this.ui.row).val("");
		$(this.ui.levelid).val("");
		window.setTimeout(function(){
			button.html("Add Badge");	
		}, 2000);
	}
});

//
//-- App init!
// 
DashBoard.addInitializer(function(options) {
//JSP Uses <% %> to render elements and this clashes with default underscore templates.
_.templateSettings = {
	interpolate : /\<\@\=(.+?)\@\>/gim,
	evaluate : /\<\@([\s\S]+?)\@\>/gim,
	escape : /\<\@\-(.+?)\@\>/gim
};
DashBoard.addRegions(options.regions);
DashBoard.AddRowView = new AddRowView();
DashBoard.mainWrapper.show(DashBoard.AddRowView);
});

//App Start
DashBoard.start({
	regions:{
		mainWrapper: '#dashboard-container'
	}
});