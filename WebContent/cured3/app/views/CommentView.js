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
		commentContent: ".commentContent"
	},
	template : CommentTemplate,
	events: {
		"change .commentContent": 'saveComment',
	},
	initialize : function(){
		_.bindAll(this, 'saveComment');
		this.model.bind('change', this.render);
	},
	saveComment: function(){
		var content = $(this.ui.commentContent).val();
		this.model.set("content",content);
	}
});

return CommentView;
});
