define([
	'jquery',
	'marionette',
	//Model
	'app/models/Node',
	//Templates
	'text!app/templates/JSONSplitNodeGeneSummary.html',	
	'text!app/templates/JSONSplitValueSummary.html',
	'text!app/templates/JSONSplitNodeCfSummary.html',
	'text!app/templates/CustomSplitNode.html',
	'text!app/templates/ClassifierInString.html',
	'text!app/templates/TreeDetails.html'
    ], function($, Marionette, Node, splitNodeGeneSummary, splitValueSummary, splitNodeCfSummary, customNodeSummaryTmpl, classifierInString, treeDetails) {
JSONItemView = Marionette.ItemView.extend({
	model : Node,
	ui : {
		jsondata : ".jsonview_data",
		showjson : ".showjson",
		sampleTable: '#sample-wrapper',
		featuresInClassifier: ".features-in-classifier",
		treeStructure: ".tree-structure .tree-details",
		SvgPreview: ".tree-structure svg"
	},
	events : {
		'click .showjson' : 'ShowJSON',
		'click button.close' : 'HideJSON',
		'click #text-exp': 'testExp',
		'click .close-json-view': 'HideView'
	},
	tagName : "tr",
	url:base_url+"MetaServer",
	initialize : function() {
		_.bindAll(this, 'getSummary', 'ShowJSON', 'HideJSON', 'renderTestCase', 'drawTreeStructure', 'HideView');
		this.listenTo(this.model,'change:gene_summary', this.render);
		this.model.bind('change:showJSON', function() {
			if (this.model.get('showJSON') != 0) {
				this.ShowJSON();
			} else {
				this.HideJSON();
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
		if(this.model.get('options').toJSON().hasOwnProperty("unique_id")){
			if(this.model.get('options').get('unique_id').indexOf("custom_classifier")!=-1){
				this.getCustomClassifierFeatures();
			} else if(this.model.get('options').get('unique_id').indexOf("custom_tree")!=-1) {
				this.getCustomTreeStructure();
			}
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
		if(serialized_model.options.hasOwnProperty("unique_id") && serialized_model.options.unique_id.indexOf("custom_")==-1){
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
	getCustomTreeStructure: function(){
		var args = {
				command:"get_tree_by_id",
				treeid: this.model.get('options').get('unique_id').replace("custom_tree_",""),
    	        dataset: "metabric_with_clinical"
    	      };
    	      $.ajax({
    	          type : 'POST',
    	          url : this.url,
    	          data : JSON.stringify(args),
    	          dataType : 'json',
    	          contentType : "application/json; charset=utf-8",
    	          success : this.drawTreeStructure,
    	          error: this.error
    	});
	},
	drawTreeStructure: function(data){
		$(this.ui.treeStructure).html(treeDetails(data.trees[0]));
		var treestruct = data.trees[0].json_tree.treestruct;
		var id = $(this.ui.SvgPreview).attr('id');
		var svg = d3.select("#"+id)
			.attr("width",300)
			.attr("height",300)
			.append("g")
			.attr("transform","translate(0,20)");
		var cluster = d3.layout.tree().size([ 250, 250 ]);
		var diagonal = d3.svg.diagonal().projection(function(d) { return [d.x, d.y]; });
		var json = JSON.stringify(treestruct);
		var nodes = cluster.nodes(JSON.parse(json)),
    links = cluster.links(nodes);
	  var link = svg.selectAll(".link")
	      .data(links)
	    .enter().append("path")
	      .attr("class", "link")
	      .attr("d", diagonal)
	      .style("stroke","steelblue")
	      .style("stroke-width", "2");
	
	  var node = svg.selectAll(".node")
	      .data(nodes)
	    .enter().append("g")
	      .attr("class", "node")
	      .attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; });
	
	  node.append("text")
	      .attr("dx", function(d) { return d.children ? -8 : 8; })
	      .attr("dy", 3)
	      .style("text-anchor", "middle")
	      .text(function(d) { return d.name; });
	},
	getCustomClassifierFeatures: function(){
		var thisView = this;
		var args = {
				command:"custom_classifier_getById",
				id: this.model.get('options').get('unique_id').replace("custom_classifier_",""),
    	        dataset: "metabric_with_clinical"
    	      };
    	      $.ajax({
    	          type : 'POST',
    	          url : this.url,
    	          data : JSON.stringify(args),
    	          dataType : 'json',
    	          contentType : "application/json; charset=utf-8",
    	          success : function(data){
    	        	  console.log(classifierInString(data));
    	        	  $(thisView.ui.featuresInClassifier).html(classifierInString(data));
    	          },
    	          error: this.error
    	});
	},
	ShowJSON : function() {
		var description =null;
		var idFlag = 1;
		if(this.model.get('options').get('kind') == "split_node"){
			if(this.model.get('options').get('unique_id')!="" && this.model.get('options').get('unique_id')!=null && this.model.get('options').get('unique_id').indexOf("custom_")==-1){
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
			this.model.set("showJSON", 1);
		}
		
		this.$el.find(this.ui.showjson).addClass("disabled");
		this.$el.find(this.ui.jsondata).css({
			'display' : 'block'
		});
		return 1;
	},
	HideView: function(e){
		e.preventDefault();
		this.HideJSON();
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
