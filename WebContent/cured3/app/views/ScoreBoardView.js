define([
	'jquery',
	'marionette',
	'app/views/ScoreEntryView'
    ], function($, Marionette, ScoreEntryView) {

ScoreBoardView = Backbone.Marionette.CollectionView.extend({
	itemView : ScoreEntryView,
	tagName: 'table',
	className: 'table ScoreBoardInnerWrapper',
	collection : ScoreBoard,
	initialize : function() {
		
	}
});

return ScoreBoardView;
});
