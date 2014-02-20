define([
	'jQuery',
    	'backbone',
	'app/models/ScoreEntry'
    ], function($, Backbone, ScoreEntry) {
ScoreBoard = Backbone.Collection.extend({
	model: ScoreEntry,
	initialize : function(){
		_.bindAll(this, 'parseResponse');
	},
	upperLimit: 10,
	lowerLimit: 0,
	url : '/cure/MetaServer',
	fetch: function(){
		var args = {
				command : "get_trees_with_range",
				lowerLimit : this.lowerLimit,
				upperLimit : this.upperLimit,
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
	},
	parseResponse : function(data) {
		//If empty tree is returned, no tree rendered.
		if(data.n_trees > 0) {
			this.add(data.trees);
		}
		Cure.ScoreBoardRequestSent = false;
	},
	error : function(data) {

	}
});

return ScoreBoard;

});
