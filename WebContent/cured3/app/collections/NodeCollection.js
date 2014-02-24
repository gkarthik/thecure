define([
  //Libraries
	'jquery',
   'backbone',
   //Models
	'app/models/Node',
	//Templates
	'text!app/templates/NodeCollectionText.html'
    ], function($, Backbone, Node, TextTmpl) {
NodeCollection = Backbone.Collection.extend({
	model : Node,
	initialize : function() {
	},
	text_branches:{
		branches: [],
	},
	url : "/cure/MetaServer",
	sync : function() {
		//Function to send request to Server with current tree information.
		var tree = [];
		if (this.models[0]) {
			tree = this.models[0].toJSON();
		}
		
		Cure.utils.showLoading();
		
		var args = {
			command : "scoretree",
			dataset : "metabric_with_clinical",
			treestruct : tree,
			comment: Cure.Comment.get("content")
		};
		
		//POST request to server.		
		$.ajax({
			type : 'POST',
			url : '/cure/MetaServer',
			data : JSON.stringify(args),
			dataType : 'json',
			contentType : "application/json; charset=utf-8",
			success : this.parseResponse,
			error : this.error
		});
	},
	updateCollection : function(json_node, node, parent) {
		if (node != null && json_node != null) {
			for ( var key in json_node) {
				if (key != "children") {
					node.set(key, json_node[key]);
				}
			}
			if (json_node.children.length > 0
					&& json_node.children.length == node.get('children').length) {
				for ( var temp in json_node.children) {
					this.updateCollection(json_node.children[temp],
							node.get('children').models[temp]);
				}
			} else if (json_node.children.length > node.get('children').length) {
				for ( var temp in json_node.children) {
					this.updateCollection(json_node.children[temp], null, node);
				}
			} else if (json_node.children.length < node.get('children').length) {
				var temp = 0;
				for (temp in json_node.children) {
					this.updateCollection(json_node.children[temp],
							node.get('children').models[temp]);
				}
				if(json_node.children.length>0){
					temp++;
				}
				for ( var i = temp; i < node.get('children').length; i++) {
					Cure.utils.delete_all_children(node.get('children').models[i]);
					node.get('children').models[i].destroy();
					i--;
				}
			}
		} else if (node == null) {
			var newValues = {};
			for ( var key in json_node) {
				if (key != "children") {
					newValues[key] = json_node[key];
				}
			}
			var newNode = new Node(newValues);
			
			if (parent != null) {
				parent.get('children').add(newNode);
			}
			for ( var temp in json_node.children) {
				this.updateCollection(json_node.children[temp], null, newNode);
			}
		}
		Cure.utils.updatepositions(Cure.PlayerNodeCollection);
		Cure.utils.render_network(Cure.PlayerNodeCollection.toJSON()[0]);
	},
	parseResponse : function(data) {
		//If empty tree is returned, no tree rendered.
		if (data["treestruct"].name) {
			Cure.PlayerNodeCollection.updateCollection(data["treestruct"], Cure.PlayerNodeCollection.models[0], null);
			Cure.utils.updatepositions(Cure.PlayerNodeCollection);
			Cure.utils.render_network(Cure.PlayerNodeCollection.toJSON()[0]);	
		} else {
		//If server returns json with tree render and update positions of nodes.
			Cure.utils.updatepositions(Cure.PlayerNodeCollection);
			Cure.utils.render_network(Cure.PlayerNodeCollection.toJSON()[0]);
		}
		
		//Storing Score in a Score Model.
		var scoreArray = data;
		scoreArray.treestruct = null;
		if(scoreArray.novelty == "Infinity"){
			scoreArray.novelty = 0;
		}
		Cure.utils.hideLoading();
		Cure.Score.set("previousAttributes",Cure.Score.toJSON());
		Cure.Score.set(scoreArray);
		
		//Cure.Comment.set("content",data["comment"]); TODO: Include comment in json_tree on server side.
	},
	error : function(data) {
		console.log("Error Receiving Data From Server.");
	},
	getTreeinText : function(node){
		if(node == undefined){
			node = this.models[0];
			this.text_branches = {
					branches :[]
			};
		}
		if(node.get('options').kind == "leaf_node" && node.get('children').length == 0){
				var branch = {
						splitnodes: [],
						splitvalues: [],
						leafnode: {}
				};
				branch.leafnode = node.toJSON();
				var tempNode = node.get('parentNode');
				while(tempNode != null){
					if(tempNode.get('options').kind == "split_node"){
						if(tempNode.get('options').id.indexOf("metabric") == -1){
							branch.splitnodes.push({node: tempNode.toJSON(), type: "gene"});
						} else if(tempNode.get('options').id.indexOf("metabric") != -1) {
							branch.splitnodes.push({node: tempNode.toJSON(), type: "cf"});
						} 
					} else if(tempNode.get('options').kind == "split_value") {
						branch.splitvalues.push(tempNode.toJSON());
					}
					tempNode = tempNode.get('parentNode');
				}
				this.text_branches.branches.push(branch);
		}
		if(node.get('children').length>0){
			for(var temp in node.get('children').models){
				this.getTreeinText(node.get('children').models[temp]);
			}
		}
	},
	renderText : function(){
		var html = TextTmpl(this.text_branches);
		$("body").append(html);
	}
});

return NodeCollection;
});
