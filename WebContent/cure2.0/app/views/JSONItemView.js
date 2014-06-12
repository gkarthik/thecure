define([
	'jquery',
	'marionette',
	//Model
	'app/models/Node',
	//Templates
	'text!app/templates/JSONSplitNodeGeneSummary.html',	
	'text!app/templates/JSONSplitValueSummary.html',
	'text!app/templates/JSONSplitNodeCfSummary.html',
	'text!app/templates/CustomFeature_splitnode.html'
    ], function($, Marionette, Node, splitNodeGeneSummary, splitValueSummary, splitNodeCfSummary, customfeatureSummaryTmpl) {
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
		this.model.bind('change:showJSON', function() {
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
			$.getJSON("http://mygene.info/v2/gene/" + thisView.model.get("options").get('unique_id'),
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
		if(serialized_model.options.unique_id!=null && serialized_model.options.unique_id!="" && serialized_model.options.unique_id.indexOf("custom_feature_")==-1){
			if(serialized_model.options.kind == "split_node" && serialized_model.options.unique_id.indexOf("metabric") == -1) {
				return splitNodeGeneSummary({
					id: serialized_model.cid,
					name : name,
					summary : serialized_model.gene_summary,
					kind : serialized_model.options.kind
				});
			} else if (serialized_model.options.kind == "split_node" && serialized_model.options.unique_id.indexOf("metabric") != -1){
				return splitNodeCfSummary({
					id: serialized_model.cid,
					name : name,
					summary : serialized_model.gene_summary,
					kind : serialized_model.options.kind
				});
			} else {
				return splitValueSummary({
					id: serialized_model.cid,
					name : name,
					summary : serialized_model.gene_summary,
					kind : serialized_model.options.kind
				});
			} 
		} else {
			return customfeatureSummaryTmpl({
				id: serialized_model.cid,
				name : name,
				description : serialized_model.options.description,
				kind : serialized_model.options.kind
			});
		}
		
	},
	ShowJSON : function() {
		var description =null;
		var idFlag = 1;
		if(this.model.get('options').get('kind') == "split_node"){
			if(this.model.get('options').get('unique_id')!="" && this.model.get('options').get('unique_id')!=null && this.model.get('options').get('unique_id').indexOf("custom_feature_")==-1){
				if(this.model.get('options').get('unique_id').indexOf("metabric") == -1){
					idFlag = 0;
					this.getSummary();
				} else {
					description = this.model.get('options').get('description').replace("\\n","<br>");
				}
			} else {
				//For Custom Features
			}
		} else if(this.model.get('options').get('kind') == "split_value") {
				if(this.model.get('parentNode').get('options').get('unique_id').indexOf("metabric")!=-1){
					var summaryTextArray = this.model.get('parentNode').get('options').get('description').split("\n");
					for(var temp in summaryTextArray){
						if(summaryTextArray[temp].match(this.model.get('name').replace(/[\-\[\]\/\{\}\(\)\*\+\?\.\\\^\$\|]/g, "\\$&"))){//To escape +
							description = summaryTextArray[temp]; 
						}
					}
					if(description == null || description.split(" ").length < 3){
						description = this.model.get('name') + " " + this.model.get('parentNode').get('name');
					}
				} else {
					description = this.model.get('name')+" expression level of "+ this.model.get('parentNode').get('name');
				}
		} else {
			if(this.model.get('name')==Cure.posNodeName){
				description = "Predicted to survive beyond ten years.";
			} else {
				description = "<b>Not</b> Predicted to survive beyond ten years.";
			}
		}
		if(idFlag){
			var summary = {};
			summary.name = this.model.get('name');
			summary.summaryText = description;
			this.model.set("gene_summary",summary);
		}
		
		this.$el.find(this.ui.showjson).addClass("disabled");
		this.$el.find(this.ui.jsondata).css({
			'display' : 'block'
		});
		return 1;
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
