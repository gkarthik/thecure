define([
	'jquery',
	'marionette',
	//Views
	'app/views/NodeView',
	'app/views/layouts/emptyLayout'
    ], function($, Marionette, NodeView, emptyLayout) {
NodeCollectionView = Marionette.CollectionView.extend({
	// -- View to manipulate and display list of all nodes in collection
	itemView : NodeView,
	emptyView : emptyLayout,
	initialize : function() {

	}
});

return NodeCollectionView;
});
