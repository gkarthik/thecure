define([
  //Libraries
	'jquery',
	'marionette',
	'backbone',
	//Templates
	'text!app/templates/GenePoolItem.html',
	'jqueryui'
    ], function($, Marionette, Backbone, GenePoolItemTmpl) {
GenePoolItemView = Marionette.ItemView.extend({
	tagName: 'li',
	className: 'list-group-item gene-pool-item',
	events: {
	},
	ui: {
	},
	initialize : function() {
		this.listenTo(this.model,'change', this.render);
	},
	template: GenePoolItemTmpl,
	onRender: function(){
		this.$el.attr("data-index", Cure.GeneCollection.indexOf(this.model));
		this.$el.draggable({
			revert: 'invalid',
			helper: "clone",
			opacity: 0.8
		});
	}
});

return GenePoolItemView;
});
