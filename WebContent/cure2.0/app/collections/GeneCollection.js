define([
	'jquery',
  'backbone',
	'app/models/GeneItem'
    ], function($, Backbone, GeneItem) {
GeneCollection = Backbone.Collection.extend({
	model: GeneItem,
	initialize: function(){
		this.listenTo(this, 'reset', this.addFirstRow);
		this.addFirstRow();
	},
	addFirstRow: function(){
		this.add({
			unique_id: "Unique ID",
			long_name: "Long Name",
			short_name: "Short Name",
			source: "Source"
		});
	}
});
return GeneCollection;
});