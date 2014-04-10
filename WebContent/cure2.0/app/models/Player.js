define([
      	'backbone',
      	'backboneRelational'
    ], function(Backbone) {
	Player = Backbone.RelationalModel.extend({
	defaults : {
		'username' : '',
		'id': 0,
		showLogin: 0,
		signUp: 0
	}
});

return Player;
});
