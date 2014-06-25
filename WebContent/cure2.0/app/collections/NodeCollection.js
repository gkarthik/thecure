define([
  //Libraries
	'jquery',
   'backbone',
   'csb',
   //Models
	'app/models/Node',
	'app/models/DistributionData',
	//Views
	'app/views/distributionChartView',
	'text!app/templates/currentRank.html'
    ], function($, Backbone, csb, Node, DistributionData, distributionChartView, CurrentRankTemplate) {
NodeCollection = Backbone.Collection.extend({
	model : Node,
	initialize : function() {
		_.bindAll(this, 'setDistributionData');
	},
	text_branches:{
		branches: [],
	},
	tree_id: 0,
	prevTreeId : -1,
	url : base_url+"MetaServer",
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
			player_id : Cure.Player.get('id')
		};
		
		//POST request to server.		
		$.ajax({
			type : 'POST',
			url : this.url,
			data : JSON.stringify(args),
			dataType : 'json',
			contentType : "application/json; charset=utf-8",
			success : this.parseResponse,
			error : this.error
		});
	},
	getSplitNodeArray: function(){
		var kindArray = this.toJSON();
		var splitNodeArray = [];
		for(var temp in kindArray){
			if(kindArray[temp].options.kind=="split_node"){
				splitNodeArray.push({
					'name': kindArray[temp].name,
					'cid': kindArray[temp].options.cid,
					'nodeGroup':[]//Holds the nodes that are pushed by syncSplitNodeArray in Score View.
				});
			}
		}
		return splitNodeArray;
	},
	updateCollection : function(json_node, node, parent) {
		var thisCollection = this;
		setTimeout(function(){
			if (node != null && json_node != null) {
				for ( var key in json_node) {
					if (key != "children" && key!= "options") {
						node.set(key, json_node[key]);
					} else if(key=="options"){
						node.get(key).set(json_node[key]);
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
			thisCollection.numOfNodesAdded++;
		}, 10);
	},
	responseSize : 0,
	numOfNodesAdded: 0,
	parseTreeinList: function(data){
		Cure.utils.showLoading();
		Backbone.Relational.store.reset();//To remove previous relations.
		Cure.CollaboratorCollection.reset();
		Cure.PlayerNodeCollection.reset();
		Cure.PlayerNodeCollection.prevTreeId = data.id;
		Cure.PlayerNodeCollection.parseResponse(data.json_tree);
		Cure.Comment.set("content",data.comment);
		Cure.Comment.set("flagPrivate",data.private);
		Cure.utils.hideLoading();
	},
	parseResponse : function(data) {
		var jsonsize = Cure.utils.getNumNodesinJSON(data.treestruct);
		Cure.PlayerNodeCollection.numOfNodesAdded = 0;
		//If empty tree is returned, no tree rendered.
		if (data["treestruct"].name) {
			Cure.PlayerNodeCollection.responseSize = jsonsize;
			Cure.PlayerNodeCollection.updateCollection(data["treestruct"], Cure.PlayerNodeCollection.models[0], null);
		} else {
		//If server returns json, render and update positions of nodes.
			Cure.utils.updatepositions(Cure.PlayerNodeCollection);
		}
		var updateScore = true;
		var renderT = window.setInterval(function(){
			if(Cure.PlayerNodeCollection.numOfNodesAdded == parseInt(jsonsize) || parseInt(jsonsize)==1){
				Cure.utils.render_network();
				Cure.utils.hideLoading();
				if(data.distribution_data){
					if(data.distribution_data.dataArray){
						Cure.PlayerNodeCollection.setDistributionData(data.distribution_data);
						updateScore = false;
					}
				}
				if(updateScore){
					//Storing Score in a Score Model.
					var scoreArray = data;
					scoreArray.treestruct = null;
					if(scoreArray.novelty == "Infinity"){
						scoreArray.novelty = 0;
					}
					Cure.Score.set("previousAttributes",Cure.Score.toJSON());
					Cure.Score.set(scoreArray);
				}
				if(Cure.PlayerNodeCollection.models.length==5){
					if(!Cure.treeTour.ended()){
						Cure.treeTour.init();
						if(Cure.startTour){
							Cure.treeTour.start();
						}
					}
				} else if(Cure.PlayerNodeCollection.models.length == 0){
					Cure.Zoom.set('scaleLevel',1);
				}
				Cure.TreeBranchCollection.updateCollection();
				window.clearInterval(renderT);
			}
		},10);
		if(Cure.PlayerNodeCollection.length == 0){
			Cure.Comment.set("content","");
			Cure.ScoreBoardView.render();
			Cure.PlayerNodeCollection.tree_id = 0;
			Backbone.Relational.store.reset();//To remove previous relations.
			Cure.CollaboratorCollection.reset();
		}
		if($("#current-tree-rank").html("")!=""){
			$("#current-tree-rank").html("");
		}
	},
	setDistributionData: function(data){
		var requiredModel = this.findWhere({getSplitData: true});
		if(requiredModel){
			if(requiredModel.get('distribution_data')==null){
				var newDistData = new DistributionData(data);//Assuming only data of 1 model is sent with any request
				requiredModel.set('distribution_data', newDistData);
			} else {
				requiredModel.get('distribution_data').set(data);
			}
			requiredModel.set('getSplitData',false);
			requiredModel.set('showDistChart',true);
		}
	},
	saveTree: function(){
		Cure.Comment.set("saving",1);
		var tree;
		var thisURL = this.url;
    if (Cure.PlayerNodeCollection.models[0]) {
      tree = Cure.PlayerNodeCollection.models[0].toJSON();
      var args = {
        command : "savetree",
        dataset : "metabric_with_clinical",
        treestruct : tree,
        player_id : Cure.Player.get('id'),
        comment : Cure.Comment.get("content"),
        previous_tree_id: Cure.PlayerNodeCollection.prevTreeId,
        privateflag : Cure.Comment.get('flagPrivate')
      };
      $.ajax({
            type : 'POST',
            url : thisURL,
            data : JSON.stringify(args),
            dataType : 'json',
            contentType : "application/json; charset=utf-8",
            success : function(data){
            	Cure.utils.showAlert("Tree Saved!<br />Your tree has been saved.", 1);
            	Cure.PlayerNodeCollection.tree_id = data.tree_id;
            	var badges = data.badges;
            	if(Cure.PlayerNodeCollection.length>0 && Cure.PlayerNodeCollection.tree_id != 0 && Cure.Comment.get('flagPrivate')==0){
          			var args = {
          	        command : "get_rank",
          	        dataset : "metabric_with_clinical",
          	        tree_id: Cure.PlayerNodeCollection.tree_id
          	      };
          	      $.ajax({
          	            type : 'POST',
          	            url : thisURL,
          	            data : JSON.stringify(args),
          	            dataType : 'json',
          	            contentType : "application/json; charset=utf-8",
          	            success : function(data){
          	            	Cure.Comment.set("editView",0);
          	            	Cure.Comment.set("saving",0);
          	            	Cure.ScoreBoard.rank = data.rank;
          	            	Cure.ScoreBoard.updateCurrent();
          	            	Cure.BadgeCollection.reset(badges);
          	            	if(Cure.BadgeCollection.length>0){
          	            		if(Cure.BadgeCollection.pluck("id").indexOf(badge_id)!=-1){
          	            			if(csb.inSession()){
          	            				csb.complete();
          	            			}
          	            		}
          	            		$("#BadgesPlaceholder").html("New Badges Earned!<br><small style='font-size:12px;' class='btn btn-link'>Click to view badges earned.</small>");
          	            	} else {
          	            		$("#BadgesPlaceholder").html("");
          	            	}
          	            	$("#current-tree-rank").html(CurrentRankTemplate({rank:data.rank}));
          	            },
          	            error : function(data){
          	            	
          	            }
          	          });
          		} else {
          			Cure.Comment.set("editView",0);
	            	Cure.Comment.set("saving",0);
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
