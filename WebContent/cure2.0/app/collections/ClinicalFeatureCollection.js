define([
	'jquery',
  'backbone',
	'app/models/ClinicalFeature'
    ], function($, Backbone, ClinicalFeature) {
ClinicalFeatureCollection = Backbone.Collection.extend({
	model: ClinicalFeature,
	url: base_url+'MetaServer',
	initialize: function(){
		_.bindAll(this, 'parseResponse');
	},
	fetch: function(){
		var args = {
				command : "get_clinical_features",
				dataset : "metabric_with_clinical"
		};
		$.ajax({
			type : 'POST',
			url : this.url,
			data : JSON.stringify(args),
			dataType : 'json',
			contentType : "application/json; charset=utf-8",
			success : this.parseResponse,
			error : this.error,
		});
	},
	parseResponse : function(data) {
		if(data.features.length > 0) {
			this.add(data.features);
		}
	},
	error : function(data) {

	}
});

return ClinicalFeatureCollection;
});
