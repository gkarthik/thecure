define([
	'jquery',
  'backbone',
	'app/models/Collaborator'
    ], function($, Backbone, Collaborator) {
	CollaboratorCollection = Backbone.Collection.extend({
	model: Collaborator,
	initialize: function(){
		
	}
	
});
return CollaboratorCollection;
});
