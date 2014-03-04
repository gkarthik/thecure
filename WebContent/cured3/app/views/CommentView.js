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
		"click .enter-comment": 'changeView',
		"click .save-comment": 'saveComment'
	},
	initialize : function(){
		this.model.bind('change', this.render);
	},
	changeView: function(){
		this.model.set("editView",1);
	},
	saveComment: function(){
		Cure.PlayerNodeCollection.saveTree();
	}
});

return CommentView;
});
