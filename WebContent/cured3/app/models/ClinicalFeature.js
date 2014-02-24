define([
      'backbone',
    	'backboneRelational'
    ], function(Backbone) {
ClinicalFeature = Backbone.RelationalModel.extend({
	defaults : {
		description : "",
		id : 0,
		long_name : "",
		short_name : "",
		unique_id : 0
	},
	initialize: function(){
		this.bind('change:long_name', this.updateLabel);
		this.updateLabel();
	},
	updateLabel: function(){
		var label = this.get('long_name')
		this.set("label",label);
	}
});

return ClinicalFeature;
});
