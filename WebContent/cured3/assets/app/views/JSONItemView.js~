define([
	'jQuery',
	'marionette'
    ], function($, Marionette) {
JSONItemView = Marionette.ItemView.extend({
	model : Node,
	ui : {
		jsondata : ".jsonview_data",
		showjson : ".showjson"
	},
	events : {
		'click .showjson' : 'ShowJSON',
		'click button.close' : 'HideJSON'
	},
	tagName : "tr",
	initialize : function() {
		_.bindAll(this, 'getSummary', 'ShowJSON', 'HideJSON');
		this.model.bind('change', this.render);
		this.model.on('change:showJSON', function() {
			if (this.model.get('showJSON') != 0) {
				this.ShowJSON();
			}
		}, this);
		var thisView = this;
		$(document).mouseup(function(e){
			var classToclose = $(".jsonview_data");
			if (!classToclose.is(e.target)	&& classToclose.has(e.target).length == 0) 
			{
				thisView.HideJSON();
			}
		});
	},
	onRender : function() {
		if (this.model.get('showJSON') != 0) {
			this.ShowJSON();
		}
	},
	getSummary : function() {
		var thisView = this,
			summary = this.model.get("gene_summary").summaryText || "";
		if (summary.length == 0) {
			$.getJSON("http://mygene.info/v2/gene/" + thisView.model.get("options").id,
					function(data) {
						var summary = {
							"summaryText" : data.summary,
							"goTerms" : data.go,
							"generif" : data.generif,
							"name" : data.name
						};
						thisView.model.set("gene_summary", summary);
						thisView.model.set("showJSON", 1);
					});
		}
	},
	template : function(serialized_model) {
		var name = serialized_model.name;
		var options = serialized_model.options;
		if(serialized_model.options.kind == "split_node" && serialized_model.options.id.indexOf("metabric") == -1) {
			return _.template(shownode_html, {
				name : name,
				summary : serialized_model.gene_summary,
				kind : serialized_model.options.kind
			}, {
				variable : 'args'
			});
		} else if (serialized_model.options.kind == "split_node" && serialized_model.options.id.indexOf("metabric") != -1){
			return _.template(showsplitnodecf_html, {
				name : name,
				summary : serialized_model.gene_summary,
				kind : serialized_model.options.kind
			}, {
				variable : 'args'
			});
		} else {
			return _.template(showsplitvaluenode_html, {
				name : name,
				summary : serialized_model.gene_summary,
				kind : serialized_model.options.kind
			}, {
				variable : 'args'
			});
		}
	},
	ShowJSON : function() {
		if(this.model.get('options').kind == "split_node"){
			if(this.model.get('options').id.indexOf("metabric") == -1){
				this.getSummary();
			} else {
				var summary = {};
				summary.name = this.model.get('name');
				summary.summaryText = this.model.get('options').description;
				this.model.set("gene_summary",summary);
			}
		} else {
			if(Cure.isJSON(this.model.get('parentNode').get('options').description)){
				var json_string = JSON.parse(this.model.get('parentNode').get('options').description);
				var summary = {};
				summary.name = this.model.get('name');
				summary.summaryText = json_string[this.model.get('name')];
				this.model.set("gene_summary",summary);
			}
		}
		this.$el.find(this.ui.showjson).addClass("disabled");
		this.$el.find(this.ui.jsondata).css({
			'display' : 'block'
		});
	},
	HideJSON : function() {
		$(this.ui.jsondata).css({
			'display' : 'none'
		});
		$(this.ui.showjson).removeClass("disabled");
		this.model.set("showJSON", 0);
	}
});

return JSONItemView;
});
