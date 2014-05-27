define([
  //Libraries
	'jquery',
	'marionette',
	'backbone',
	//Templates
	'text!app/templates/GeneItem.html'
    ], function($, Marionette, Backbone, GeneItemTmpl) {
GeneItemView = Marionette.ItemView.extend({
	tagName: 'tr',
	initialize : function() {
		this.model.bind('change', this.render);
	},
	template: GeneItemTmpl
});

return GeneItemView;
});
