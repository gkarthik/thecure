define([
      	'backbone',
      	'backboneRelational'
    ], function(Backbone) {
	Node = Backbone.RelationalModel.extend({
	defaults : {
		'name' : '',
		'cid' : 0,
		'options' : {
			"id" : "",
			"kind" : "split_node"
		},
		edit : 0,
		highlight : 0,
		children : [],
		gene_summary : {
			"summaryText" : "",
			"goTerms" : {},
			"generif" : {},
			"name" : ""
		},
		accLimit: 0,
		showJSON : 0,
		x: 0,
		y: 0,
		x0 : 0,
		y0: 0
	},
	initialize : function() {
		Cure.PlayerNodeCollection.add(this);
		if(this.get('collaborator')==null){
			var index = Cure.CollaboratorCollection.pluck("id").indexOf(cure_user_id);
			var newCollaborator;
			if(index!=-1){
				newCollaborator = Cure.CollaboratorCollection.at(index);
			} else {
				newCollaborator = new Collaborator({
					"name": cure_user_name,
					"id": cure_user_id,
					"created" : new Date()
				});
				Cure.CollaboratorCollection.add(newCollaborator);
			}
			this.set("collaborator", newCollaborator);
		} else {
			var index = Cure.CollaboratorCollection.pluck("id").indexOf(this.get('collaborator').id);
			if(index == -1){
				Cure.CollaboratorCollection.add(this.get('collaborator'));
			}
		}
	},
	relations : [
	{
		type : Backbone.HasOne,	
		key : 'collaborator',
		relatedModel : 'Collaborator',
		reverseRelation : {
			type: Backbone.HasMany,
			key : 'ownedNodes',
			includeInJSON: false
		}
	}, {
		type : Backbone.HasMany,
		key : 'children',
		relatedModel : 'Node',
		reverseRelation : {
			type : Backbone.HasOne,	
			key : 'parentNode',
			includeInJSON: false
		}
	}]
});

return Node;
});
