define([
	'jquery',
  'backbone',
	'app/models/Badge'
    ], function($, Backbone, Badge) {
	BadgeCollection = Backbone.Collection.extend({
	model: Badge,
	initialize: function(){
		
	}
	
});
return BadgeCollection;
});