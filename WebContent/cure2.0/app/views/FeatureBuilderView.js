define([
  //Libraries
	'jquery',
	'marionette',
	//Templates
	'text!app/templates/FeatureBuilder.html',
	//Plugins
	'myGeneAutocomplete',
	'jqueryui'
    ], function($, Marionette, FeatureBuilderTemplate) {
FeatureBuilderView = Marionette.ItemView.extend({
	initialize : function() {
		$.valHooks.textarea = {
			get: function( elem ) {
				return elem.value;
			}
		};
	},
	template: FeatureBuilderTemplate,
	ui : {
		'featureExpression': '#feature-expression',
		'hiddenSpan': '#hidden-span'
	},
	events:{
		'click #build-feature': 'buildFeature',
		'keyup #feature-expression': 'checkAndTagText'
	},
	checkAndTagText: function(e){
		if($(this.ui.featureExpression).get(0).scrollHeight-11 > $(this.ui.featureExpression).height()){
			$(this.ui.featureExpression).css({'height': $(this.ui.featureExpression).get(0).scrollHeight+5});
		}
		var value = $(this.ui.featureExpression).val();
		var keyword = "EGR3";
		var index = value.toUpperCase().indexOf(keyword);
		if(index!=-1){
			this.addTag(value,index,keyword);
		}
	},
	addTag: function(value,index,keyword){
		var textBeforeKey = value.substring(0,index);
		$(this.ui.hiddenSpan).html(textBeforeKey+keyword);
		var pos = [];
		pos[1] = $(this.ui.hiddenSpan).innerHeight();
		var lines = textBeforeKey.split("\n");
		$(this.ui.hiddenSpan).html(lines[lines.length-1]);
		pos[0] = $(this.ui.hiddenSpan).width()+1;
		console.log(pos);
		//var pos = [$(this.ui.hiddenSpan).width(),$(this.ui.hiddenSpan).height()]
		this.$el.append("<span id='"+keyword+"-tag' class='highlight-tag'>"+value.substring(index,index+keyword.length)+"</span>");
		var tag = $("#"+keyword+"-tag");
		tag.css({'left':pos[0],'top':pos[1]-tag.height()});
	},
	buildFeature: function(){
		var feature_exp = $(this.ui.featureExpression).val();
		if(feature_exp != ""){
			if(!Cure.initTour.ended()){
				Cure.initTour.end();
			}
			var kind_value = "";
			try {
				kind_value = model.get("options").get('kind');
			} catch (exception) {
			}
			if (kind_value == "leaf_node") {
					if(model.get("options")){
						model.get("options").unset("split_point");
					}
					
					if(model.get("distribution_data")){
						model.get("distribution_data").set({
							"range": -1
						});
					}
				model.set("previousAttributes", model.toJSON());
				model.set("name", "New Feature");
				model.set('accLimit', 0, {silent:true});
				
				var index = Cure.CollaboratorCollection.pluck("id").indexOf(Cure.Player.get('id'));
				var newCollaborator;
				if(index!=-1){
					newCollaborator = Cure.CollaboratorCollection.at(index);
				} else {
					newCollaborator = new Collaborator({
						"name": cure_user_name,
						"id": Cure.Player.get('id'),
						"created" : new Date()
					});
					Cure.CollaboratorCollection.add(newCollaborator);
					index = Cure.CollaboratorCollection.indexOf(newCollaborator);
				}
				model.get("options").set({
					"unique_id" : '',
					"kind" : "split_node",
					"full_name" : 'New Feature',
					"description" : feature_exp,
					"feature_exp": feature_exp
				});
			} else {
				new Node({
					'name' : 'New Feature',
					"options" : {
						"unique_id" : '',
						"kind" : "split_node",
						"full_name" : '',
						"description" : feature_exp,
						"feature_exp": feature_exp
					}
				});
			}
			Cure.PlayerNodeCollection.sync();
		}
	}
});


return FeatureBuilderView;
});
