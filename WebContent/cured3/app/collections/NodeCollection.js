define([
  //Libraries
	'jquery',
   'backbone',
   //Models
	'app/models/Node',
	'text!app/templates/currentRank.html'
    ], function($, Backbone, Node, CurrentRankTemplate) {
NodeCollection = Backbone.Collection.extend({
	model : Node,
	initialize : function() {
	},
	text_branches:{
		branches: [],
	},
	tree_id: 0,
	prevTreeId : -1,
	url : "/cure/MetaServer",
	sync : function() {
		//Function to send request to Server with current tree information.
		var tree = [];
		if (this.models[0]) {
			tree = this.models[0].toJSON();
		}
		
		Cure.utils.showLoading(null);
		
		var args = {
			command : "scoretree",
			dataset : "metabric_with_clinical",
			treestruct : tree,
			comment: Cure.Comment.get("content"),
			player_id : cure_user_id
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
		var thisCollection = this;
		setTimeout(function(){
			if (node != null && json_node != null) {
				for ( var key in json_node) {
					if (key != "children") {
						node.set(key, json_node[key]);
					}
				}
				if (json_node.children.length > 0
						&& json_node.children.length == node.get('children').length) {
					for ( var temp in json_node.children) {
						thisCollection.updateCollection(json_node.children[temp],
								node.get('children').models[temp]);
					}
				} else if (json_node.children.length > node.get('children').length) {
					for ( var temp in json_node.children) {
							thisCollection.updateCollection(json_node.children[temp], null, node);
					}
				} else if (json_node.children.length < node.get('children').length) {
					var temp = 0;
					for (temp in json_node.children) {
						thisCollection.updateCollection(json_node.children[temp],
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
						thisCollection.updateCollection(json_node.children[temp], null, newNode);
					}
					Cure.utils.showLoading(Cure.PlayerNodeCollection.length+" of "+Cure.PlayerNodeCollection.responseSize);
			}	
			Cure.utils.updatepositions(Cure.PlayerNodeCollection);
		}, 10);
	},
	responseSize : 0,
	parseTreeinList: function(data){
		Cure.utils.showLoading();
		Backbone.Relational.store.reset();//To remove previous relations.
		Cure.CollaboratorCollection.reset();
		Cure.PlayerNodeCollection.reset();
		Cure.PlayerNodeCollection.prevTreeId = data.id;
		Cure.PlayerNodeCollection.parseResponse(data.json_tree);
		Cure.Comment.set("content",data.comment);
		Cure.utils.hideLoading();
	},
	parseResponse : function(data) {
		var jsonsize = Cure.utils.getNumNodesinJSON(data.treestruct);
		//If empty tree is returned, no tree rendered.
		if (data["treestruct"].name) {
			Cure.PlayerNodeCollection.responseSize = jsonsize;
			Cure.PlayerNodeCollection.updateCollection(data["treestruct"], Cure.PlayerNodeCollection.models[0], null);
		} else {
		//If server returns json with tree render and update positions of nodes.
			Cure.utils.updatepositions(Cure.PlayerNodeCollection);
		}
		var renderT = window.setInterval(function(){
			if(Cure.PlayerNodeCollection.length == parseInt(jsonsize) || parseInt(jsonsize)==1){
				Cure.utils.render_network(Cure.PlayerNodeCollection.toJSON()[0]);
				Cure.utils.hideLoading();
				//Storing Score in a Score Model.
				var scoreArray = data;
				scoreArray.treestruct = null;
				if(scoreArray.novelty == "Infinity"){
					scoreArray.novelty = 0;
				}
				if(Cure.PlayerNodeCollection.models.length==5){
					Cure.treeTour.start();
				} else if(Cure.PlayerNodeCollection.models.length == 0){
					Cure.scaleLevel = 1;
					Cure.utils.transformRegion(Cure.PlayerSvg.attr('transform'),Cure.scaleLevel);
				}
				Cure.Score.set("previousAttributes",Cure.Score.toJSON());
				Cure.Score.set(scoreArray);
				Cure.TreeBranchCollection.updateCollection();
				window.clearInterval(renderT);
			}
		},20);
		if(Cure.PlayerNodeCollection.length == 0){
			Cure.Comment.set("content","");
			Cure.ScoreBoardView.render();
			Cure.PlayerNodeCollection.tree_id = 0;
		}
		if($("#current-tree-rank").html("")!=""){
			$("#current-tree-rank").html("");
		}
	},
	saveTree: function(){
		var tree;
    if (Cure.PlayerNodeCollection.models[0]) {
      tree = Cure.PlayerNodeCollection.models[0].toJSON();
      var args = {
        command : "savetree",
        dataset : "metabric_with_clinical",
        treestruct : tree,
        player_id : cure_user_id,
        comment : Cure.Comment.get("content"),
        previous_tree_id: Cure.PlayerNodeCollection.prevTreeId
      };
      $.ajax({
            type : 'POST',
            url : '/cure/MetaServer',
            data : JSON.stringify(args),
            dataType : 'json',
            contentType : "application/json; charset=utf-8",
            success : function(data){
            	Cure.utils.showAlert("Tree Saved!<br />Your tree has been saved. You can open the Score Board to see your tree's rank.", 1);
            	Cure.PlayerNodeCollection.tree_id = data.tree_id;
            	if(Cure.PlayerNodeCollection.length>0 && Cure.PlayerNodeCollection.tree_id != 0){
          			var args = {
          	        command : "get_rank",
          	        dataset : "metabric_with_clinical",
          	        tree_id: Cure.PlayerNodeCollection.tree_id
          	      };
          	      $.ajax({
          	            type : 'POST',
          	            url : '/cure/MetaServer',
          	            data : JSON.stringify(args),
          	            dataType : 'json',
          	            contentType : "application/json; charset=utf-8",
          	            success : function(data){
          	            	Cure.ScoreBoard.rank = data.rank;
          	            	Cure.ScoreBoard.updateCurrent();
          	            	$("#current-tree-rank").html(CurrentRankTemplate({rank:data.rank}));
          	            },
          	            error : function(data){
          	            	
          	            }
          	          });
          		} else {
          			$("#current-tree-rank").html("");
          		}
            },
            error : function(){
            }
          });
    } else if(Cure.PlayerNodeCollection.length == 0) {
      tree = [];
      Cure.utils
          .showAlert("<strong>Empty Tree!</strong><br>Please build a tree by using the auto complete box.", 0);
    }
	},
	error : function(data) {
		Cure.utils
    .showAlert("<strong>Server Error</strong><br>Please try saving again in a while.", 0);
	}
});

return NodeCollection;
});
