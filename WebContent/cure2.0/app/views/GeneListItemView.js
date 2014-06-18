define([
  //Libraries
	'jquery',
	'marionette',
	'backbone',
	//Templates
	'text!app/templates/GeneListItem.html',
	'jqueryui'
    ], function($, Marionette, Backbone, GeneItemTmpl) {
GeneItemView = Marionette.ItemView.extend({
	tagName: 'tr',
	events: {
		'click .delete': 'deleteThisItem',
	},
	ui: {
		'keepAll': '.keepAll',
		'keepInCollection': '.keepInCollection'
	},
	template: GeneItemTmpl,
	deleteThisItem: function(){
		this.model.destroy();
	}
});

return GeneItemView;
});
