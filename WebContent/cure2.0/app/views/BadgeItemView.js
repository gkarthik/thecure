define([
	'jquery',
	'marionette',
	//Model
	'app/models/Badge',
	//Templates
	'text!app/templates/Badge.html'
    ], function($, Marionette, Badge,badgeTmpl) {
BadgeItemView = Marionette.ItemView.extend({
	model : Badge,
	tagName : "tr",
	initialize : function() {
		
	},
	template: badgeTmpl
});

return BadgeItemView;
});
