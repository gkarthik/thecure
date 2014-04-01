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
		var constraints = {};
		$(this.ui.row).each(function(){
			constraints[$(this).attr("id")] = $(this).val();
		});
		var args = {
				command : "add_badge",
				level_id: $(this.ui.levelid).val(),
				constraints: constraints
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