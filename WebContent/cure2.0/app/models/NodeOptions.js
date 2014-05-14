define([
      	'backbone',
      	'backboneRelational'
    ], function(Backbone) {
	NodeOptions = Backbone.RelationalModel.extend({
	defaults : {
		kind: "",  
		cid: 0, 
		viewCSS: {}
	},
	initialize: function(){
		this.listenTo(this,'change:bin_size', this.updateAccLimit);
		this.listenTo(this,'change:pct_correct', this.updateAccLimit);
		this.updateAccLimit();
	},
	updateAccLimit: function(){
		if(Cure.PlayerNodeCollection.models.length > 0) {
			Cure.binScale = d3.scale.linear().domain([ 0, Cure.PlayerNodeCollection.at(0).get('options').get('bin_size') ]).range([ 0, 100 ]);
		} else {
			Cure.binScale = d3.scale.linear().domain([ 0, 239 ]).range([ 0, 100 ]);
		}
		var accLimit = Cure.binScale(this.get('bin_size'))*(this.get('pct_correct'));
		this.set('accLimit',accLimit);
	}
});

return NodeOptions;
});
