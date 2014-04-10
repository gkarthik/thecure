define([
	'jquery',
	'marionette',
	//Model
	'app/models/Player',
	//Templates
	'text!app/templates/Login.html'
    ], function($, Marionette, Player,LoginTmpl) {
LoginView = Marionette.ItemView.extend({
	model : Player,
	tagName: 'ul',
	events: {
		'click #login-button': 'sendRequest',
		'click #show-login': 'showLogin'
	},
	ui: {
		'username': '#username',
		'password': '#password',
		'loginWrapper': "#LoginRegion form"
	},
	initialize : function() {
		this.listenTo(this.model,'change', this.render);
	},
	template: LoginTmpl,
	showLogin: function(){
		this.model.set('showLogin',1);
	},
	sendRequest: function(){
		 var args = {
	        command : "user_login",
	        username: $(this.ui.username).val(),
	        password: $(this.ui.password).val()
	      };
	      $.ajax({
	            type : 'POST',
	            url : '/cure/SocialServer',
	            data : JSON.stringify(args),
	            dataType : 'json',
	            contentType : "application/json; charset=utf-8",
	            success : this.parseResponse,
	            error: this.error
	      });
	},
	parseResponse: function(data){
		console.log("hello");
		Cure.Player.set('name',data.player_name);
		Cure.Player.set('id',data.player_id);
		if(Cure.CollaboratorCollection.pluck('id').indexOf(data.player_id)==-1){
			var guest = Cure.CollaboratorCollection.findWhere({ id: '-1' });
			if(guest){
				guest.set({
					'name': data.player_name,
					'id': data.player_id
				});
			}
		} else {
			//TODO Remove backbone relational model from store.
		}
	},
	error: function(data){
		if(data.message){
			Cure.utils
	    .showAlert("<strong>Error!</strong><br>"+data.message, 0);
		} else {
			Cure.utils
	    .showAlert("<strong>Server Error!</strong><br>Please try again in a while.", 0);
		}
	}
});

return LoginView;
});
