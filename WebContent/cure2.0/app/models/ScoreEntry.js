define([
      	'backbone',
      	'backboneRelational',
    ], function(Backbone) {
ScoreEntry = Backbone.RelationalModel.extend({
	defaults: {
		comment: "",
		created: 0,
		id: 0,
		ip: "",
		rank: 0,
		player_name: "",
		json_tree :{
			novelty : 0,
			pct_correct : 0,
			size : 1,// Least Size got form server = 1.
			score : 0,
			text_tree : '',
			treestruct : {}
		}
	},
	initialize: function(){
		_.bindAll(this, 'updateScore');
		this.bind('change', this.updateScore);
		this.updateScore();
	},
	updateScore: function(){
		if(this.get("json_tree").score != "Score"){
			var scoreVar = this.get('json_tree');
			if(scoreVar.size>=1) {
				scoreVar.score = Math.round(750 * (1 / scoreVar.size) + 
						500 * scoreVar.novelty + 
						1000 * scoreVar.pct_correct);
			} else {
				scoreVar.score = 0;
			}
			this.set("json_tree", scoreVar);
		}
	}
});

return ScoreEntry;
});
