define([
  //Libraries
	'jquery',
	'marionette',
	'd3',
	//View
	'app/views/optionsView',
	'app/views/distributionChartView',
	//Templates
	'text!app/templates/LeafNode.html',
	'text!app/templates/SplitValue.html',
	'text!app/templates/SplitNode.html'
    ], function($, Marionette, d3, optionsView, distributionChartView, LeafNodeTemplate, splitValueTemplate, splitNodeTemplate) {
NodeView = Marionette.Layout.extend({
	tagName : 'div',
	className : 'node dragHtmlGroup',
	ui : {
		input : ".edit",
		addgeneinfo : ".addgeneinfo",
		name : ".name",
		chart : ".chart",
		collaboratorIcon: ".collaborator-icon",
		distributionChart: ".distribution-chart"
	},
	url: base_url+"MetaServer",
	template : function(serialized_model) {
		if (serialized_model.options.kind == "split_value") {
			return splitValueTemplate(serialized_model);
		} else if (serialized_model.options.kind == "split_node") {
			return splitNodeTemplate(serialized_model);
		} else if(serialized_model.options.kind=="leaf_node"){
			return LeafNodeTemplate(serialized_model);
		}
	},
	regions: {
		chartRegion: ".chartRegion",
		distributionChartRegion: ".distributionChartRegion",
		addGeneRegion : ".addgeneinfo"
	},
	events : {
		'click button.addchildren' : 'addChildren',
		'click button.delete' : 'removeChildren',
		'click .name' : 'showSummary',
		'blur .name': 'setHighlight',
		'click .chart': 'showNodeDetails',
		'click .showDistribution': 'getDistributionData'
	},
	initialize : function() {
		_.bindAll(this, 'remove', 'addChildren', 'showSummary', 'renderPlaceholder');
		this.listenTo(this.model, 'change:x', this.render);
		this.listenTo(this.model, 'change:y', this.render);
		this.listenTo(this.model, 'remove', this.remove);
		this.listenTo(this.model, 'change:collaborator',this.renderPlaceholder);
		this.listenTo(this.model, 'change:getSplitData',this.showDistView);
		
		var options = this.model.get('options');
		options.set('cid',this.cid);
		
		var thisModel = this.model;
		 $(document).mouseup(
        function(e) {
          classToclose = $('.distribution-chart');
          if (!classToclose.is(e.target)
              && classToclose.has(e.target).length == 0) {
	           thisModel.distributionChartRegion.close();
          }
      });
	},
	getDistributionData: function(){
		if(this.model.get("options").get('kind')=="split_node"){
			this.model.set("getSplitData",true);
			Cure.PlayerNodeCollection.sync();
		}
	},
	showDistView: function(){
		if(this.model.get('getSplitData')==false){
			var newdistChartView = new distributionChartView({model: this.model.get('distribution_data')});
			this.distributionChartRegion.show(newdistChartView);
		}
	},
	error: function(){
		Cure.utils
    .showAlert("<strong>Server Error</strong><br>Please try again in a while.", 0);
	},
	renderPlaceholder: function(){
		if(this.model.get('collaborator')!=null){
			this.$el.find(".collaborator-icon").css({//Change .find to .ui.el
				color: Cure.colorScale(Cure.CollaboratorCollection.indexOf(this.model.get('collaborator'))),
				border: "2px solid "+Cure.colorScale(Cure.CollaboratorCollection.indexOf(this.model.get('collaborator')))
			});
		}	
	},
	showNodeDetails: function(){
		var datasetBinSize = Cure.PlayerNodeCollection.models[0].get('options').get('bin_size');
		var content = "";
		//% cases
		if(this.model.get('options').get('bin_size') && this.model.get('options').get('kind') =="leaf_node"){
			content+="<p class='binsizeNodeDetail'><span class='percentDetails'>"+Math.round((this.model.get('options').get('bin_size')/datasetBinSize)*10000)/100+"%</span><span class='textDetail'>of cases from the dataset fall here.</span></p>";
		} else if(this.model.get('options').get('bin_size') && this.model.get('options').get('kind') =="split_node") {
			content+="<p class='binsizeNodeDetail'><span class='percentDetails'>"+Math.round((this.model.get('options').get('bin_size')/datasetBinSize)*10000)/100+"%</span><span class='textDetail'>of cases from the dataset pass through this node.</span></p>";
		}		
		//Accuracy
		if(this.model.get('options').get('pct_correct')){
			content+="<p class='accuracyNodeDetail'><span class='percentDetails'>"+Math.round(this.model.get('options').get('pct_correct')*10000)/100+"%</span><span class='textDetail'>is the percentage accuracy at this node.</span></p>";
		}
		Cure.utils.showDetailsOfNode(content, this.$el.offset().top, this.$el.offset().left);
	},
	setHighlight: function(){
		this.model.set('highlight',0);
	},
	showSummary : function() {
			this.model.set("showJSON", 1);
	},
	onBeforeRender : function() {
		//Render the positions of each node as obtained from d3.
		var numNodes = Cure.utils.getNumNodesatDepth(Cure.PlayerNodeCollection.models[0], Cure.utils.getDepth(this.model));
		
		if(numNodes * 100 >= Cure.width-100){//TODO: find way to get width of node dynamically.
			this.$el.addClass('shrink_'+this.model.get('options').get('kind'));
			this.$el.css({
				width: (Cure.width - 10*numNodes) /numNodes,//TODO: account for border-width and padding programmatically.	
				height: 'auto',
				'font-size': '0.5vw',
				'min-width': '0'
			});
		} else {
			if(this.$el.hasClass('shrink_'+this.model.get('options').get('kind'))){
				this.$el.removeClass('shrink_'+this.model.get('options').get('kind'));
			}
			if(this.model.get('options').get('kind')=='leaf_node'||this.model.get('options').get('kind')=='split_node'){
				try{
					this.$el.css({
						'width': parseFloat(this.model.get('parentNode').get('parentNode').get('options').get('viewCSS').width)+"px",
						'min-width': '0'
					});
				} catch(e){
						this.$el.css({
							'width': "100px"
						});
					}
				} else {
					if(this.model.get('parentNode')!=null){
						this.$el.css({
							'width': parseFloat(this.model.get('parentNode').get('options').get('viewCSS').width)+"px",
							'min-width': '0'
						});
					}
				}
			}
		var width = this.$el.outerWidth();
		var nodeTop = (this.model.get('y')+71);
		var styleObject = {
			"left": (this.model.get('x') - ((width) / 2)) +"px",
			"top": nodeTop +"px",
			"background": "#FFF",
			"border-color": "#000"
		};
		if(this.model.get('name')==Cure.negNodeName) {
			styleObject.background = "rgba(255,0,0,0.2)";
			styleObject.borderColor = "red";
		} else if(this.model.get('name')==Cure.posNodeName) {
			styleObject.background = "rgba(0,0,255,0.2)";
			styleObject.borderColor = "blue";
		}
		this.$el.attr('class','node dragHtmlGroup');//To refresh class every time node is rendered.
		this.$el.css(styleObject);
		var options = this.model.get('options');
		this.$el.addClass(options.get('kind'));
		options.set("viewCSS", {'width':width});
	}, 
	remove : function() {
		//Remove and destroy current node.
		this.$el.remove();
		Cure.utils.delete_all_children(this.model);
		this.model.destroy();
	},
	removeChildren : function() {
		//Remove all children from current node.
		if (this.model.get('parentNode') != null) {
			Cure.utils.delete_all_children(this.model);
			var prevAttr = this.model.get("previousAttributes");
				for ( var key in prevAttr) {
						this.model.set(key, prevAttr[key]);
				}
				this.model.set("previousAttributes", []);
		} else {
			Cure.utils.delete_all_children(this.model);
			this.model.destroy();
		}
		Cure.PlayerNodeCollection.sync();
	},
	addChildren : function() {
		//Adding new children to the node. This function shows the AddRootNodeView to show mygeneinfo autocomplete.
		var ShowGeneInfoWidget = new AddRootNodeView({
			'model' : this.model
		});
		this.addGeneRegion.show(ShowGeneInfoWidget);
	},
	onRender: function(){
		if(this.model.get('options').get('kind')=="split_node" || this.model.get('options').get('kind')=="leaf_node"){
			var newOptionsView = new optionsView({model: this.model.get('options')});
			this.chartRegion.show(newOptionsView);
		}
		this.renderPlaceholder();	
	},
	onShow: function(){
		this.render();//To draw chart for final element.
	},
});
return NodeView;
});
