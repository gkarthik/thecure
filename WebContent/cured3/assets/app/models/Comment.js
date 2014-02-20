define([
    	'backboneRelational'
    ], function(BackboneRelational) {
Comment = BackboneRelational.extend({
	defaults: {
		content: "",
		editView: 0
	}
});
return Comment;
});
