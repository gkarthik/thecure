define([
	'jquery',
	'marionette',
	//Model
	'app/models/Collaborator',
	//Templates
	'text!app/templates/Collaborators.html'
    ], function($, Marionette, Collaborator, CollaboratorTemplate) {
CollaboratorView = Marionette.ItemView.extend({
	tagName : "tr",
	template: CollaboratorTemplate,
	initialize : function() {
		this.model.bind('change', this.render);
	}
});

return CollaboratorView;
});
