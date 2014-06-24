define([
	'jquery',
	'marionette',
	//Models
	'app/models/GeneItem',
	//Collections
	'app/collections/GeneCollection',
	//Views
	'app/views/GeneListCollectionView',
	//Layouts
	'app/views/layouts/PathwaySearchLayout',
	//Templates
	'text!app/templates/AggregateNodeLayout.html',
	'text!app/templates/GeneSummary.html',
	'text!app/templates/ClinicalFeatureSummary.html',
	//Plugins
	'myGeneAutocomplete',
	'jqueryui',
    ], function($, Marionette, GeneItem, GeneCollection, GeneCollectionView, PathwaySearchLayout, AggNodeTmpl, geneinfosummary, cfsummary) {
AggNodeLayout = Marionette.Layout.extend({
    template: AggNodeTmpl,
    className: "panel panel-default",
    initialize: function(){
    	_.bindAll(this,'buildAggNode');
    	this.listenTo(this,'onBeforeDestroy',this.destroyAllCollections);
    },
    destroyAllCollections: function(){
    	this.newGeneCollection.destroy();
    },
    url: base_url+"MetaServer",
    ui: {
    	gene_query: '#add_gene_to_aggNode_query',
    	nameInput: "#name_input",
    	descInput: "#desc_input",
    	classifierType: 'input:radio[name=classifierType]',
    	buildAggNode: '.build-aggnode',
    	msgWrapper: '.message-wrapper',
    	duplicateClassifier: '.duplicate-customclassifier-wrapper',
    	customfeature_query: "#add_customfeature_to_aggNode_query",
    	cf_query: "#add_cf_to_aggNode_query"
    	
    },
    events: {
    	'click .close-aggnode': 'closeAggNode',
    	'click .build-aggnode': 'sendRequest',
    	'click .duplicate_customclassifier':'addDuplicateEntry',
    	'click .open-pathway-search': 'openPathwaySearch'
    },
    regions: {
      GeneCollectionRegion: "#Aggnode_GeneCollectionRegion"
    },
    closeAggNode: function(){
    	Cure.sidebarLayout.AggNodeRegion.close();
    },
	openPathwaySearch: function(){
		Cure.PathwaySearchLayout = new PathwaySearchLayout({aggNode: true});
		Cure.sidebarLayout.PathwaySearchRegion.show(Cure.PathwaySearchLayout);
	},
	addToGeneCollection: function(models){
		this.newGeneCollection.add(models);
	},
    onRender: function(){
    	this.newGeneCollection = new GeneCollection();
    	var newGeneView = new GeneCollectionView({collection:this.newGeneCollection});
    	this.GeneCollectionRegion.show(newGeneView);
    },
    onShow: function(){
    	var thisUi = this.ui;
    	var thisCollection = this.newGeneCollection;
    	this.showCf();
    	$(this.ui.gene_query).genequery_autocomplete({
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
					var dropdown = $(thisUi.gene_query).data('my-genequery_autocomplete').bindings[0];
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
					thisCollection.add([{
						unique_id: ui.item.entrezgene,
						short_name: ui.item.symbol,
						long_name: ui.item.label
					}]);
					$(this).val("");
				}
			}
		});
		$(this.ui.gene_query).focus();
    },
    sendRequest: function(){
    	var uniqueIds = this.newGeneCollection.pluck("unique_id");
    	uniqueIds.splice(0,1);//Removing "Short Name"
    	if($(this.ui.nameInput).val()!="" && $(this.ui.descInput).val()!="" && uniqueIds.length>0){
    		$(this.ui.buildAggNode).val("Building classifier ... ");
    		$(this.ui.buildAggNode).addClass("disabled");
    		var args = {
        	        command : "custom_classifier_create",
        	        unique_ids: uniqueIds,
        	        name: $(this.ui.nameInput).val(),
        	        description: $(this.ui.descInput).val(),
        	        user_id: Cure.Player.get("id"),
        	        type: parseInt($(this.ui.classifierType).filter(":checked").val()),
        	        dataset: "metabric_with_clinical"
        	      };
        	console.log(args);
        	      $.ajax({
        	          type : 'POST',
        	          url : this.url,
        	          data : JSON.stringify(args),
        	          dataType : 'json',
        	          contentType : "application/json; charset=utf-8",
        	          success : this.buildAggNode,
        	          error: this.error
        	        });
    	}
    },
    buildAggNode: function(data){
    	$(this.ui.buildAggNode).removeClass("disabled");
		$(this.ui.buildAggNode).val("Build Classifier");
			if(data.exists==true){
				$(this.ui.msgWrapper).html("<span class='text-danger error-message'>"+data.message+"</span><br><p class='bg-info duplicate_customclassifier' data-name='"+data.name+"' data-description='"+data.description+"' data-id='"+data.id+"'>"+data.name+": "+data.description+"</p>");
			} else {
				this.addCustomClassifier(data);
			}
    },
    addDuplicateEntry: function(e){
    	var data = $(e.target).data();
    	console.log(data);
    	this.addCustomClassifier(data);
    },
    addCustomClassifier: function(data){
    	var kind_value = "";
    	var model = this.model;
			try {
				kind_value = model.get("options").get('kind');
			} catch (exception) {
			}
			if (kind_value == "leaf_node") {
					if(model.get("options")){
						model.get("options").unset("split_point");
					}
					
					if(model.get("distribution_data")){
						model.get("distribution_data").set({
							"range": -1
						});
					}
				model.set("previousAttributes", model.toJSON());
				model.set("name", data.name);
				model.set('accLimit', 0, {silent:true});
				
				var index = Cure.CollaboratorCollection.pluck("id").indexOf(Cure.Player.get('id'));
				var newCollaborator;
				if(index!=-1){
					newCollaborator = Cure.CollaboratorCollection.at(index);
				} else {
					newCollaborator = new Collaborator({
						"name": cure_user_name,
						"id": Cure.Player.get('id'),
						"created" : new Date()
					});
					Cure.CollaboratorCollection.add(newCollaborator);
					index = Cure.CollaboratorCollection.indexOf(newCollaborator);
				}
				model.get("options").set({
					"unique_id" : "custom_classifier_"+data.id,
					"kind" : "split_node",
					"full_name" : '',
					"description" : data.description
				});
			} else {
				new Node({
					'name' : data.name,
					"options" : {
						"unique_id" : "custom_classifier_"+data.id,
						"kind" : "split_node",
						"full_name" : '',
						"description" : data.description
					}
				});
			}
			Cure.PlayerNodeCollection.sync();
			this.closeAggNode();
    },
	showCf: function(){
		var thisUi = this.ui;
		var thisCollection = this.newGeneCollection;
		
		//Clinical Features Autocomplete
		var availableTags = Cure.ClinicalFeatureCollection.toJSON();
		
		$(this.ui.cf_query).autocomplete({
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
					var dropdown = $(thisUi.cf_query).data('ui-autocomplete').bindings[1];
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
				console.log(ui.item);
				if(ui.item.short_name != undefined){//To ensure "no gene name has been selected" is not accepted.
						$("#SpeechBubble").remove();
						thisCollection.add([{
							unique_id: ui.item.unique_id,
							short_name: ui.item.short_name.replace(/_/g," "),
							long_name: ui.item.description
						}]);
						$(this).val("");
					}
			},
		}).bind('focus', function(){ $(this).autocomplete("search"); } )
			.data("ui-autocomplete")._renderItem = function (ul, item) {
		    return $("<li></li>")
	        .data("item.autocomplete", item)
	        .append("<a>" + item.label + "</a>")
	        .appendTo(ul);
	    };
	},
    error : function(data) {
		Cure.utils
    .showAlert("<strong>Server Error</strong><br>Please try saving again in a while.", 0);
	}
});
return AggNodeLayout;
});
