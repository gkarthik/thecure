define([
	'jquery',
	'marionette',
	//Models
	'app/models/GeneItem',
	//Collections
	'app/collections/GeneCollection',
	//Views
	'app/views/GeneListCollectionView',
	//Templates
	'text!app/templates/AggregateNodeLayout.html',
	'text!app/templates/GeneSummary.html',
	//Plugins
	'myGeneAutocomplete',
	'jqueryui',
    ], function($, Marionette, GeneItem, GeneCollection, GeneCollectionView, AggNodeTmpl, geneinfosummary) {
AggNodeLayout = Marionette.Layout.extend({
    template: AggNodeTmpl,
    className: "panel panel-default",
    initialize: function(){
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
    },
    events: {
    	'click .close-aggnode': 'closeAggNode',
    	'click .build-aggnode': 'sendRequest'
    },
    regions: {
      GeneCollectionRegion: "#Aggnode_GeneCollectionRegion"
    },
    closeAggNode: function(){
    	Cure.sidebarLayout.AggNodeRegion.close();
    },
    onRender: function(){
    	this.newGeneCollection = new GeneCollection();
    	var newGeneView = new GeneCollectionView({collection:this.newGeneCollection});
    	this.GeneCollectionRegion.show(newGeneView);
    },
    onShow: function(){
    	var thisUi = this.ui;
    	var thisCollection = this.newGeneCollection;
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
						id: ui.item.entrezgene,
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
    	var uniqueIds = this.newGeneCollection.pluck("id");
    	uniqueIds.splice(0,1);//Removing "Short Name"
    	if($(this.ui.nameInput).val()!="" && $(this.ui.descInput).val()!="" && uniqueIds.length>0){
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
        	          success : this.buildAggNode
        	        });
    	}
    },
    buildAggNode: function(data){
    	console.log(data);
    }
});
return AggNodeLayout;
});
