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
		this.model.set("content",$(this.ui.commentContent).val());
		this.model.set("editView",0);
		var tree;
    if (Cure.PlayerNodeCollection.models[0]) {
      tree = Cure.PlayerNodeCollection.models[0].toJSON();
      var args = {
        command : "savetree",
        dataset : "metabric_with_clinical",
        treestruct : tree,
        player_id : cure_user_id,
        comment : Cure.Comment.get("content")
      };
      $
          .ajax({
            type : 'POST',
            url : '/cure/MetaServer',
            data : JSON.stringify(args),
            dataType : 'json',
            contentType : "application/json; charset=utf-8",
            success : Cure.utils.showAlert("saved"),
            error : Cure.utils
                .showAlert("Error Occured. Please try again in a while.")
          });
    } else {
      tree = [];
      Cure.utils
          .showAlert("Empty Tree!<br>Please build a tree by using the auto complete box.");
    }
	}
});

return CommentView;
});
