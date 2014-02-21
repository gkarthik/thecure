define([
	'jquery',
	'marionette',
    ], function($, Marionette) {
ScoreEntryView = Marionette.ItemView.extend({
	tagName: 'tr',
	ui : {

	},
	initialize : function() {
		_.bindAll(this, 'loadNewTree');
		this.model.bind('change', this.render);
		this.$el.click(this.loadNewTree);
	},
	loadNewTree: function(){
		Cure.utils.showLoading();
		var json_struct = JSON.stringify(this.model.get('json_tree'));//JSON.stringify to not pass model reference.
		Cure.PlayerNodeCollection.parseResponse(JSON.parse(json_struct));
		Cure.utils.hideLoading();
	},
	template: "#ScoreBoardTemplate"
});

return ScoreEntryView;
});
