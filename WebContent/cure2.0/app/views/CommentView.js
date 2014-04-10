define([
  //Libraries
	'jquery',
	'marionette',
	//Models
	'app/models/Comment',
	//Templates
	'text!app/templates/Comment.html',
    ], function($, Marionette, Comment, CommentTemplate) {
CommentView = Backbone.Marionette.ItemView.extend({
	tagName: 'div',
	model: 'Comment',
	className: 'commentBox',
	ui: {
		commentContent: ".commentContent",
  	saveButton: "#save_tree",
  	flagPrivate: '#save-private'
	},
	template : CommentTemplate,
	events: {
		"change .commentContent": 'saveComment',
		'click #save_tree': 'saveTree',
		'click #init_save_tree': 'initSave',
		'click #save-private': 'flagPrivate'
	},
	initialize : function(){
		this.model.bind('change', this.render);
	},
	initSave: function(){
		if(Cure.PlayerNodeCollection.length>0 && Cure.CollaboratorCollection.pluck("id").indexOf(Cure.Player.get('id'))!=-1 && Cure.Player.get('id')!=-1){
			this.model.set("editView",1);
		} else if(Cure.PlayerNodeCollection.length==0) {
			Cure.utils
	    .showAlert("<strong>Empty Tree!</strong><br>Use the autocomplete box to build a tree.", 0);
		} else if(Cure.CollaboratorCollection.pluck("id").indexOf(cure_user_id)==-1) {
      Cure.utils
      .showAlert("<strong>You haven't added anything!</strong><br>Please do not save trees built by other users directly.", 0);
		} else if(Cure.Player.get('id') == -1) {
    	tree = [];
      Cure.utils
      .showAlert("<strong>Login to save tree!</strong><br>Please login to save the tree.", 0);
      Cure.Player.set("showLogin",1);
    }
	},
	saveTree: function(){
		this.model.set("content",$(this.ui.commentContent).val());
		Cure.PlayerNodeCollection.saveTree();
	},
	flagPrivate: function(){
		if($(this.ui.flagPrivate).is(':checked')){
			this.model.set("flagPrivate",1);
		} else {
			this.model.set("flagPrivate",0);
		}
	}
});

return CommentView;
});
