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
		
	},
	template: FeatureBuilderTemplate,
	ui : {
		'featureExpression': '#feature-expression'
	},
	events:{
		'click #build-feature': 'buildFeature'
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
