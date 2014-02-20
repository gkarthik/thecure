define([
    	'backboneRelational'
    ], function(BackboneRelational) {
	Node = BackboneRelational.extend({
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
		showJSON : 0,
		x: 0,
		y: 0,
		x0 : 0,
		y0: 0
	},
	initialize : function() {
		Cure.PlayerNodeCollection.add(this);
	},
	relations : [ {
		type : Backbone.HasMany,
		key : 'children',
		relatedModel : 'Node',
		reverseRelation : {
			key : 'parentNode',
			includeInJSON: false
		}
	} ]
});

return Node;
});
