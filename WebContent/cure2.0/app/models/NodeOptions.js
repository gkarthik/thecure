define([
      	'backbone',
      	'backboneRelational'
    ], function(Backbone) {
	NodeOptions = Backbone.RelationalModel.extend({
	defaults : {
		
	},
	initialize: function(){
		if(Cure.PlayerNodeCollection.models.length > 0) {
			Cure.binScale = d3.scale.linear().domain([ 0, Cure.PlayerNodeCollection.models[0].get('options').get('bin_size') ]).range([ 0, 100 ]);
		} else {
			Cure.binScale = d3.scale.linear().domain([ 0, 239 ]).range([ 0, 100 ]);
		}
		this.listenTo(this,'change', this.updateAccLimit);
		this.updateAccLimit();
	},
	updateAccLimit: function(){
		var accLimit = Cure.binScale(this.get('bin_size'))*(this.get('pct_correct'));
		this.set('accLimit',accLimit);	
	},
});

return NodeOptions;
});
