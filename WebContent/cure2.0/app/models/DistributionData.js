define([
    	'backbone',
    	'backboneRelational'
    ], function(Backbone) {
DistributionData = Backbone.RelationalModel.extend({
	defaults: {
		dataArray: [],
		globalHeight: 200,
		globalWidth: 400
	}
});
return DistributionData;
});
