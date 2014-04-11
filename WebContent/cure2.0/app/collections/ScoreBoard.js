define([
	'jquery',
    	'backbone',
	'app/models/ScoreEntry',
	'text!app/templates/currentRank.html'
    ], function($, Backbone, ScoreEntry, CurrentRankTemplate) {
ScoreBoard = Backbone.Collection.extend({
	model: ScoreEntry,
	initialize : function(){
		_.bindAll(this, 'parseResponse', 'updateCurrent');
	},
	rank: -1,
	upperLimit: 10,
	lowerLimit: 0,
	updateCurrent: function(){
		this.allowRequest = 1;
			if(this.rank!=-1){
				this.lowerLimit = 0;
				this.upperLimit = this.rank + 10;
			}
			var args = {
					command : "get_trees_with_range",
					lowerLimit : this.lowerLimit,
					upperLimit : this.upperLimit,
					orderby: "score"
			};
			var thisCollection = this;
			$.ajax({
				type : 'POST',
				url : this.url,
				data : JSON.stringify(args),
				dataType : 'json',
				contentType : "application/json; charset=utf-8",
				success : function(data){
					var trees = data.trees;
					trees.unshift({
						comment: "Comment",
						created: "Created",
						id: "id",
						ip: "ip",
						player_name: "<i class='glyphicon glyphicon-user'></i>",
						json_tree :{
							novelty : "Nov",
							pct_correct : "Acc",
							size : "Size",
							score : "Score",
							text_tree : '',
							treestruct : {}
						}
					});
					
					if(data.n_trees > 0) {
						thisCollection.reset(trees);
						thisCollection.allowRequest = 1;
					} else {
						thisCollection.allowRequest = 0;
					}
					$("#score-board-outerWrapper").show();
					$("#scoreboard_wrapper").css({
						scrollTop: 0
					});
		    	$("#scoreboard_wrapper").animate({
		    		scrollTop: $('.current_tree').offset().top - $(".ScoreBoardInnerWrapper").offset().top 
		    	}, 2000);
					Cure.ScoreBoardRequestSent = false;
				},
				error : this.error,
				async: true
			});
	},
	comparator: 'rank',
	url : base_url+'MetaServer',
	fetch: function(direction){
		if(this.allowRequest){
			this.lowerLimit = Cure.ScoreBoard.at(Cure.ScoreBoard.length-1).get('rank');
			this.upperLimit = this.lowerLimit + 10;
			var args = {
					command : "get_trees_with_range",
					lowerLimit : this.lowerLimit,
					upperLimit : this.upperLimit,
					orderby: "score"
			};
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
			player_name: "<i class='glyphicon glyphicon-user'></i>",
			json_tree :{
				novelty : "Nov",
				pct_correct : "Acc",
				size : "Size",
				score : "Score",
				text_tree : '',
				treestruct : {}
			}
		});
		
		if(data.n_trees > 0) {
			this.add(trees);
			this.allowRequest = 1;
		} else {
			this.allowRequest = 0;
		}
		Cure.ScoreBoardRequestSent = false;
	},
	error : function(data) {
		Cure.utils
    .showAlert("<strong>Server Error</strong><br>Please try saving again in a while.", 0);
	}
});

return ScoreBoard;

});
