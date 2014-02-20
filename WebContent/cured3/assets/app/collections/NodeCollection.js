define([
	'jQuery',
    	'backbone',
	'app/models/Node'
    ], function($, Backbone, Node) {
NodeCollection = Backbone.Collection.extend({
	model : Node,
	initialize : function() {
	},
	url : "/cure/MetaServer",
	sync : function() {
		//Function to send request to Server with current tree information.
		var tree = [];
		if (this.models[0]) {
			tree = this.models[0].toJSON();
		}
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
					Cure.delete_all_children(node.get('children').models[i]);
					node.get('children').models[i].destroy();
					i--;
				}
			}
		} else if (node == null) {
			var newNode = new Node({
				'name' : "",
				"options" : {},
			});
			for ( var key in json_node) {
				if (key != "children") {
					newNode.set(key, json_node[key]);
				}
			}
			newNode.set("cid", newNode.cid);
			if (parent != null) {
				parent.get('children').add(newNode);
			}
			for ( var temp in json_node.children) {
				this.updateCollection(json_node.children[temp], null, newNode);
			}
		}
		Cure.updatepositions(Cure.PlayerNodeCollection);
		Cure.render_network(Cure.PlayerNodeCollection.toJSON()[0]);
	},
	parseResponse : function(data) {
		//If empty tree is returned, no tree rendered.
		if (data["treestruct"].name) {
			Cure.PlayerNodeCollection.updateCollection(data["treestruct"], Cure.PlayerNodeCollection.models[0], null);
		} else {
		//If server returns json with tree render and update positions of nodes.
			Cure.updatepositions(Cure.PlayerNodeCollection);
			Cure.render_network(Cure.PlayerNodeCollection.toJSON()[0]);
		}
		
		//Storing Score in a Score Model.
		var scoreArray = data;
		scoreArray.treestruct = null;
		if(scoreArray.novelty == "Infinity"){
			scoreArray.novelty = 0;
		}
		Cure.Score.set("previousAttributes",Cure.Score.toJSON());
		Cure.Score.set(scoreArray);
		
		//Cure.Comment.set("content",data["comment"]); TODO: Include comment in json_tree on server side.
	},
	error : function(data) {
		console.log("Error Receiving Data From Server.");
	}
});

return NodeCollection;
});
