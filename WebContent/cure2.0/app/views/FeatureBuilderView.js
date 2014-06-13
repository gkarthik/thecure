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
		_.bindAll(this,'checkAndTagText', 'validateFeatureName');
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
	url: base_url+"MetaServer",
	className :"feature-builder-wrapper",
	template: FeatureBuilderTemplate,
	ui : {
		'featureExpression': '#feature-expression',
		'hiddenSpan': '#hidden-span',
		"GeneAutoComplete": "#gene-autocomplete-wrapper",
		"featureName": ".feature-name",
		"msgWrapper":"#message-wrapper",
		'buildFeature': "#build-feature"
	},
	events:{
		'click #build-feature': 'buildFeature',
		'keydown #feature-expression': 'checkAndTagText',
		'keyup #feature-expression': 'checkAndTagText',
		'click .gene-autocomplete-option':'getTags',
		'click .duplicate_feature':'addDuplicateFeature'
	},
	tagsList: [],
	prevExp: "",
	focusOptions: -1,
	timoutVar: null,
	checkAndTagText: function(e){
		var thisView = this;
		var featureExpEl = $(this.ui.featureExpression);
		var autocompEl = $(this.ui.GeneAutoComplete);
		var value = featureExpEl.val();
		value = this.adjustIndices(1);//Only 1 char difference occurs here	
		query = (value.match(/@([A-Za-z0-9])+/g)!=null) ? value.match(/@([A-Za-z0-9])+/g)[0] : "";
		var selectOptions = $("#"+autocompEl.attr("id")+" .gene-autocomplete-option");
		var tempIndex = 0;
		if(((e.which == 38) || (e.which==40)) && selectOptions.length>0 && e.type=="keydown"){
			e.preventDefault();
			switch(e.which){
				case 38:
					tempIndex = -1;
					break;
				case 40:
					tempIndex = 1;
					break;
			}
			if(selectOptions[this.focusOptions+tempIndex]){
				this.focusOptions+=tempIndex;
				selectOptions.removeClass("focus-option");
				$(selectOptions[this.focusOptions]).addClass("focus-option");
			}
		} else if(e.which == 13 && selectOptions.length>0 && e.type=="keyup") {
			e.preventDefault();
			$(selectOptions[this.focusOptions]).trigger('click');
		} else if(e.which == 13 && selectOptions.length>0 && e.type=="keydown") {
			e.preventDefault();
		} else if(e.which!=38 && e.which!=40 && e.which!=13) {
			if(query.length>0){
				thisView.timoutVar = window.setTimeout(function(){
					query = (featureExpEl.val().match(/@([A-Za-z0-9])+/g)!=null) ? featureExpEl.val().match(/@([A-Za-z0-9])+/g)[0] : "";
					if(query.length>0){
							if(autocompEl.html()==""){
								autocompEl.html("<li>Loading</li>");
							}
							$.getJSON( "http://mygene.info/v2/query?q=%28symbol%3A"+query.replace("@","")+"+OR+symbol%3A+"+query.replace("@","")+"*+OR+name%3A"+query.replace("@","")+"*+OR+alias%3A+"+query.replace("@","")+"*+OR+summary%3A"+query.replace("@","")+"*%29&limit=20&fields=name%2Csymbol%2Ctaxid%2Centrezgene&species=human&userfilter=bgood_metabric&callback=?", function( data ) {
								thisView.focusOptions = -1;
								var items = [];
								for(var temp in data.hits){
									items.push( "<li id='" + data.hits[temp].entrezgene + "' data-query='"+query+"' data-stringindex= '"+featureExpEl.val().indexOf(query)+"' data-id='"+data.hits[temp].entrezgene+"' data-symbol='"+data.hits[temp].symbol+"' class='gene-autocomplete-option'>" + data.hits[temp].symbol + "</li>" );
								}
								autocompEl.html(items.join(""));
								thisView.setAutoCompPos(featureExpEl.val(),featureExpEl.val().indexOf(query),query);
								autocompEl.show();
							});
					}
					window.clearTimeout(thisView.timoutVar);
				},300);
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
		}
	},
	adjustIndices: function(length,id,symbol){
		var featureExpEl = $(this.ui.featureExpression);
		var value = featureExpEl.val();
		var indexofDiff = -1;
		var prevExp = this.prevExp;
		var counter = 0;
		var lowerCheck = -1;
		var upperCheck = -1;
		if(value.length != prevExp.length){
			while(indexofDiff==-1){
				if(value.substring(0,counter)!=prevExp.substring(0,counter)){
					indexofDiff = counter-1;
					break;
				}
				counter++;
			}
		}
		length = length * (value.length-prevExp.length)/Math.abs(value.length-prevExp.length);
		if(id !=undefined && symbol!=undefined){
			lowerCheck = id;
			upperCheck = id+symbol;
		}
		for(var temp in this.tagsList){
			var id = this.tagsList[temp].id;
			if(id<lowerCheck || id>upperCheck){
				if(id>=(indexofDiff) && indexofDiff!=-1){
					this.tagsList[temp].id+=length;
					$("#tag"+id).attr("id","tag"+this.tagsList[temp].id);
					id+=length;
					this.setTagPos(value,this.tagsList[temp].id,this.tagsList[temp].text);
				}
			}
			if(value.substring(id,this.tagsList[temp].text.length+id)!=this.tagsList[temp].text){
				$("#tag"+id).remove();
				value = value.substring(0,id)+value.substring(this.tagsList[temp].text.length+id-2,value.length-1);
				featureExpEl.val(value);
				this.tagsList.splice(temp,1);
			}
		}
		return value;
	},
	getTags: function(e){
		$(this.ui.GeneAutoComplete).hide();
		var el = e.target;
		var symbol = $(el).data('symbol');
		var entrezid = $(el).data('id');
		var query = $(el).data('query');
		var stringIndex = $(el).data('stringindex');
		//var tags = $(this.ui.featureExpression).val().toUpperCase().match(query);
		var value = $(this.ui.featureExpression).val();
		value = value.substring(0,stringIndex)+symbol+value.substring(query.length+stringIndex,value.length);
		$(this.ui.featureExpression).val(value);
		this.tagsList.push({text:symbol,id:stringIndex, entrezid: entrezid});
		var length = Math.abs(symbol.length-query.length);
		this.addTag(value,stringIndex,symbol,entrezid);
		this.adjustIndices(length,stringIndex,symbol);//Length of tag - Length of query
		this.prevExp = value;
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
		var feature_name = $(this.ui.featureName).val();
		var model = this.model;
		//feature_name, exp, description, user_id, name_dataset.get(dataset), dataset
		
		if(feature_exp != "" && feature_name != ""){
			if(!Cure.initTour.ended()){
				Cure.initTour.end();
			}
			$(this.ui.buildFeature).addClass("disabled");
			$(this.ui.buildFeature).val("Loading...");
			var args = {
	    	        command : "custom_feature_create",
	    	        name: feature_name,
	    	        expression: feature_exp,
	    	        description: $(this.ui.featureExpression).val(),
	    	        dataset: "metabric_with_clinical",
	    	        user_id: Cure.Player.get('id')
	    	      };
	    	      $.ajax({
	    	          type : 'POST',
	    	          url : this.url,
	    	          data : JSON.stringify(args),
	    	          dataType : 'json',
	    	          contentType : "application/json; charset=utf-8",
	    	          success : this.validateFeatureName,
	    	          error: this.error
	    	});
		}
	},
	error : function(data) {
		Cure.utils
    .showAlert("<strong>Server Error</strong><br>Please try saving again in a while.", 0);
	},
	validateFeatureName: function(data){
		$(this.ui.buildFeature).removeClass("disabled");
		$(this.ui.buildFeature).val("Build Feature");
		if(data.success==true){
			if(data.exists==true){
				$(this.ui.msgWrapper).html("<span class='text-danger error-message'>"+data.message+"</span><br><p class='bg-info duplicate_feature' data-name='"+data.name+"' data-description='"+data.description+"' data-id='"+data.id+"'>"+data.name+": "+data.description+"</p>");
			} else {
				this.addFeatureId(data);
			}
		} else {
			$(this.ui.msgWrapper).html("<span class='text-danger error-message'>"+data.message+"</span>");
		}

	},
	addDuplicateFeature: function(e){
		var data = {};
		console.log(e);
		data.id = $(e.currentTarget).data('id');
		data.description = $(e.currentTarget).data('description');
		data.name = $(e.currentTarget).data('name');
		this.addFeatureId(data);
	},
	addFeatureId: function(data){
		var kind_value = "";
		var model = this.model;
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
			model.set("name", data.name);
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
				"unique_id" : data.id,
				"kind" : "split_node",
				"full_name" : '',
				"description" : data.description 
			});
		} else {
			new Node({
				'name' : data.name,
				"options" : {
					"unique_id" : data.id,
					"kind" : "split_node",
					"full_name" : '',
					"description" : data.description
				}
			});
		}
		Cure.PlayerNodeCollection.sync();
		Cure.FeatureBuilderRegion.close();
	},
	error: function(data){
		console.log("error");
	}
});


return FeatureBuilderView;
});
