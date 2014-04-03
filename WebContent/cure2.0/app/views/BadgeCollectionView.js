define([
  //Libraries
	'jquery',
	'marionette',
	//Views
	'app/views/BadgeItemView'
    ], function($, Marionette, BadgeItemView) {
BadgeCollectionView = Marionette.CollectionView.extend({
	itemView : BadgeItemView,
	tagName: 'table',
	className: 'table',
	initialize : function() {
		
	}
});

return BadgeCollectionView;
});
