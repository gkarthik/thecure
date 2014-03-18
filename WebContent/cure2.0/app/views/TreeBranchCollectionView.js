define([
  //Libraries
	'jquery',
	'marionette',
	//Collectons
	'app/collections/TreeBranchCollection',
	//Views
	'app/views/TreeBranchView'
    ], function($, Marionette, TreeBranchCollection, TreeBranchView) {
TreeBranchCollectionView = Marionette.CollectionView.extend({
	itemView : TreeBranchView,
	collection : TreeBranchCollection,
	initialize : function() {
		this.collection.bind('add', this.render);
		this.collection.bind('remove', this.render);
	}
});
return TreeBranchCollectionView;
});

