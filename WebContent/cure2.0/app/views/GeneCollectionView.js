define([
  //Libraries
	'jquery',
	'marionette',
	//Views
	'app/views/GeneItemView'
    ], function($, Marionette, GeneItemView) {
GeneCollectionView = Marionette.CollectionView.extend({
	itemView : GeneItemView,
	tagName: 'table',
	className: 'table table-condensed'
});

return GeneCollectionView;
});
