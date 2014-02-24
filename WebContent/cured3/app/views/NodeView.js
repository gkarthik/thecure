define([
  //Libraries
	'jquery',
	'marionette',
	'd3',
	//Templates
	'text!app/templates/LeafNode.html',
	'text!app/templates/SplitValue.html',
	'text!app/templates/SplitNode.html'
    ], function($, Marionette, d3, LeafNodeTemplate, splitValueTemplate, splitNodeTemplate) {
NodeView = Marionette.ItemView.extend({
	tagName : 'div',
	className : 'node dragHtmlGroup',
	ui : {
		input : ".edit",
		addgeneinfo : ".addgeneinfo",
		name : ".name",
		chart : ".chart"
	},
	template : function(serialized_model) {
		if (serialized_model.options.kind == "split_value") {
			return splitValueTemplate({
				name : serialized_model.name,
				highlight : serialized_model.highlight,
				options : serialized_model.options,
				cid : serialized_model.cid
			});
		} else if (serialized_model.children.length > 0
				&& serialized_model.options.kind == "split_node") {
			return splitNodeTemplate({
				name : serialized_model.name,
				highlight : serialized_model.highlight,
				options : serialized_model.options,
				cid : serialized_model.cid
			});
		}
		return LeafNodeTemplate({
			name : serialized_model.name,
			highlight : serialized_model.highlight,
			options : serialized_model.options,
			cid : serialized_model.cid,
			posNodeName : Cure.posNodeName,
			negNodeName : Cure.negNodeName
		});
	},
	events : {
		'click button.addchildren' : 'addChildren',
		'click button.delete' : 'removeChildren',
		'click .name' : 'showSummary',
		'blur .name': 'setHighlight',
		'click .chart': 'showNodeDetails'
	},
	initialize : function() {
		if(this.model.get('options').kind != "split_value") {
				this.model.set('accLimit',0);																																																																																																																																																																												
		}
		if(Cure.PlayerNodeCollection.models.length > 0) {
			Cure.binScale = d3.scale.linear().domain([ 0, Cure.PlayerNodeCollection.models[0].get('options').bin_size ]).range([ 0, 100 ]);
		} else {
			Cure.binScale = d3.scale.linear().domain([ 0, 239 ]).range([ 0, 100 ]);
		}
		_.bindAll(this, 'remove', 'addChildren', 'showSummary', 'setaccLimit');
		this.model.bind('change:x', this.render);
		this.model.bind('change:y', this.render);
		this.model.bind('add:children', this.setaccLimit);
		this.model.bind('remove', this.remove);
		
		this.model.set("cid",this.cid);
	},
	setaccLimit : function(children){
		if(children.get('options').kind=="leaf_node") {
			var accLimit = 0;
			if(children.get('name')==Cure.negNodeName) {
				accLimit += Cure.binScale(children.get('options').bin_size)*(1-children.get('options').pct_correct);
			} else if(children.get('name') == Cure.posNodeName) {
				accLimit += Cure.binScale(children.get('options').bin_size)*(children.get('options').pct_correct);
			}
			accLimit += this.model.get('parentNode').get('accLimit');
			this.model.get('parentNode').set('accLimit',accLimit);
		}
		this.render;
	},
	showNodeDetails: function(){
		var datasetBinSize = Cure.PlayerNodeCollection.models[0].get('options').bin_size;
		var content = "";
		//% cases
		if(this.model.get('options').bin_size && this.model.get('options').kind =="leaf_node"){
			content+="<p class='binsizeNodeDetail'><span class='percentDetails'>"+Math.round((this.model.get('options').bin_size/datasetBinSize)*10000)/100+"%</span><span class='textDetail'>of cases from the dataset fall here.</span></p>";
		} else if(this.model.get('options').bin_size && this.model.get('options').kind =="split_node") {
			content+="<p class='binsizeNodeDetail'><span class='percentDetails'>"+Math.round((this.model.get('options').bin_size/datasetBinSize)*10000)/100+"%</span><span class='textDetail'>of cases from the dataset pass through this node.</span></p>";
		}		
		//Accuracy
		if(this.model.get('options').pct_correct){
			content+="<p class='accuracyNodeDetail'><span class='percentDetails'>"+Math.round(this.model.get('options').pct_correct*10000)/100+"%</span><span class='textDetail'>is the percentage accuracy at this node.</span></p>";
		}
		Cure.utils.showDetailsOfNode(content, this.$el.offset().top, this.$el.offset().left);
	},
	setHighlight: function(){
		this.model.set('highlight',0);
	},
	showSummary : function() {
		//showJSON is used to render the Gene Info POP Up.
		if (this.model.get("options").kind == "split_node" || this.model.get("options").kind == "split_value") {
			this.model.set("showJSON", 1);
		}
	},
	onBeforeRender : function() {
		//Render the positions of each node as obtained from d3.
		var numNodes = Cure.utils.getNumNodesatDepth(Cure.PlayerNodeCollection.models[0], Cure.utils.getDepth(this.model));
		
		if(numNodes * 100 >= Cure.width){//TODO: find way to get width of node dynamically.
			this.$el.addClass('shrink_'+this.model.get('options').kind);
			this.$el.css({
				width: (Cure.width - 10*numNodes) /numNodes,//TODO: account for border-width and padding programmatically.	
				height: 'auto',
				'font-size': '0.5vw',
				'min-width': '0px'
			});
		} else {
			if(this.$el.hasClass('shrink_'+this.model.get('options').kind)){
				this.$el.removeClass('shrink_'+this.model.get('options').kind);
			}
			if(this.model.get('options').kind=='leaf_node'||this.model.get('options').kind=='split_node'){
				try{
					this.$el.css({
						'width': this.model.get('parentNode').get('parentNode').get('viewCSS').width+"px",
						'min-width': '0px'
					});
				} catch(e){
						this.$el.css({
							'min-width': "100px"
						});
					}
				} else {
					if(this.model.get('parentNode')!=null){
						this.$el.css({
							'width': parseFloat(this.model.get('parentNode').get('viewCSS').width)+"px",
							'min-width': '0px'
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
		this.$el.addClass(this.model.get("options").kind);
		console.log(this.$el.width());
		this.model.set("viewCSS",{'width':this.$el.outerWidth()});
	}, 
	onRender: function(){
		var id = this.$el.find(".chart").attr('id');
		var accLimit = 0;
		//Setting up accLimit for leaf_node
		if(this.model.get('options').kind=="leaf_node") {
			accLimit = Cure.binScale(this.model.get('options').bin_size)*(this.model.get('options').pct_correct);
			this.model.set('accLimit',accLimit);
		}
		if(id!=undefined){
			id = "#"+id;
			var radius = 4;
			var width = this.model.get('viewCSS').width-20;
			radius = (width - 4)/20;
			var limit = Cure.binScale(this.model.get('options').bin_size);
			Cure.utils.drawChart(d3.select(id), limit, this.model.get('accLimit'), radius, this.model.get('options').kind, this.model.get('name'));
			var classToChoose = [{"className":""},{"color":""}];
			if(this.model.get('name') == Cure.negNodeName){
				classToChoose["className"]= " .posCircle";
				classToChoose["color"]= "red";
			} else{
				classToChoose["className"]= " .posCircle";
				classToChoose["color"]= "blue";
			}
			d3.selectAll(id+classToChoose["className"]).style("fill",classToChoose["color"]);
		}		
		/*
		else if(this.$el.hasClass("shrink_leaf_node") && id!= undefined){
			var bin_size = this.model.get('options').bin_size;
			var accLimit = this.model.get('accLimit'); 
			console.log(accLimit/100)
			var hue = 0;
			if(this.model.get('name')==Cure.posNodeName){//Deviate starting from blue. H = 240 degrees
				hue = 1.2/360 - ((100-accLimit) * 1.2/360);
			} else if(this.model.get('name')==Cure.negNodeName) {//Deviate starting from red. H = 360 degrees
				hue = 1.2/360 - ((accLimit) * 1.2/360);	
			}
			var rgb = Cure.hslToRgb(hue,1,0.5);
			id = "#"+id;
			d3.select(id).style('background',function(){
				return 'rgb('+rgb[0]+','+rgb[1]+','+rgb[2]+')';
			});
		}*/
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
		var GeneInfoRegion = new Backbone.Marionette.Region({
			el : "#" + $(this.ui.addgeneinfo).attr("id")
		});
		Cure.addRegions({
			MyGeneInfoRegion : GeneInfoRegion
		});
		var ShowGeneInfoWidget = new AddRootNodeView({
			'model' : this.model
		});
		Cure.MyGeneInfoRegion.show(ShowGeneInfoWidget);
	}
});
return NodeView;
});
