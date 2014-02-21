define([
      	'backbone',
      	'backboneRelational'
    ], function(Backbone) {
	Score = Backbone.RelationalModel.extend({
	defaults : {
		novelty : 0,
		pct_correct : 0,
		size : 0,// Least Size got form server = 1.
		score : 0,
		scoreDiff : 0,
		sizeDiff : 0,
		pct_correctDiff : 0,
		noveltyDiff : 0,
		previousAttributes: {}
	},
	initialize: function(){
		this.bind('change:size', this.updateScore);
	},
	updateScore: function(){
		if (this.get("size") > 1) {
			var oldScore = this.get('score');
			var score = 750 * (1 / this.get("size")) + 500
					* this.get("novelty") + 1000 * this.get("pct_correct");
			this.set("score", Math.round(score));
			this.set("sizeDiff",parseFloat(this.get('size')-this.get('previousAttributes').size));
			this.set("pct_correctDiff",parseFloat(this.get('pct_correct')-this.get('previousAttributes').pct_correct));
			this.set("noveltyDiff",parseFloat(this.get('novelty')-this.get('previousAttributes').novelty));
			this.set("scoreDiff",parseFloat(Math.round(score)-oldScore));
		} else {
			this.set({
				"score" : 0,
				"size" : 0,
				"pct_correct" : 0,
				"novelty" : 0,
				"scoreDiff": 0,
				"sizeDiff": 0,
				"pct_correctDiff": 0,
				"noveltyDiff": 0,
			});
		}
	}
});
return Score;
});
