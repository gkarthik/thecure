define([
  //Libraries
	'jquery',
	'marionette',
	//Views
	'app/views/GeneListItemView'
    ], function($, Marionette, GeneItemView) {
GeneCollectionView = Marionette.CollectionView.extend({
	itemView : GeneItemView,
	tagName: 'table',
	className: 'table'
});

return GeneCollectionView;
});
