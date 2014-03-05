define([
  //Libraries
	'jquery',
	'marionette',
	//Collectons
	'app/collections/CollaboratorCollection',
	//Views
	'app/views/CollaboratorView'
    ], function($, Marionette, CollaboratorCollection, CollaboratorView) {
	CollaboratorCollectionView = Marionette.CollectionView.extend({
	itemView : CollaboratorView,
	collection : CollaboratorCollection,
	tagName: 'table',
	initialize : function() {
		
	}
});

return CollaboratorCollectionView;
});

