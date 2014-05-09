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
		chart : ".chart",
		collaboratorIcon: ".collaborator-icon",
		distributionChart: ".distribution-chart"
	},
	url: base_url+"MetaServer",
	template : function(serialized_model) {
		if (serialized_model.options.kind == "split_value") {
			return splitValueTemplate({
				name : serialized_model.name,
				highlight : serialized_model.highlight,
				options : serialized_model.options,
				cid : serialized_model.cid,
				collaborator: serialized_model.collaborator
			});
		} else if (serialized_model.options.kind == "split_node") {
			return splitNodeTemplate({
				name : serialized_model.name,
				highlight : serialized_model.highlight,
				options : serialized_model.options,
				cid : serialized_model.cid,
				collaborator: serialized_model.collaborator
			});
		} else if(serialized_model.options.kind=="leaf_node"){
			return LeafNodeTemplate({
				name : serialized_model.name,
				highlight : serialized_model.highlight,
				options : serialized_model.options,
				cid : serialized_model.cid,
				posNodeName : Cure.posNodeName,
				negNodeName : Cure.negNodeName,
				collaborator: serialized_model.collaborator
			});
		}
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
		if(Cure.PlayerNodeCollection.models.length > 0) {
			Cure.binScale = d3.scale.linear().domain([ 0, Cure.PlayerNodeCollection.models[0].get('options').bin_size ]).range([ 0, 100 ]);
		} else {
			Cure.binScale = d3.scale.linear().domain([ 0, 239 ]).range([ 0, 100 ]);
		}
		_.bindAll(this, 'remove', 'addChildren', 'showSummary', 'setaccLimit', 'highlight', 'checkNodeAddition', 'renderPlaceholder', 'parseDistributionData');
		this.listenTo(this.model, 'change:x', this.render);
		this.listenTo(this.model, 'change:y', this.render);
		this.listenTo(this.model, 'change:accLimit', this.render);
		this.listenTo(this.model, 'change:highlight', this.highlight);
		this.listenTo(this.model, 'add:children', this.checkNodeAddition);
		this.listenTo(this.model, 'remove', this.remove);
		this.listenTo(this.model, 'change:collaborator',this.renderPlaceholder);
		this.listenTo(this.model, 'change:getSplitData', this.parseDistributionData);
		
		if(parseFloat(this.model.get('accLimit'))!=0){
			this.model.set('modifyAccLimit',0);
		}
		var accLimit = 0;
		//Setting up accLimit for leaf_node
		if(this.model.get('options').kind=="leaf_node" && this.model.get('modifyAccLimit')==1) {
			accLimit = Cure.binScale(this.model.get('options').bin_size)*(this.model.get('options').pct_correct);
			this.model.set('accLimit',accLimit,{silent: true});
		}
		this.model.set("cid",this.cid);
	},
	getDistributionData: function(){
		if(this.model.get("options").kind=="split_node"){
			this.model.set("getSplitData",true);
			Cure.PlayerNodeCollection.sync();
		}
	},
	parseDistributionData: function(){
		if(this.model.get('getSplitData')==false){
			var data;
			try{
				data = this.model.get('distribution_data').get("dataArray");
			} catch (e) {
				data = [];
			}
			$(this.ui.distributionChart).show();
			var id = $(this.ui.distributionChart).attr("id");
			var globalHeight = 200;
			var globalWidth = 400;
			d3.select("#"+id).select(".chartGroup").remove();
			var SVG = d3.select("#"+id).attr({"height":globalHeight,"width":globalWidth}).append("svg:g").attr("class","chartGroup");
			//Create JSON for value and frequency of value
			var xLength = globalWidth-40;
			var yLength = globalHeight-40;
			var plotValues = [];
			var frequencies = [];
			var isNominal = this.model.get('distribution_data').get("isNominal");
			var splitPoint = this.model.get('options').split_point;
			//Check if numeric or nominal attribute
			if(data.length>0){
				if(!isNominal){
					var range = data[0].value;
					for(var i = 0; i < 11; i++){
						plotValues.push({
							"value": range,
							"frequency": [0,0]//y,n
						});
						range += parseFloat((data[data.length-1].value - data[0].value)/10);
					}
					
					for(var temp in data){
						for(var i = 1; i<11; i ++){
							if(data[temp].value <= plotValues[i].value){
								if(data[temp].classprob==Cure.posNodeName){
									plotValues[i].frequency[0]++;
								} else if(data[temp].classprob==Cure.negNodeName) {
									plotValues[i].frequency[1]++;
								}
								break;
							}
						}
					}
				} else {
					var putInArray = 1;
					var arrayIndex = 0;
					for(var temp in data){
						putInArray = 1;
						arrayIndex = 0;
						for(var i in plotValues){
							if(plotValues[i].value == data[temp].value){
								putInArray = 0;
								arrayIndex = i;
								break;
							}
						}
						if(putInArray){
							plotValues.push({
								"value": data[temp].value,
								"frequency": [0,0]//y,n
							});
						} else {
							if(data[temp].classprob==Cure.posNodeName){
								plotValues[arrayIndex].frequency[0]++;
							} else if(data[temp].classprob==Cure.negNodeName) {
								plotValues[arrayIndex].frequency[1]++;
							}
						}
					}
				}
			}
			var total = 0;
			for(var temp in plotValues){
				total = 0;
				for(var i in plotValues[temp].frequency){
					total += plotValues[temp].frequency[i];
				}
				frequencies.push(total);
			}
			frequencies.sort(function(a,b){return a-b;});
			var rectWidth = (globalWidth-40)/plotValues.length;
			if(isNominal){
				rectWidth = rectWidth/2;
			}
			var valueScale = d3.scale.ordinal().domain(plotValues.map(function(d){return isNominal ? d.value : Math.round(d.value*100)/100;})).rangeBands([0,xLength]);
			var frequencyScale = d3.scale.linear().domain([0,frequencies[frequencies.length-1]]).range([yLength,0]);
			var xAxis = d3.svg.axis().scale(valueScale).orient("bottom");
			var yAxis = d3.svg.axis().scale(frequencyScale).orient("left");
			SVG.append("g").attr("class","axis xaxis").attr("transform", "translate(30,"+(globalHeight-30)+")").call(xAxis);
			SVG.append("g").attr("class","axis yaxis").attr("transform", "translate(30,10)").call(yAxis);
			
			SVG.append("svg:g").attr("class","distLayerGroup");
			var layer = SVG.selectAll(".distLayerGroup").selectAll(".distLayer").data(plotValues);
			
			var layerEnter = layer.enter().append("g").attr("class","distLayer")
			.attr("transform",function(d){
				var translateX = (30 - (rectWidth/2)) + ((globalWidth-40)/(plotValues.length*2)) + parseFloat(valueScale(d.value));
				if(!isNominal){
					translateX = (30 - (rectWidth/2)) + parseFloat(valueScale(d.value));
				}
				var total = 0;
				for(var i in plotValues[temp].frequency){
					total += plotValues[temp].frequency[i];
				}
				return "translate("+translateX+",10)";
			});
			
			//Insert rect on layerEnter
			var totalVal = 0;
			var layerRect = layerEnter.selectAll(".distRect").data(function(d){return d.frequency; });
			console.log(plotValues);
			layerRect.enter().append("rect").attr("class","distRect")
			.attr("width", rectWidth)
	    .style("fill", function(d,i) { return (i==0) ? "blue" : "red"; })
	    .attr("y",function(d,i){
	    	if(i==0){
	    		totalVal = 0;
	    	}
	    	totalVal += frequencyScale(frequencies[frequencies.length-1] - d);
	    	return parseFloat((globalHeight-40)-totalVal);
	    })
	    .attr("height", 0);
			
			layer.selectAll(".distRect").data(function(d){return d.frequency; }).transition().duration(function(d,i){
				return Cure.duration*(i);
			}).attr("height",function(d){
	    	return frequencyScale(frequencies[frequencies.length-1] - d);
			});
			
			if(!isNominal){
				xLength = xLength - ((globalWidth-40)/(plotValues.length));
				var splitScale = d3.scale.linear().domain([plotValues[0].value, plotValues[plotValues.length-1].value]).range([0,xLength]);
				var reverseSplitScale = d3.scale.linear().domain([0,xLength]).range([plotValues[0].value, plotValues[plotValues.length-1].value]);
				var splitPointGroup = SVG.append("g").attr("class","split_point")
				.attr("transform",function(){
					var translateX = 30 + ((globalWidth-40)/(plotValues.length*2)) + parseFloat(splitScale(splitPoint));
					var total = 0;
					for(var i in plotValues[temp].frequency){
						total += plotValues[temp].frequency[i];
					}
					return "translate("+translateX+",10)";
				});
				
				var origin = [0,0];
				var drag = d3.behavior.drag().origin(function() { 
					var t = d3.select(this).attr("transform");
					var origin = String(t).match(/-?[0-9\.]+/g);
					return { x: parseFloat(origin[0]), y: parseFloat(origin[1]) }; 
				}).on("dragstart", function(d) {
					var t = d3.select(this).attr("transform");
	        var origin = String(t).match(/-?[0-9\.]+/g);
	        this.__customorigin__ = { x: parseFloat(origin[0]), y: parseFloat(origin[1]) };
	      }).on("drag", function(){
	      	var o = this.__customorigin__;
	        var dist = parseFloat(d3.event.x);
					var splitValue = reverseSplitScale(dist-30-((globalWidth-40)/(plotValues.length*2)));
	        d3.selectAll(".splitValueLabel").text(Math.round(splitValue*100)/100);
	        d3.select(this).attr("transform","translate("+d3.event.x+","+o.y+")");
		    }).on("dragend",function(){
		    	Cure.PlayerNodeCollection.sync();
		    	delete this.__customorigin__;
		    });
		    
				d3.selectAll(".split_point").call(drag);
				
				splitPointGroup.append("svg:rect").attr("height",globalHeight-40).attr("width",2).attr("fill","steelblue");
				splitPointGroup.append("svg:text").attr("class","splitValueLabel").attr("fill","steelblue").text(Math.round(splitPoint*100)/100).attr("text-anchor","middle").style("font-size","10px");
				var dragHolder = splitPointGroup.append("svg:g").attr("transform","translate(-18,"+((globalHeight-40)/2)+")").attr("class","dragSplitPoint");
				dragHolder.append("svg:rect").attr("height",15).attr("width",40).attr("fill","steelblue").attr("transform","translate(0,-10)");
				dragHolder.append("svg:text").attr("fill","white").text("DRAG").style("font-size","10px").attr("transform","translate(2,2)");
			}
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
	highlight: function(){
		if(this.model.get('highlight')!=0){
			this.$el.addClass("highlightNode");
		} else {
			this.$el.removeClass("highlightNode");
		}
	},
	checkNodeAddition: function(children){
		if(this.model.get('modifyAccLimit')){
			this.setaccLimit(children);
		}
	},
	setaccLimit : function(children){
		if(children.get('options').kind=="leaf_node") {
			if(this.model.get('parentNode').get('modifyAccLimit')){
				var accLimit = 0;
				if(children.get('name')==Cure.negNodeName) {
					accLimit += Cure.binScale(children.get('options').bin_size)*(1-children.get('options').pct_correct);
				} else if(children.get('name') == Cure.posNodeName) {
					accLimit += Cure.binScale(children.get('options').bin_size)*(children.get('options').pct_correct);
				}
				accLimit += this.model.get('parentNode').get('accLimit');
				this.model.get('parentNode').set('accLimit',accLimit);
			}
		}
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
			this.model.set("showJSON", 1);
	},
	onBeforeRender : function() {
		//Render the positions of each node as obtained from d3.
		var numNodes = Cure.utils.getNumNodesatDepth(Cure.PlayerNodeCollection.models[0], Cure.utils.getDepth(this.model));
		
		if(numNodes * 100 >= Cure.width-100){//TODO: find way to get width of node dynamically.
			this.$el.addClass('shrink_'+this.model.get('options').kind);
			this.$el.css({
				width: (Cure.width - 10*numNodes) /numNodes,//TODO: account for border-width and padding programmatically.	
				height: 'auto',
				'font-size': '0.5vw',
				'min-width': '0'
			});
		} else {
			if(this.$el.hasClass('shrink_'+this.model.get('options').kind)){
				this.$el.removeClass('shrink_'+this.model.get('options').kind);
			}
			if(this.model.get('options').kind=='leaf_node'||this.model.get('options').kind=='split_node'){
				try{
					this.$el.css({
						'width': parseFloat(this.model.get('parentNode').get('parentNode').get('viewCSS').width)+"px",
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
							'width': parseFloat(this.model.get('parentNode').get('viewCSS').width)+"px",
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
		this.$el.addClass(this.model.get("options").kind);
		this.model.set("viewCSS",{'width':width});
	}, 
	onRender: function(){
		this.renderPlaceholder();	
		if(this.model.get('options').kind=="leaf_node" || this.model.get('options').kind=="split_node"){
			var id = "#chart"+this.model.get('cid');
			var radius = 4;
			var width = this.model.get('viewCSS').width-20;
			radius = parseFloat((width - 4)/20);
			var limit = Cure.binScale(this.model.get('options').bin_size);
			Cure.utils.drawChart(d3.selectAll(id), limit, this.model.get('accLimit'), radius, this.model.get('options').kind, this.model.get('name'));
			var classToChoose = {"className":"","color":""};
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
	onShow: function(){
		this.render();//To draw chart for final element.
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
