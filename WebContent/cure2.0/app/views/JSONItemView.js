define([
	'jquery',
	'marionette',
	//Model
	'app/models/Node',
	//Templates
	'text!app/templates/JSONSplitNodeGeneSummary.html',	
	'text!app/templates/JSONSplitValueSummary.html',
	'text!app/templates/JSONSplitNodeCfSummary.html',
	'text!app/templates/CustomSplitNode.html'
    ], function($, Marionette, Node, splitNodeGeneSummary, splitValueSummary, splitNodeCfSummary, customNodeSummaryTmpl) {
JSONItemView = Marionette.ItemView.extend({
	model : Node,
	ui : {
		jsondata : ".jsonview_data",
		showjson : ".showjson",
		sampleTable: '#sample-wrapper'
	},
	events : {
		'click .showjson' : 'ShowJSON',
		'click button.close' : 'HideJSON',
		'click #text-exp': 'testExp'
	},
	tagName : "tr",
	url:base_url+"MetaServer",
	initialize : function() {
		_.bindAll(this, 'getSummary', 'ShowJSON', 'HideJSON', 'renderTestCase');
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
	testExp: function(){
		var args = {
    	        command : "custom_feature_testcase",
    	        id: this.model.get('options').get('unique_id'),
    	        dataset: "metabric_with_clinical"
    	      };
    	      $.ajax({
    	          type : 'POST',
    	          url : this.url,
    	          data : JSON.stringify(args),
    	          dataType : 'json',
    	          contentType : "application/json; charset=utf-8",
    	          success : this.renderTestCase,
    	          error: this.error
    	});
	},
	renderTestCase: function(data){
		var html = "<h3>Sample No: "+data.sample+"</h3>"; 
		html += "<table id='sample-data' class='table pull-right'><tr><th>Attribute</th><th>Value</th></tr>";
		for(var temp in data.features){
			html+="<tr><td>";
			html+=temp;
			html+="</td><td>";
			html+=data.features[temp];
			html+="</td></tr>";
		}
		html+="<tr class='info'><td>"+this.model.get('name')+"</td><td>"+data.custom_feature+"</td></tr></table>";
		$(this.ui.sampleTable).html(html);
	},
	error : function(data) {
		Cure.utils
    .showAlert("<strong>Server Error</strong><br>Please try saving again in a while.", 0);
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
		if(serialized_model.options.hasOwnProperty("unique_id") && serialized_model.options.unique_id.indexOf("custom_feature_")==-1 && serialized_model.options.unique_id.indexOf("custom_classifier_")==-1 ){
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
		} else if(!serialized_model.options.hasOwnProperty("unique_id")) {
			return splitValueSummary({
				id: serialized_model.cid,
				name : name,
				summary : serialized_model.gene_summary,
				kind : serialized_model.options.kind
			});
		} else {
			return customNodeSummaryTmpl({
				id: serialized_model.cid,
				name : name,
				description : serialized_model.options.description,
				kind : serialized_model.options.kind,
				options: options
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
