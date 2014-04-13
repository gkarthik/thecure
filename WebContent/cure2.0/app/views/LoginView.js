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
		'click #show-login': 'showLogin',
		'click #close-login': 'closeLogin',
		'click .signup-prompt': 'showSignUp',
		'click #signup-button': 'sendSignupRequest',
		'click #close-signup': 'closeSignUp'
	},
	ui: {
		'username': '#username',
		'password': '#password',
		'loginWrapper': "#LoginRegion form",
		'newusername': '#new-username',
		'newpassword': '#new-password',
		'newemail': '#new-email'
	},
	url: base_url+"SocialServer",
	initialize : function() {
		this.listenTo(this.model,'change', this.render);
		_.bindAll(this,'parseResponse', 'parseSignupResponse');
	},
	sendSignupRequest: function(){
		var args = {
        command : "user_signup",
        username: $(this.ui.newusername).val(),
        password: $(this.ui.newpassword).val(),
        email: $(this.ui.newemail).val()
      };
      $.ajax({
            type : 'POST',
            url : this.url,
            data : JSON.stringify(args),
            dataType : 'json',
            contentType : "application/json; charset=utf-8",
            success : this.parseSignupResponse,
            error: this.error
      });
	},
	parseSignupResponse: function(data){
		if(data.success==true){
			Cure.utils.showAlert("<strong>Success!</strong><br>You are logged in as "+data.player_name, 1);
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
			this.closeSignUp();
		} else {
			Cure.utils
	    .showAlert("<strong>Error!</strong><br>"+data.message, 0);
		}
	},
	closeSignUp: function(){
		this.model.set("signUp",0);
		$(this.ui.newusername).val("");
    $(this.ui.newpassword).val("");
    $(this.ui.newemail).val("");
	},
	showSignUp: function(){
		this.model.set("showLogin",0);
		this.model.set("signUp",1);
	},
	closeLogin: function(){
		this.model.set("showLogin",0);
		$(this.ui.username).val("");
    $(this.ui.password).val("");
	},
	template: LoginTmpl,
	showLogin: function(){
		this.model.set('showLogin',1);
		this.model.set('signUp',0);
	},
	sendRequest: function(){
		 var args = {
	        command : "user_login",
	        username: $(this.ui.username).val(),
	        password: $(this.ui.password).val()
	      };
	      $.ajax({
	            type : 'POST',
	            url : this.url,
	            data : JSON.stringify(args),
	            dataType : 'json',
	            contentType : "application/json; charset=utf-8",
	            success : this.parseResponse,
	            error: this.error
	      });
	},
	parseResponse: function(data){
		if(data.success==true){
			Cure.utils.showAlert("<strong>Success!</strong><br>You are logged in as "+data.player_name, 1);
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
			this.closeLogin();
		} else {
			Cure.utils
	    .showAlert("<strong>Error!</strong><br>"+data.message, 0);
		}
	},
	error: function(data){
			Cure.utils
	    .showAlert("<strong>Server Error!</strong><br>Please try again in a while.", 0);
	}
});

return LoginView;
});
