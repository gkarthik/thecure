define([
  //Libraries
	'jquery',
	'marionette',
	//Views
	'app/views/GenePoolItemView'
    ], function($, Marionette, GenePoolItemView) {
GenePoolCollectionView = Marionette.CollectionView.extend({
	itemView : GenePoolItemView,
	tagName: 'ul',
	className: 'list-group'
});

return GenePoolCollectionView;
});
