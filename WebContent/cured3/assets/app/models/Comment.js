define([
    	'backbone',
    	'backboneRelational'
    ], function(Backbone) {
Comment = Backbone.RelationalModel.extend({
	defaults: {
		content: "",
		editView: 0
	}
});
return Comment;
});