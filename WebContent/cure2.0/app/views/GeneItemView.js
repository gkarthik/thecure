define([
  //Libraries
	'jquery',
	'marionette',
	'backbone',
	//Templates
	'text!app/templates/GeneItem.html',
	'jqueryui'
    ], function($, Marionette, Backbone, GeneItemTmpl) {
GeneItemView = Marionette.ItemView.extend({
	tagName: 'tr',
	events: {
		'click .keepAll': 'selectAllGenes',
			'click .keepInCollection':'selectGene'
	},
	ui: {
		'keepAll': '.keepAll',
		'keepInCollection': '.keepInCollection'
	},
	initialize : function() {
		this.listenTo(this.model,'change', this.render);
		this.$el.attr("id",this.cid);
	},
	template: GeneItemTmpl,
	selectAllGenes: function(){
		if($(this.ui.keepAll).is(':checked')){
			this.model.set('keepAll',1);
			Cure.GeneCollection.forEach(function(model, index) {
				if(model.get('unique_id')!="Unique ID"){
			    model.set('keepInCollection', 1);
				}
			});
		} else {
			this.model.set('keepAll',0);
			Cure.GeneCollection.forEach(function(model, index) {
		    model.set('keepInCollection', 0);
			});
		}
	},
	selectGene: function(){
		if($(this.ui.keepInCollection).is(':checked')){
			this.model.set('keepInCollection',1);
		} else {
			this.model.set('keepInCollection',0);
		}
	}
});

return GeneItemView;
});
