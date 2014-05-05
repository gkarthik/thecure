define([
    	'backbone',
    	'backboneRelational'
    ], function(Backbone) {
DistributionData = Backbone.RelationalModel.extend({
	defaults: {
		dataArray: []
	}
});
return DistributionData;
});
