define([
	'jQuery',
	'marionette',
	'app/models/Comment'
    ], function($, Marionette, Comment) {
CommentView = Backbone.Marionette.ItemView.extend({
	tagName: 'div',
	mode: 'Comment',
	className: 'commentBox',
	ui: {
		commentContent: ".commentContent"
	},
	template : "#commentTemplate",
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
		this.model.set("content",$(this.ui.commentContent).val());
		this.model.set("editView",0);
		Cure.PlayerNodeCollection.sync();
	}
});

return CommentView;
});
