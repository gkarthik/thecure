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
	ui : {

	},
	initialize : function() {
		_.bindAll(this, 'loadNewTree');
		this.model.bind('change', this.render);
		this.$el.click(this.loadNewTree);
	},
	loadNewTree: function(){
		if(this.$el.hasClass("tree-score-entry")){
			Cure.utils.showLoading();
			var json_struct = JSON.stringify(this.model.get('json_tree'));//JSON.stringify to not pass model reference.
			Backbone.Relational.store.reset()
			Cure.CollaboratorCollection.reset();
			Cure.PlayerNodeCollection.reset();
			Cure.PlayerNodeCollection.parseResponse(JSON.parse(json_struct));
			Cure.utils.hideLoading();
		}
	},
	template: ScoreEntryTemplate
});

return ScoreEntryView;
});
