define([
      	'backbone',
      	'app/models/Collaborator',
      	'app/models/DistributionData',
      	'app/models/NodeOptions',
      	'backboneRelational'
    ], function(Backbone, Collaborator, DistributionData, NodeOptions) {
	Node = Backbone.RelationalModel.extend({
	defaults : {
		'name' : '',
		'cid' : 0,
		getSplitData: false,
		edit : 0,
		highlight : 0,
		modifyAccLimit: 1,
		children : [],
		manual_pct_correct: 0,
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
			var index = Cure.CollaboratorCollection.pluck("id").indexOf(Cure.Player.get('id'));
			var newCollaborator;
			if(index!=-1){
				newCollaborator = Cure.CollaboratorCollection.at(index);
			} else {
				newCollaborator = new Collaborator({
					"name": Cure.Player.get('username'),
					"id": Cure.Player.get('id'),
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
	},
	{
		type : Backbone.HasOne,
		key : 'distribution_data',
		relatedModel : 'DistributionData',
		includeInJSON: false,
		reverseRelation : {
			type : Backbone.HasOne,	
			key : 'splitNode'
		}
	},
	{
		type : Backbone.HasOne,
		key : 'options',
		relatedModel : 'NodeOptions',
		reverseRelation : {
			type : Backbone.HasOne,	
			key : 'AttributeNode',
			includeInJSON: false
		}
	}]
});

return Node;
});
