define([
	'jquery',
	'marionette',
	//Views
	'app/views/GeneCollectionView',
	'app/views/GenePoolCollectionView',
	'app/views/layouts/GenePoolLayout',
	//Templates
	'text!app/templates/PathwayLayout.html'
    ], function($, Marionette, GeneCollectionView, GenePoolCollectionView, GenePoolLayout, PathwayLayoutTmpl) {
PathwayLayout = Marionette.Layout.extend({
    template: PathwayLayoutTmpl,
    className: "panel panel-default",
    url: base_url+"MetaServer",
    ui: {
  		"pathwaysearch": "#pathwaysearch_query"
    },
    events: {
    	'click .close-pathway-search': 'closePathwaySearch',
    	'click #add-to-gene-pool': 'addGenestoPool'
    },
    regions: {
      GeneCollectionRegion: "#GeneCollectionRegion"
    },
    closePathwaySearch: function(){
    	Cure.sidebarLayout.PathwaySearchRegion.close();
    },
    initialize: function(options){
    	this.aggNode = options.aggNode;
    },
    onRender: function(){
    	Cure.GeneCollection.reset();
    	Cure.GenePoolRegion.close();
    	Cure.GeneCollectionView = new GeneCollectionView({
      	collection: Cure.GeneCollection
      });
      this.GeneCollectionRegion.show(Cure.GeneCollectionView);
      
      var thisURL = this.url;
      $(this.ui.pathwaysearch).autocomplete({
  			source: function( request, response ) {
  					var args = {
    	        command : "search_pathways",
    	        query: request.term
    	      };
    	      $.ajax({
    	          type : 'POST',
    	          url : thisURL,
    	          data : JSON.stringify(args),
    	          dataType : 'json',
    	          contentType : "application/json; charset=utf-8",
    	          success : function(data){
    	          	response( $.map( data, function( item ) {
    	          		return {
    	          		  label: item,
    	          		  value: item
    	          	  };
    	          	}));
    	        }
    	      });
  				},
  				minLength: 3,
  				select: function( event, ui ) {
  					var args = {
  	  	        command : "get_genes_of_pathway",
  	  	        pathway_name:	ui.item.value
  	  	      };
  	  	      $.ajax({
  	  	          type : 'POST',
  	  	          url : thisURL,
  	  	          data : JSON.stringify(args),
  	  	          dataType : 'json',
  	  	          contentType : "application/json; charset=utf-8",
  	  	          success : function(data){
  	  	          	Cure.GeneCollection.reset();
  	  	          	Cure.GeneCollection.add(data);
  	  	          }
  	  	      });
  				},
  				open: function() {
  				$( this ).removeClass( "ui-corner-all" ).addClass( "ui-corner-top" );
  				},
  				close: function() {
  				$( this ).removeClass( "ui-corner-top" ).addClass( "ui-corner-all" );
  				}
  			});
    },
    addGenestoPool: function(){
    	var model;
    	for(var i=0;i<Cure.GeneCollection.length;i++){
    		model = Cure.GeneCollection.models[i];
    		if(!model.get('keepInCollection')){
    			model.destroy();
    			i--;
    		}
    	}
    	Cure.sidebarLayout.PathwaySearchRegion.close();
    	console.log(this.aggNode);
    	if(this.aggNode){
    		var layout = Cure.sidebarLayout.AggNodeRegion.currentView;
    		layout.addToGeneCollection(Cure.GeneCollection.toArray());
    		Cure.GeneCollection.reset();
    	} else {
    		var genePoolLayout = new GenePoolLayout();
    		Cure.GenePoolRegion.show(genePoolLayout);
    	}
    }
});
return PathwayLayout;
});
