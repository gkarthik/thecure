define([
	'jQuery',
	'marionette',
	'app/views/JSONItemView'
    ], function($, Marionette, JSONItemView) {
JSONCollectionView = Marionette.CollectionView.extend({
	itemView : JSONItemView,
	collection : NodeCollection,
	initialize : function() {
		this.collection.bind('add', this.render);
		this.collection.bind('remove', this.render);
	}
});

return JSONItemView;
});

