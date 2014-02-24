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
			if(this.get('previousAttributes').size!=0){
				this.set("sizeDiff",parseInt(750 * (1 / this.get("size")-750 * (1 / this.get('previousAttributes').size))));
			} else {
				this.set("sizeDiff",parseInt(750 * (1 / this.get("size")- 0)));
			}
			this.set("pct_correctDiff",parseInt((1000 * this.get("pct_correct"))-(1000 * this.get('previousAttributes').pct_correct)));
			this.set("noveltyDiff",parseInt((500*this.get('novelty'))-(500*this.get('previousAttributes').novelty)));
			this.set("scoreDiff",parseInt(Math.round(score)-oldScore));
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
