define([
      	'backbone',
      	'backboneRelational'
    ], function(Backbone) {
	Collaborator = Backbone.RelationalModel.extend({
	defaults : {
		'name' : '',
		'id': 0,
		'created': null
	}
});

return Collaborator;
});
