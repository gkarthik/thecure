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
		previousAttributes: {},
		changeFlag: 0
	},
	initialize: function(){
		this.listenTo(this, 'change:score', this.updateScore);
		this.listenTo(this, 'change:pct_correct', this.updateScore);
		this.listenTo(this, 'change:size', this.updateScore);
		this.listenTo(this, 'change:novelty', this.updateScore);
	},
	updateScore: function(){
		this.set("changeFlag",(this.get('changeFlag')+1));
		if (this.get("size") > 1) {
			var oldScore = this.get('score');
			var score = Cure.scoreWeights.size * (1 / this.get("size")) + Cure.scoreWeights.novelty
					* this.get("novelty") + Cure.scoreWeights.pct_correct * this.get("pct_correct");
			this.set("score", Math.round(score));
			if(this.get('previousAttributes').size!=0){
				this.set("sizeDiff",parseInt((Cure.scoreWeights.size * (1 / this.get("size")) - (Cure.scoreWeights.size * (1 / this.get('previousAttributes').size)))));
			} else {
				this.set("sizeDiff",parseInt(Cure.scoreWeights.size * (1 / this.get("size"))));
			}
			this.set("pct_correctDiff",parseInt((Cure.scoreWeights.pct_correct * this.get("pct_correct"))-(Cure.scoreWeights.pct_correct * this.get('previousAttributes').pct_correct)));
			this.set("noveltyDiff",parseInt((Cure.scoreWeights.novelty*this.get('novelty'))-(Cure.scoreWeights.novelty*this.get('previousAttributes').novelty)));
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
