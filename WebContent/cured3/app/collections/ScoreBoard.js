define([
	'jquery',
    	'backbone',
	'app/models/ScoreEntry',
	'text!app/templates/currentRank.html'
    ], function($, Backbone, ScoreEntry, CurrentRankTemplate) {
ScoreBoard = Backbone.Collection.extend({
	model: ScoreEntry,
	initialize : function(){
		_.bindAll(this, 'parseResponse');
	},
	refresh: function(){
    this.lowerLimit = 0;
    this.upperLimit = 10;
    this.reset();
    this.fetch();
	},
	upperLimit: 10,
	lowerLimit: 0,
	url : '/cure/MetaServer',
	fetch: function(){
		if(this.allowRequest){
			var args = {
					command : "get_trees_with_range",
					lowerLimit : this.lowerLimit,
					upperLimit : this.upperLimit,
					orderby: "score"
			};
			this.lowerLimit+=10;
			this.upperLimit+=10;
			$.ajax({
				type : 'POST',
				url : this.url,
				data : JSON.stringify(args),
				dataType : 'json',
				contentType : "application/json; charset=utf-8",
				success : this.parseResponse,
				error : this.error,
				async: true
			});
		}
	},
	allowRequest : 1,
	parseResponse : function(data) {
		//If empty tree is returned, no tree rendered.
		var trees = data.trees;
		trees.unshift({
			comment: "Comment",
			created: "Created",
			id: "id",
			ip: "ip",
			player_name: "Player",
			json_tree :{
				novelty : "Novelty",
				pct_correct : "Accuracy",
				size : "Size",
				score : "Score",
				text_tree : '',
				treestruct : {}
			}
		});
		if(data.n_trees > 0) {
			this.add(trees);
		} else {
			this.allowRequest = 0;
		}
		Cure.ScoreBoardRequestSent = false;
		if(Cure.ScoreBoard.pluck("id").indexOf(Cure.PlayerNodeCollection.tree_id)=="-1" && Cure.PlayerNodeCollection.length>0){
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
	            	$("#current-tree-rank").html(CurrentRankTemplate({rank:data.rank}));
	            },
	            error : function(data){
	            	//$("#current-tree-rank").html(CurrentRankTemplate({rank:data.rank}));
	            }
	          });
		} else {
			$("#current-tree-rank").html("");
		}
	},
	error : function(data) {

	}
});

return ScoreBoard;

});
