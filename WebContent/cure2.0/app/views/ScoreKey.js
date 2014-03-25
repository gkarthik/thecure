define([
  //Libraries
	'jquery',
	'marionette',
	'backbone',
	//Templates
	'text!app/templates/ScoreKey.html'
    ], function($, Marionette, Backbone, ScoreKeyTemplate) {
ScoreKeyView = Marionette.ItemView.extend({
	tagName: 'table',
	className: 'table',
	initialize : function() {
		this.model.bind('change', this.render);
	},
	template: ScoreKeyTemplate
});

return ScoreKeyView;
});
