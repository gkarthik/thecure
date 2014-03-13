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
  	saveButton: "#save_tree"
	},
	template : CommentTemplate,
	events: {
		"change .commentContent": 'saveComment',
		'click #save_tree': 'saveTree',
		'click #init_save_tree': 'initSave'
	},
	initialize : function(){
		_.bindAll(this, 'saveComment');
		this.model.bind('change', this.render);
	},
	saveComment: function(){
		var content = $(this.ui.commentContent).val();
		this.model.set("content",content);
	},
	initSave: function(){
		if(Cure.PlayerNodeCollection.length>0){
			this.model.set("editView",1);
		} else {
			Cure.utils
	    .showAlert("<strong>Empty Tree!</strong><br>Use the autocomplete box to build a tree.", 0);
		}
	},
	saveTree: function(){
		Cure.PlayerNodeCollection.saveTree();
	}
});

return CommentView;
});
