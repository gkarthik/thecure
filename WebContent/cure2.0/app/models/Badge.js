define([
    	'backbone',
    	'backboneRelational'
    ], function(Backbone) {
Badge = Backbone.RelationalModel.extend({
	defaults: {
		id: "",
		description: ""
	}
});
return Badge;
});
