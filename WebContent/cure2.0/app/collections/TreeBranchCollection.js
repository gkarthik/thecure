define([
	'jquery',
  'backbone',
	'app/models/TreeBranch'
    ], function($, Backbone, ClinicalFeature) {
TreeBranchCollection = Backbone.Collection.extend({
	model: TreeBranch,
	url: base_url+'MetaServer',
	initialize: function(){
		_.bindAll(this, 'getTreeinText', 'updateCollection');
	},
	getTreeinText : function(node){
		var tempBranches = [];
		if(node.get('options').get('kind') == "leaf_node" && node.get('children').length == 0){
				var branch = {
						splitnodes: [],
						splitvalues: [],
						leafnode: {}
				};
				branch.leafnode = node.toJSON();
				var tempNode = node.get('parentNode');
				while(tempNode != null){
					if(tempNode.get('options').get('kind') == "split_node"){
						if(tempNode.get('options').get('unique_id').indexOf("metabric") == -1){
							branch.splitnodes.push({node: tempNode.toJSON(), type: "gene"});
						} else if(tempNode.get('options').get('unique_id').indexOf("metabric") != -1) {
							branch.splitnodes.push({node: tempNode.toJSON(), type: "cf"});
						} 
					} else if(tempNode.get('options').get('kind') == "split_value") {
						branch.splitvalues.push(tempNode.toJSON());
					}
					tempNode = tempNode.get('parentNode');
				}
				tempBranches.push(branch);
		}
		if(node.get('children').length>0){
			for(var temp in node.get('children').models){
				tempBranches = tempBranches.concat(this.getTreeinText(node.get('children').models[temp])); 
			}
		}
		return tempBranches;
	},
	updateCollection : function(){
		if(Cure.PlayerNodeCollection.models.length > 0){
			this.reset(this.getTreeinText(Cure.PlayerNodeCollection.models[0]));
		} else {
			this.reset();
		} 
		
	}
});

return TreeBranchCollection;
});
