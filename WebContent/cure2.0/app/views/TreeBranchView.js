	define([
  //Libraries
	'jquery',
	'marionette',
	//Models
	'app/models/TreeBranch',
	//Templates
	'text!app/templates/TreeBranch.html'
    ], function($, Marionette, TreeBranch, TreeBranchTemplate) {
TreeBranchView = Backbone.Marionette.ItemView.extend({
	tagName: 'div',
	ui: {
		highlightTree: ".highlightBranch"
	},
	events: {
		'click .highlightBranch': 'highlightBranch'
	},
	template : TreeBranchTemplate,
	initialize : function(){
		_.bindAll(this, 'highlightBranch');
	},
	highlightBranch: function(){
		var ListofIds = [];
		ListofIds.push(this.model.get('leafnode').options.cid);
		var splitnodes = this.model.get('splitnodes');
		for(var temp in splitnodes){
			ListofIds.push(splitnodes[temp].node.options.cid);
		}
		var splitvalues = this.model.get('splitvalues');
		for(var temp in splitvalues){
			ListofIds.push(splitvalues[temp].options.cid);
		}
		ListofIds.push(Cure.PlayerNodeCollection.models[0].get('options').get('cid'));//Since Root Node always is highlighted.
		Cure.utils.highlightNodes(Cure.PlayerNodeCollection.models[0],ListofIds);
	}
});

return TreeBranchView;
});
	