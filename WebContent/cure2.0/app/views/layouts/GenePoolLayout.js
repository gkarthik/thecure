define([
	'jquery',
	'marionette',
	//Views
	'app/views/GenePoolCollectionView',
	//Templates
	'text!app/templates/GenePoolLayout.html',
	'jqueryui'
    ], function($, Marionette, GenePoolCollectionView, GenePoolLayoutTmpl) {
GenePoolLayout = Marionette.Layout.extend({
   template: GenePoolLayoutTmpl,
   regions: {
	   geneRegion: ".available-genes"
   },
   events: {
	   'click .close-gene-pool': 'closeView'
   },
   onRender: function(){
	   
	   var newGenePoolCollectionView = new GenePoolCollectionView({
	      	collection: Cure.GeneCollection
	      });
	   this.geneRegion.show(newGenePoolCollectionView);
   },
   onShow: function(){
	   this.$el.draggable({handle: '.drag-pool'});
   },
   closeView: function(){
	   Cure.GenePoolRegion.close();
   }
});
return GenePoolLayout;
});
