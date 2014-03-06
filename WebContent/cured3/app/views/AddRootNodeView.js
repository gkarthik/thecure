define([
  //Libraries
	'jquery',
	'marionette',
	//Models
	'app/models/Node',
	'app/models/Collaborator',
	//Templates
	'text!app/templates/GeneSummary.html',
	'text!app/templates/ClinicalFeatureSummary.html',
	'text!app/templates/AddNode.html',
	//Plugins
	'myGeneAutocomplete',
	'jqueryui'
    ], function($, Marionette, Node, Collaborator, geneinfosummary, cfsummary, AddNodeTemplate) {
AddRootNodeView = Marionette.ItemView.extend({
	initialize : function() {
	},
	ui : {
		'input' : '.mygene_query_target',
		'cfWrapper': '#mygenecf_wrapper'
	},
	events:{
		'click #showCf': 'showCf',
		'click #hideCf': 'hideCf'
	},
	showCf: function(){
		$("#mygeneinfo_wrapper").hide();
		if (this.model) {
			var model = this.model;
		}
		
		//Clinical Features Autocomplete
		var availableTags = Cure.ClinicalFeatureCollection.toJSON();
		
		this.$el.find('#cf_query').autocomplete({
			source : availableTags,
			minLength: 0,
			open: function(event){
				var scrollTop = $(event.target).offset().top-400;
				$("html, body").animate({scrollTop:scrollTop}, '500');
			},
			close: function(){
				$(this).val("");
			},
			minLength: 0,
			focus: function( event, ui ) {
				focueElement = $(event.currentTarget);//Adding PopUp to .ui-auocomplete
				if($("#SpeechBubble")){
					$("#SpeechBubble").remove();
				}
				focueElement.append("<div id='SpeechBubble'></div>")
					var html = cfsummary({
						long_name : ui.item.long_name,
						description : ui.item.description
					});
					var dropdown = $("#cf_query").data('ui-autocomplete').bindings[1];
					var offset = $(dropdown).offset();
					var uiwidth = $(dropdown).width();
					var width = 0.9 * (offset.left);
					var left = 0;
					if(window.innerWidth - (offset.left+uiwidth) > offset.left ){
						left = offset.left+uiwidth+10;
						width = 0.9 * (window.innerWidth - (offset.left+uiwidth));
					}
					$("#SpeechBubble").css({
						"top": "10%",
						"left": left,
						"height": "50%",
						"width": width,
						"display": "block"
					});
					$("#SpeechBubble").html(html);
					$("#SpeechBubble .summary_header").css({
						"width": (0.9*width)
					});
					$("#SpeechBubble .summary_content").css({
						"margin-top": $("#SpeechBubble .summary_header").height()+10
					});
			},
			search: function( event, ui ) {
				$("#SpeechBubble").remove();
			},
			select : function(event, ui) {
				if(ui.item.long_name != undefined){//To ensure "no gene name has been selected" is not accepted.
					$("#SpeechBubble").remove();
					var kind_value = "";
					try {
						kind_value = model.get("options").kind;
					} catch (exception) {
					}
					if (kind_value == "leaf_node") {
						model.set("previousAttributes", model.toJSON());
						model.set("name", ui.item.short_name.replace(/_/g," "));
						model.set('accLimit', 0, {silent:true});
						model.set('modifyAccLimit', 1, {silent:true});
						
						if(Cure.utils.isJSON(ui.item.description)){
							model.set("options", {
								id : ui.item.unique_id,
								"unique_id" : ui.item.unique_id,
								"kind" : "split_node",
								"full_name" : ui.item.long_name,
								"description" : ui.item.description
							});
						} else {
							model.set("options", {
								id : ui.item.unique_id,
								"unique_id" : ui.item.unique_id,
								"kind" : "split_node",
								"full_name" : ui.item.long_name,
								"description" : ui.item.description
							});
						}
					} else {
						var newNode = new Node({
							'name' : ui.item.short_name.replace(/_/g," "),
							"options" : {
								id : ui.item.unique_id,
								"unique_id" : ui.item.unique_id,
								"kind" : "split_node",
								"full_name" : ui.item.long_name,
								"description" : ui.item.description
							}
						});
						newNode.set("cid", newNode.cid);
					}
					if (Cure.MyGeneInfoRegion) {
						Cure.MyGeneInfoRegion.close();
					}
					Cure.PlayerNodeCollection.sync();
				}
			},
		}).data("ui-autocomplete")._renderItem = function (ul, item) {
		    return $("<li></li>")
	        .data("item.autocomplete", item)
	        .append("<a>" + item.label + "</a>")
	        .appendTo(ul);
	    };
	    
	    this.$el.find('#cf_query').focus(function(){
	    	$(this).autocomplete("search", "");
	    });	
		$("#mygenecf_wrapper").show();
	},
	hideCf: function(){
		$("#mygenecf_wrapper").hide();
		$("#mygeneinfo_wrapper").show();
	},
	template : AddNodeTemplate,
	render : function() {
		if (this.model) {
			var model = this.model;
		}
		var html_template = AddNodeTemplate;
		this.$el.html(AddNodeTemplate);
		
		this.$el.find('#gene_query').genequery_autocomplete({
			open: function(event){
				var scrollTop = $(event.target).offset().top-400;
				$("html, body").animate({scrollTop:scrollTop}, '500');
			},
			minLength: 1,
			focus: function( event, ui ) {
				focueElement = $(event.currentTarget);//Adding PopUp to .ui-auocomplete
				if($("#SpeechBubble")){
					$("#SpeechBubble").remove();
				}
				focueElement.append("<div id='SpeechBubble'></div>")
				$.getJSON("http://mygene.info/v2/gene/"+ui.item.id,function(data){
					var summary = {
							summaryText: data.summary,
							goTerms: data.go,
							generif: data.generif,
							name: data.name
					};
					var html = geneinfosummary({
						symbol : data.symbol,
						summary : summary
					}, {
						variable : 'args'
					});
					var dropdown = $("#gene_query").data('my-genequery_autocomplete').bindings[0];
					var offset = $(dropdown).offset();
					var uiwidth = $(dropdown).width();
					var width = 0.9 * (offset.left);
					var left = 0;
					if(window.innerWidth - (offset.left+uiwidth) > offset.left ){
						left = offset.left+uiwidth+10;
						width = 0.9 * (window.innerWidth - (offset.left+uiwidth));
					}
					$("#SpeechBubble").css({
						"top": "10%",
						"left": left,
						"height": "50%",
						"width": width,
						"display": "block"
					});
					$("#SpeechBubble").html(html);
					$("#SpeechBubble .summary_header").css({
						"width": (0.9*width)
					});
					$("#SpeechBubble .summary_content").css({
						"margin-top": $("#SpeechBubble .summary_header").height()+10
					});
				});
			},
			search: function( event, ui ) {
				$("#SpeechBubble").remove();
			},
			select : function(event, ui) {
				if(ui.item.name != undefined){//To ensure "no gene name has been selected" is not accepted.
					$("#SpeechBubble").remove();
					var kind_value = "";
					try {
						kind_value = model.get("options").kind;
					} catch (exception) {
					}

					if (kind_value == "leaf_node") {
						model.set("previousAttributes", model.toJSON());
						model.set("name", ui.item.symbol);
						model.set('accLimit', 0, {silent:true});
						model.set('modifyAccLimit', 1, {silent:true});
						model.set("options", {
							id : ui.item.id,
							"kind" : "split_node",
							"full_name" : ui.item.name
						});
					} else {
						var newNode = new Node({
							'name' : ui.item.symbol,
							"options" : {
								id : ui.item.id,
								"kind" : "split_node",
								"full_name" : ui.item.name
							}
						});
						newNode.set("cid", newNode.cid);
					}
					if (Cure.MyGeneInfoRegion) {
						Cure.MyGeneInfoRegion.close();
					}
					Cure.PlayerNodeCollection.sync();
				}
			}
		});

	}
});


return AddRootNodeView;
});
