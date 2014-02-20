define([
	'jQuery',
	'marionette',
	'app/views/NodeView'
    ], function($, Marionette, NodeView) {
NodeCollectionView = Marionette.CollectionView.extend({
	// -- View to manipulate and display list of all nodes in collection
	itemView : NodeView,
	emptyView : emptyLayout,
	initialize : function() {

	}
});

return NodeCollectionView;
});
