define([
	'jquery',
	'marionette',
	'app/views/ScoreEntryView'
    ], function($, Marionette, ScoreEntryView) {

ScoreBoardView = Backbone.Marionette.CollectionView.extend({
	itemView : ScoreEntryView,
	tagName: 'table',
	className: 'table table-bordered ScoreBoardInnerWrapper',
	collection : ScoreBoard,
	initialize : function() {
	},
	appendHtml: function(collectionView, itemView, index){
    	if(collectionView.children.findByModel(index-1)){
    		collectionView.children.findByModel(index-1).$el.after(itemView.$el);
    	} else {
    		collectionView.$el.append(itemView.$el);
    	}
   },
   ScoreBoardRequestSent: false
});

return ScoreBoardView;
});
