define([
	'jQuery',
	'marionette',
	'app/views/ScoreEntryView'
    ], function($, Marionette, ScoreEntryView) {

ScoreBoardView = Backbone.Marionette.CollectionView.extend({
	itemView : ScoreEntryView,
	tagName: 'table',
	collection : ScoreBoard,
	className : "ScoreBoardInnerWrapper table",
	initialize : function() {
		this.collection.bind('add', this.render);
	}
});

return ScoreBoardView;
});
