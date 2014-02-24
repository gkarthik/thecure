define([
  //Libraries
	'jquery',
	'marionette',
	//Collectons
	'app/collections/NodeCollection',
	//Views
	'app/views/JSONItemView'
    ], function($, Marionette, NodeCollection, JSONItemView) {
JSONCollectionView = Marionette.CollectionView.extend({
	itemView : JSONItemView,
	collection : NodeCollection,
	initialize : function() {
		this.collection.bind('add', this.render);
		this.collection.bind('remove', this.render);
	}
});

return JSONCollectionView;
});

