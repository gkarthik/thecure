define([
    	'backbone',
    	'backboneRelational'
    ], function(Backbone) {
TreeBranch = Backbone.RelationalModel.extend({
	defaults: {
		leafnode: {},
		splitnodes: [],
		splitvalues: []
	}
});
return TreeBranch;
});
