define([
      'backbone',
    	'backboneRelational'
    ], function(Backbone) {
Zoom = Backbone.RelationalModel.extend({
	defaults : {
		scaleLevel: 1,
		fitToScreen: true
	}
});

return Zoom;
});
