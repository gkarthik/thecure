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
		_.bindAll(this,'checkAndTagText');
		$.valHooks.textarea = {
			get: function( elem ) {
				return elem.value;
			}
		};
		$(document).on("mouseup",
        function(e) {
          classToclose = $('#FeatureBuilderRegion');
          if (!classToclose.is(e.target)
              && classToclose.has(e.target).length == 0) {
          	Cure.FeatureBuilderRegion.close();
          }
      });
	},
	className :"feature-builder-wrapper",
	template: FeatureBuilderTemplate,
	ui : {
		'featureExpression': '#feature-expression',
		'hiddenSpan': '#hidden-span',
		"GeneAutoComplete": "#gene-autocomplete-wrapper"
	},
	events:{
		'click #build-feature': 'buildFeature',
		'keydown #feature-expression': 'checkAndTagText',
		'keyup #feature-expression': 'checkAndTagText',
		'click .gene-autocomplete-option':'getTags'
	},
	tagsList: [],
	prevExp: "",
	checkAndTagText: function(e){
		var thisView = this;
		var featureExpEl = $(this.ui.featureExpression);
		var autocompEl = $(this.ui.GeneAutoComplete);
		var value = featureExpEl.val();
		var indexofDiff = -1;
		var prevExp = this.prevExp;
		var counter = 1;
		if(value.length > prevExp.length){
			while(indexofDiff==-1){
				if(value.substring(0,counter)!=prevExp.substring(0,counter)){
					indexofDiff = counter-1;
					break;
				}
				counter++;
			}
		}
		for(var temp in this.tagsList){
			var id = this.tagsList[temp].id;
			if(id>=indexofDiff && indexofDiff!=-1){
				this.tagsList[temp].id+=1;
				console.log(this.tagsList[temp].id);
				$("#tag"+id).attr("id","tag"+this.tagsList[temp].id);
				id+=1;
				this.setTagPos(value,this.tagsList[temp].id,this.tagsList[temp].text);
			}
			if(value.substring(id,this.tagsList[temp].text.length+id)!=this.tagsList[temp].text){
				$("#tag"+id).remove();
				value = value.substring(0,id)+value.substring(this.tagsList[temp].text.length+id-2,value.length-1);
				featureExpEl.val(value);
				this.tagsList.splice(temp,1);
			}
		}	
		query = (value.match(/@([A-Za-z0-9])+/g)!=null) ? value.match(/@([A-Za-z0-9])+/g)[0] : "";
		if(query.length>1){
			var t = window.setTimeout(function(){
				query = (featureExpEl.val().match(/@([A-Za-z0-9])+/g)!=null) ? featureExpEl.val().match(/@([A-Za-z0-9])+/g)[0] : "";
				if(query.length>1){
					if(query!=""){
						if(autocompEl.html()==""){
							autocompEl.html("<li>Loading</li>");
						}
						$.getJSON( "http://mygene.info/v2/query?q="+query.replace("@","")+"&fields=symbol,entrezgene&userfilter=bgood_metabric&callback=?", function( data ) {
							var items = [];
							for(var temp in data.hits){
								items.push( "<li id='" + data.hits[temp].entrezgene + "' data-id='"+data.hits[temp].entrezgene+"' data-symbol='"+data.hits[temp].symbol+"' class='gene-autocomplete-option'>" + data.hits[temp].symbol + "</li>" );
							}
							autocompEl.html(items.join(""));
							thisView.setAutoCompPos(featureExpEl.val(),featureExpEl.val().indexOf(query),query);
							autocompEl.show();
						});
					}
				}
				window.clearInterval(t);
			},500);
		} else {
			autocompEl.hide();
		}
		if($(this.ui.featureExpression).get(0).scrollHeight-11 > $(this.ui.featureExpression).height()){
			$(this.ui.featureExpression).css({'height': $(this.ui.featureExpression).get(0).scrollHeight+5});
		}
		if($(this.ui.featureExpression).get(0).scrollWidth > $(this.ui.featureExpression).innerWidth()){
			value = [value.slice(0, value.length-2), "\n", value.slice(value.length-2)].join('');
		}
		$(this.ui.featureExpression).val(value);
		this.prevExp = value;
	},
	getTags: function(e){
		$(this.ui.GeneAutoComplete).hide();
		var el = e.target;
		var symbol = $(el).data('symbol');
		var entrezid = $(el).data('id');
		var tags = $(this.ui.featureExpression).val().toUpperCase().match(/@([A-Za-z0-9])+/g);
		var value = $(this.ui.featureExpression).val();
		var index = 0;
		for(tag in tags){
			index = -1;
			if(symbol.indexOf(tags[tag].replace("@",""))!=-1){
				index = value.toUpperCase().indexOf(tags[tag]);
			}
			if(index!=-1){
				var keyword = value.substring(index,tags[tag].length+index);
				var tagText = symbol;
				var re = new RegExp(keyword, "g");
				value = value.replace(re,tagText);
				$(this.ui.featureExpression).val(value);
				this.tagsList.push({text:tagText,id:index, entrezid: entrezid});
				this.addTag(value,index,tagText,entrezid);
				this.prevExp = value;
			}
		}
	},
	setTagPos: function(value,index,keyword){
		var textBeforeKey = value.substring(0,index);
		$(this.ui.hiddenSpan).html(textBeforeKey+keyword);
		var pos = [];
		pos[1] = $(this.ui.hiddenSpan).innerHeight();
		var lines = textBeforeKey.split("\n");
		$(this.ui.hiddenSpan).html(lines[lines.length-1]);
		pos[0] = $(this.ui.hiddenSpan).width()+1;
		var tag = $("#tag"+index);
		tag.css({'left':pos[0],'top':pos[1]-tag.height()});
	},
	setAutoCompPos: function(value,index,keyword){
		var textBeforeKey = value.substring(0,index);
		$(this.ui.hiddenSpan).html(textBeforeKey+keyword);
		var pos = [];
		pos[1] = $(this.ui.hiddenSpan).innerHeight();
		var lines = textBeforeKey.split("\n");
		$(this.ui.hiddenSpan).html(lines[lines.length-1]);
		pos[0] = $(this.ui.hiddenSpan).width()+1;
		$(this.ui.GeneAutoComplete).css({'left':pos[0],'top':pos[1]});
	},
	addTag: function(value,index,keyword){
		this.$el.append("<span id='tag"+index+"' class='highlight-tag'>"+value.substring(index,index+keyword.length)+"</span>");
		this.setTagPos(value,index,keyword);
	},
	getFeatureExp: function(value){
		var tagsList = this.tagsList;
		var re;
		for(var temp in tagsList){
			re = new RegExp(tagsList[temp].text, "g");
			value = value.replace(re,"@"+tagsList[temp].entrezid);
		}
		return value;
	},
	buildFeature: function(){
		var feature_exp = this.getFeatureExp($(this.ui.featureExpression).val());
		var model = this.model;
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
					"unique_id" : "",
					"kind" : "split_node",
					"full_name" : '',
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
			Cure.FeatureBuilderRegion.close();
		}
	}
});


return FeatureBuilderView;
});
