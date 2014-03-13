define([
  //Libraries
	'jquery',
	'marionette',
	'backbone',
	//Templates
	'text!app/templates/ScoreEntry.html'
    ], function($, Marionette, Backbone, ScoreEntryTemplate) {
ScoreEntryView = Marionette.ItemView.extend({
	tagName: 'tr',
	className: function(){
		if(this.model.get('json_tree').pct_correct!="Accuracy"){
			return "tree-score-entry";
		}
		return "";
	},
	initialize : function() {
		_.bindAll(this, 'loadNewTree');
		this.model.bind('change', this.render);
		this.$el.click(this.loadNewTree);
	},
	loadNewTree: function(){
		if(this.$el.hasClass("tree-score-entry")){
			var data = JSON.stringify(this.model.toJSON());
			Cure.PlayerNodeCollection.parseTreeinList(JSON.parse(data));
		}
	},
	template: ScoreEntryTemplate
});

return ScoreEntryView;
});
